
public class DISK extends FileSystem {

    protected Console console;
    protected CPU cpu;
    protected MEM mem;

    public DISK(CPM cpm) {
		super(cpm);
        this.console = cpm.console;
        this.cpu = cpm.cpu;
        this.mem = cpm.mem;
    }   

    // typedef struct {
    //     uint8 dr;
    //     uint8 fn[8];
    //     uint8 tp[3];
    //     uint8 ex, s1, s2, rc;
    //     uint8 al[16];
    //     uint8 cr, r0, r1, r2;
	// } CPM_FCB;
	
	class CPM_FCB {
		char dr = 0x00; // 0 -> 'A'
		char[] fn = new char[8];
		char[] tp = new char[3];
		char ex, s1, s2, rc;
		char[] al = new char[16];
		char cr, r0, r1, r2;
	}

	// CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB readFCBFromRamSysAddr(int fcbaddr) {
		CPM_FCB result = new CPM_FCB();
		int address = fcbaddr;
		result.dr = mem._RamRead(address++);
		for(int i=0; i < 8; i++) { result.fn[i] = mem._RamRead(address++); }
		for(int i=0; i < 3; i++) { result.tp[i] = mem._RamRead(address++); }
		result.ex = mem._RamRead(address++);
		result.s1 = mem._RamRead(address++);
		result.s2 = mem._RamRead(address++);
		result.rc = mem._RamRead(address++);
		for(int i=0; i < 16; i++) { result.al[i] = mem._RamRead(address++); }
		result.cr = mem._RamRead(address++);
		result.r0 = mem._RamRead(address++);
		result.r1 = mem._RamRead(address++);
		result.r2 = mem._RamRead(address++);
		return result;
	}

/*
Cf global.h

#define BATCHA			// If this is defined, the $$$.SUB will be looked for on drive A:
//#define BATCH0		// If this is defined, the $$$.SUB will be looked for on user area 0
						// The default behavior of DRI's CP/M 2.2 was to have $$$.SUB created on the current drive/user while looking for it
                        // on drive A: current user, which made it complicated to run SUBMITs when not logged to drive A: user 0
*/



/*
Disk errors
*/
final int errWRITEPROT = 1;
final int errSELECT = 2;

// #define RW	(roVector & (1 << F->dr))
boolean RW(CPM_FCB F) {
    return (cpm.roVector & (1 << F.dr)) != 0;
}

/*
FCB related numbers
*/
final int BlkSZ = 128;	// CP/M block size
final int BlkEX = 128;	// Number of blocks on an extension
final int BlkS2 = 4096;	// Number of blocks on a S2 (module)
final int MaxCR = 128;	// Maximum value the CR field can take
final int MaxRC = 127;	// Maximum value the RC field can take
final int MaxEX = 31;	// Maximum value the EX field can take
final int MaxS2 = 15;	// Maximum value the S2 (modules) field can take - Can be set to 63 to emulate CP/M Plus

void _error(int error) {
	console._puts("\r\nBdos Error on ");
	console._putcon( (char)((int)'A' + (int)cpm.cDrive) );
	console._puts(" : ");
	switch (error) {
	case errWRITEPROT:
        console._puts("R/O");
		break;
	case errSELECT:
        console._puts("Select");
		break;
	default:
        console._puts("\r\nCP/M ERR");
		break;
	}
	cpu.setStatus( console._getch() );
	console._puts("\r\n");
	cpm.cDrive = cpm.oDrive;
	mem._RamWrite(0x0004, (char)( (mem._RamRead(0x0004) & 0xf0) | cpm.oDrive) );
	cpu.setStatus(2);
}

// BEWARE here dr = 0 -> 0 + 'A' => 'A'
// dr != 'A'
int _SelectDisk(char dr) {
	char result = 0xff;
	charP disk = new charP( new char[] { 'A', 0x00 } );
	// [] = { 'A', 0 };

	if (dr == 0) {
		dr = cpm.cDrive;	// This will set dr to defDisk in case no disk is passed
	} else {
		--dr;			// Called from BDOS, set dr back to 0=A: format
	}

	//disk[0] += dr;
	disk.set( 0, (char)(disk.get(0) + dr) );

	//if (_sys_select(&disk[0])) {
	if (_sys_select(disk.reset()) != FALSE) {
		cpm.loginVector = cpm.loginVector | (1 << (disk.get(0) - 'A'));
		result = 0x00;
	} else {
		_error(errSELECT);
	}

	return(result);
}

//uint8 _FCBtoHostname(uint16 fcbaddr, uint8 *filename) {
char _FCBtoHostname(int fcbaddr, charP filename) {
	char addDot = (char)TRUE;
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char i = 0;
	char unique = (char)TRUE;

	if (F->dr) {
		//*(filename++) = (F->dr - 1) + 'A';
		filename.ptr[ filename.ptrA++ ] = ((char) ((int)(F.dr - 1) + (int)'A') );
	} else {
		// *(filename++) = cDrive + 'A';
		filename.ptr[ filename.ptrA++ ] = ((char) ((int)(cpm.cDrive) + (int)'A') );
	}
	// *(filename++) = FOLDERCHAR;
	filename.ptr[ filename.ptrA++ ] = FOLDERCHAR;

	// *(filename++) = toupper(tohex(userCode));
	filename.ptr[ filename.ptrA++ ] = DataUtils.toupper(DataUtils.tohex(userCode));
	// *(filename++) = FOLDERCHAR;
	filename.ptr[ filename.ptrA++ ] = FOLDERCHAR;

	while (i < 8) {
		if (F.fn[i] > 32) {
			//*(filename++) = toupper(F->fn[i]);
			filename.ptr[ filename.ptrA++ ] = DataUtils.toupper( F.fn[i] );
		}
		if (F.fn[i] == '?') {
			unique = FALSE;
		}
		++i;
	}
	i = 0;
	while (i < 3) {
		if (F->tp[i] > 32) {
			if (addDot) {
				addDot = FALSE;
				// *(filename++) = '.';	// Only add the dot if there's an extension
				filename.ptr[ filename.ptrA++ ] = '.';
			}
			// *(filename++) = toupper(F->tp[i]);
			filename.ptr[ filename.ptrA++ ] = DataUtils.toupper( F.tp[i] );
		}
		if (F->tp[i] == '?')
			unique = FALSE;
		++i;
	}
	// *filename = 0x00;
	filename.ptr[ filename.ptrA++ ] = 0x00;

	return(unique);
}

void _HostnameToFCB(int fcbaddr, charP filename) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char i = 0;

	// ++filename;
	filename.ptrA++;
	if (filename.get() == FOLDERCHAR) {	// Skips the drive and / if needed
		//filename += 3;
		filename.ptrA+=3;
	} else {
		//--filename;
		filename.ptrA--;
	}

	while (filename.get() != 0 && filename.get() != '.') {
		F.fn[i] = DataUtils.toupper(filename.get());
		filename.inc();
		++i;
	}

	while (i < 8) {
		F.fn[i] = ' ';
		++i;
	}
	if (filename.get() == '.')
		filename.inc();

	i = 0;
	while (filename.get() != 0) {
		F.tp[i] = DataUtils.toupper(filename.get());
		filename.inc();
		++i;
	}
	while (i < 3) {
		F.tp[i] = ' ';
		++i;
	}
}

void _HostnameToFCBname(charP from, charP to) {	// Converts a string name (AB.TXT) to FCB name (AB      TXT)
	int i = 0;

	from.inc();
	if (from.get() == FOLDERCHAR) {	// Skips the drive and / if needed
		from.inc(3);
	} else {
		from.dec();
	}

	while (from.get() != 0 && from.get() != '.') {
		to.set( DataUtils.toupper(from.get()) );
		to.inc(); from.inc(); ++i;
	}
	while (i < 8) {
		to.set( ' ' );
		to.inc();  ++i;
	}
	if (from.get() == '.')
		from.inc();
	i = 0;
	while (from.get() != 0) {
		to.set( DataUtils.toupper(from.get()) );
		to.inc(); from.inc(); ++i;
	}
	while (i < 3) {
		to.set(' ');
		to.inc();  ++i;
	}
	to.set( (char)0x00);
}

char match(charP fcbname, charP pattern) {
	char result = 1;
	char i;

	for (i = 0; i < 12; ++i) {
		if (pattern.get() == '?' || pattern.get() == fcbname.get()) {
			pattern.inc(); fcbname.inc();
			continue;
		} else {
			result = 0;
			break;
		}
	}
	return(result);
}

long _FileSize(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	long r, l = -1;

	//if (!_SelectDisk(F.dr)) {
	if ( _SelectDisk(F.dr) == 0) {
		_FCBtoHostname(fcbaddr, cpm.filename.reset() );
		l = _sys_filesize(cpm.filename);
		r = l % BlkSZ;
		if (r != 0)
			l = l + BlkSZ - r;
	}
	return(l);
}

char _OpenFile(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;
	long len;
	//int32 i;
	int i;

	if ( _SelectDisk(F.dr) == 0 ) {
		_FCBtoHostname(fcbaddr, cpm.filename.reset());
		if (_sys_openfile(cpm.filename.reset()) != 0) {

			len = _FileSize(fcbaddr) / 128;	// Compute the len on the file in blocks

			F.s1 = 0x00;
			F.s2 = 0x00;
	
			F.rc = len > MaxRC ? 0x80 : DataUtils.int8(len);
			for (i = 0; i < 16; ++i)	// Clean up AL
				F.al[i] = 0x00;

			result = 0x00;
		}
	}
	return(result);
}

char _CloseFile(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	if (_SelectDisk(F.dr) == 0) {
		if (!RW(F)) {
			_FCBtoHostname(fcbaddr, cpm.filename.reset());
			if (fcbaddr == cpm.BatchFCB)
				_Truncate(cpm.filename, F.rc);	// Truncate $$$.SUB to F->rc CP/M records so SUBMIT.COM can work
			result = 0x00;
		} else {
			_error(errWRITEPROT);
		}
	}
	return(result);
}

char _MakeFile(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;
	char i;

	if ( _SelectDisk(F.dr) == 0 ) {
		if (!RW(F)) {
			_FCBtoHostname(fcbaddr, cpm.filename.reset());
			if (_sys_makefile(cpm.filename.reset()) != 0) {
				F.ex = 0x00;	// Makefile also initializes the FCB (file becomes "open")
				F.s1 = 0x00;
				F.s2 = 0x00;
				F.rc = 0x00;
				for (i = 0; i < 16; ++i)	// Clean up AL
					F.al[i] = 0x00;
				F.cr = 0x00;
				result = 0x00;
			}
		} else {
			_error(errWRITEPROT);
		}
	}
	return(result);
}

char _SearchFirst(int fcbaddr, char isdir) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	// if (!_SelectDisk(F.dr)) {
	if (_SelectDisk(F.dr) == 0 ) {
		_FCBtoHostname(fcbaddr, cpm.filename.reset());
		result = _findfirst(isdir);
	}
	return(result);
}

char _SearchNext(int fcbaddr, char isdir) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	//if (!_SelectDisk(F->dr))
	if (_SelectDisk(F.dr) == 0 ) 
		result = _findnext(isdir);

	return(result);
}

char _DeleteFile(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);

// FIXME
// #if defined(USE_PUN) || defined(USE_LST)
// 	CPM_FCB *T = (CPM_FCB*)_RamSysAddr(tmpFCB);
// #endif

	char result = 0xff;
	char deleted = 0xff;

	//if (!_SelectDisk(F.dr)) {
	if ( _SelectDisk(F.dr) == 0) {
		if (!RW(F)) {
			result = _SearchFirst(fcbaddr, FALSE);	// FALSE = Does not create a fake dir entry when finding the file
			while (result != 0xff) {
// #ifdef USE_PUN
// 				if (!strcmp((char*)T->fn, "PUN     TXT") && pun_open) {
// 					_sys_fclose(pun_dev);
// 					pun_open = FALSE;
// 				}
// #endif
// #ifdef USE_LST
// 				if (!strcmp((char*)T->fn, "LST     TXT") && lst_open) {
// 					_sys_fclose(lst_dev);
// 					lst_open = FALSE;
// 				}
// #endif
				_FCBtoHostname(mem.tmpFCB, cpm.filename.reset());
				if (_sys_deletefile(cpm.filename.reset()))
					deleted = 0x00;
				result = _SearchFirst(fcbaddr, FALSE);	// FALSE = Does not create a fake dir entry when finding the file
			}
		} else {
			_error(errWRITEPROT);
		}
	}
	return(deleted);
}

char _RenameFile(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	if ( _SelectDisk(F.dr) == 0 ) {
		if (!RW(F)) {
			mem._RamWrite(fcbaddr + 16, mem._RamRead(fcbaddr));	// Prevents rename from moving files among folders
			_FCBtoHostname(fcbaddr + 16, cpm.newname.reset());
			_FCBtoHostname(fcbaddr, cpm.filename.reset() );
			if (_sys_renamefile(cpm.filename.reset(), cpm.newname.reset()))
				result = 0x00;
		} else {
			_error(errWRITEPROT);
		}
	}
	return(result);
}

char _ReadSeq(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	long fpos =	((F.s2 & MaxS2) * BlkS2 * BlkSZ) + 
				(F.ex * BlkEX * BlkSZ) + 
				(F.cr * BlkSZ);

	if ( _SelectDisk(F.dr) == 0 ) {
		_FCBtoHostname(fcbaddr, cpm.filename.reset());
		result = _sys_readseq(cpm.filename.reset(), fpos);
		if (result == 0) {	// Read succeeded, adjust FCB
			F.cr++;
			if (F.cr > MaxCR) {
				F.cr = 1;
				F.ex++;
			}
			if (F.ex > MaxEX) {
				F.ex = 0;
				F.s2++;
			}
			if (F.s2 > MaxS2)
				result = 0xfe;	// (todo) not sure what to do 
		}
	}
	return(result);
}

char _WriteSeq(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	long fpos =	((F.s2 & MaxS2) * BlkS2 * BlkSZ) +
				(F.ex * BlkEX * BlkSZ) + 
				(F.cr * BlkSZ);

	if (_SelectDisk(F->dr) == 0) {
		if (!RW(F)) {
			_FCBtoHostname(fcbaddr, cpm.filename.reset());
			result = _sys_writeseq(cpm.filename.reset(), fpos);
			if (result == 0) {	// Write succeeded, adjust FCB
				F.cr++;
				if (F.cr > MaxCR) {
					F.cr = 1;
					F.ex++;
				}
				if (F.ex > MaxEX) {
					F.ex = 0;
					F.s2++;
				}
				if (F.s2 > MaxS2)
					result = 0xfe;	// (todo) not sure what to do 
			}
		} else {
			_error(errWRITEPROT);
		}
	}
	return(result);
}

char _ReadRand(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	//int32 (uses 24 bits ?)
	//int32 record = (F->r2 << 16) | (F->r1 << 8) | F->r0;
	int record = (F.r2 << 16) | (F.r1 << 8) | F.r0;
	long fpos = record * BlkSZ;

	if (_SelectDisk(F.dr) == 0) {
		_FCBtoHostname(fcbaddr, cpm.filename.reset());
		result = _sys_readrand(cpm.filename.reset(), fpos);
		if (result == 0) {	// Read succeeded, adjust FCB
			F.cr = (char)(record & 0x7F);
			F.ex = (char)((record >> 7) & 0x1f);
			F.s2 = (char)((record >> 12) & 0xff);
		}
	}
	return(result);
}

char _WriteRand(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;

	// int32 (uses 24 bits ?)
	int record = (F.r2 << 16) | (F.r1 << 8) | F.r0;
	long fpos = record * BlkSZ;

	if (_SelectDisk(F.dr) == 0) {
		if ( !RW(F) ) {
			_FCBtoHostname(fcbaddr, cpm.filename.reset());
			result = _sys_writerand(cpm.filename.reset(), fpos);
			if (result == 0) {	// Write succeeded, adjust FCB
				F.cr = (char)(record & 0x7F);
				F.ex = (char)((record >> 7) & 0x1f);
				F.s2 = (char)((record >> 12) & 0xff);
			}
		} else {
			_error(errWRITEPROT);
		}
	}
	return(result);
}

char _GetFileSize(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0xff;
	int count = (int)_FileSize(cpu.DE.get()) >> 7;

	if (count != -1) {
		F.r0 = (char)(count & 0xff);
		F.r1 = (char)((count >> 8) & 0xff);
		F.r2 = (char)((count >> 16) & 0xff);
		result = 0x00;
	}
	return(result);
}

char _SetRandom(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	char result = 0x00;

	int count = F.cr & 0x7f;
	count += (F.ex & 0x1f) << 7;
	count += F.s2 << 12;

	F.r0 = (char)(count & 0xff);
	F.r1 = (char)((count >> 8) & 0xff);
	F.r2 = (char)((count >> 16) & 0xff);

	return(result);
}

void _SetUser(char user) {
	cpm.userCode = (char)(user & 0x1f);	// BDOS unoficially allows user areas 0-31
							// this may create folders from G-V if this function is called from an user program
							// It is an unwanted behavior, but kept as BDOS does it
	_MakeUserDir();			// Creates the user dir (0-F[G-V]) if needed
}

char _MakeDisk(int fcbaddr) {
	//CPM_FCB *F = (CPM_FCB*)_RamSysAddr(fcbaddr);
	CPM_FCB F = readFCBFromRamSysAddr(fcbaddr);
	return(_sys_makedisk(F.dr));
}

char _CheckSUB() {
	char result;
	char oCode = cpm.userCode;							// Saves the current user code (original BDOS does not do this)
	_HostnameToFCB( mem.tmpFCB, new charP("$???????.???") );	// The original BDOS in fact only looks for a file which start with $
// #ifdef BATCHA
	mem._RamWrite( mem.tmpFCB, (char)0x01);						// Forces it to be checked on drive A:
// #endif
// #ifdef BATCH0
// 	userCode = 0;									// Forces it to be checked on user 0
// #endif
	result = (_SearchFirst( mem.tmpFCB, (char)FALSE) == 0x00) ? (char)0xff : (char)0x00;
	cpm.userCode = oCode;								// Restores the current user code
	return(result);
}



}