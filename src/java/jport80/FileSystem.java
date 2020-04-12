import java.io.File;
import java.util.List;

public abstract class FileSystem {

    protected static int TRUE = DataUtils.TRUE;
    protected static int FALSE = DataUtils.FALSE;
    protected static final char FOLDERCHAR = '/';

    protected CPM cpm;
    protected MEM mem;

    public FileSystem(CPM cpm) {
        this.cpm = cpm;
        this.mem = cpm.mem;
    }

    void _driveLedOn() {
        // emulates DRIVE-UNIT led
    }

    void _driveLedOff() {
        // emulates DRIVE-UNIT led
    }


    long _sys_filesize(charP filename) {
        // FIXME : TODO ...
        return -1;
    }

    int _sys_select(charP disk) {
        // FIXME : TODO ...
        return -1;
    }


    char _sys_makedisk(char drive) {
        // FIXME : TODO ...
        return 0x00;
    }

    abstract void _HostnameToFCBname(charP from, charP to);
    abstract void _HostnameToFCB(int fcbaddr, charP filename);
    abstract char match(charP fcbname, charP pattern);



    void _MakeUserDir() {
        char dFolder = (char) ((int)cpm.cDrive + (int)'A');
        char uFolder = DataUtils.toupper(DataUtils.tohex(cpm.userCode));
    
        charP path = new charP( new char[] { dFolder, FOLDERCHAR, uFolder, 0 } );
    
        _driveLedOn();
        SD.mkdir(path.toString());
        _driveLedOff();
    }




    char _findnext(char isdir) {
        // File f;
        char result = 0xff;
        charP dirname = new charP(13);
        // uint8 dirname[13];
        boolean isfile;
    
        _driveLedOn();

        //while (f = root.openNextFile()) {
        for(SDFile f : root) {
            //f.getName((char*)&dirname[0], 13);
            f.getName( dirname.reset(), 13);
            isfile = !f.isDirectory();
            f.close();
            if (!isfile)
                continue;
            _HostnameToFCBname(dirname, cpm.fcbname);
            if (match(cpm.fcbname, cpm.pattern) != 0) {
                if (isdir != 0) {
                    _HostnameToFCB(cpm.dmaAddr, dirname);
                    mem._RamWrite(cpm.dmaAddr, (char)0x00);
                }
                mem._RamWrite(mem.tmpFCB, (char)(cpm.filename.get(0) - '@') );
                _HostnameToFCB(mem.tmpFCB, dirname);
                result = 0x00;
                break;
            }
        }
        _driveLedOff();
        return(result);
    }
    
    List<SDFile> root = null;

    char _findfirst(char isdir) {
        // uint8 path[4] = { '?', FOLDERCHAR, '?', 0 };
        charP path = new charP( new char[] { '?', FOLDERCHAR, '?', 0 } );

        path.set(0, cpm.filename.get(0) );
        // path[0] = filename[0];
        // path[2] = filename[2];
        path.set(2, cpm.filename.get(2) );

        // if (root)
        //     root.close();
        if ( root != null ) {
            root.clear();
            root = null;
        }

        // root = SD.open((char *)path); // Set directory search to start from the first position
        root = SD.ls( path.toString() );

        _HostnameToFCBname(cpm.filename, cpm.pattern);
        return(_findnext(isdir));
    }

    int _sys_renamefile(charP filename, charP newname) {
        // File f;
        int result = 0;
      
        _driveLedOn();
        // f = SD.open((char *)filename, O_WRITE | O_APPEND);
        // if (f) {
        //   if (f.rename(SD.vwd(), (char*)newname)) {
        //     f.close();      
        //     result = 1;
        //   }
        // }

        // filename as filePath
        // newName as fileName only
        if ( SD.renamePathToName( filename.toString(), newname.toString() ) ) {
            result = 1;
        }
        _driveLedOff();
        return(result);
    }


    int _sys_openfile(charP filename) {
        // File f;
        int result = 0;
    
        _driveLedOn();
        // f = SD.open((char *)filename, O_READ);
        // if (f) {
        //     f.close();
        //     result = 1;
        // }
        if ( SD.exists( filename.toString() ) ) {
            result = 1;
        }
        _driveLedOff();
        return(result);
    }

    char _Truncate(charP filename, char rc) {
        SDFile f;
        char result = 0;
      
        _driveLedOn();
        f = SD.open( filename.toString(), SD.O_WRITE | SD.O_APPEND);
        if (f != null) {
          if (SD.truncate(f, rc*128)) {
            // f.close();
            result = 1;
          }
        }
        _driveLedOff();
        return result;
    }

    int _sys_makefile(charP filename) {
        SDFile f;
        int result = 0;
    
        _driveLedOn();
        f = SD.open(filename.toString(), SD.O_CREAT | SD.O_WRITE);
        if (f != null ) {
            f.close();
            result = 1;
        }
        _driveLedOff();
        return(result);
    }

    int _sys_deletefile(charP filename) {
        _driveLedOn();
        int result = (SD.remove(filename.reset().toString()) ? 1 : 0);
        _driveLedOff();
        return result;
    }
        

    char _sys_readseq(charP filename, long fpos) {
        char result = 0xff;
        SDFile f = null;
        char bytesread;
        char[] dmabuf = new char[128];
        char i;
    
        _driveLedOn();
        if (_sys_extendfile(filename, fpos))
            f = SD.open( filename.toString(), SD.O_READ);
        if (f != null) {
            if (f.seek(fpos)) {
                for (i = 0; i < 128; ++i)
                    dmabuf[i] = 0x1a;
                // bytesread = f.read(&dmabuf[0], 128);
                bytesread = (char)f.read(dmabuf, 128);
                if (bytesread != 0) {
                    for (i = 0; i < 128; ++i)
                        mem._RamWrite(cpm.dmaAddr + i, dmabuf[i]);
                }
                result = (bytesread != 0) ? (char)0x00 : (char)0x01;
            } else {
                result = 0x01;
            }
            f.close();
        } else {
            result = 0x10;
        }
        _driveLedOff();
        return(result);
    }


    boolean _sys_extendfile(charP fn, /*unsigned*/ long fpos) {
        boolean result = true;
        SDFile f;
        /*unsigned*/ long i;
    
        _driveLedOn();
        if ((f = SD.open(fn.reset().toString(), SD.O_WRITE | SD.O_APPEND)) != null) {
            if (fpos > f.size()) {
                for (i = 0; i < f.size() - fpos; ++i) {
                    if (f.write((char)0) < 0) {
                        result = false;
                        break;
                    }
                }
            }
            f.close();
        } else {
            result = false;
        }
        _driveLedOff();
        return(result);
    }

    char _sys_writeseq(charP filename, long fpos) {
        char result = 0xff;
        SDFile f;
    
        _driveLedOn();
        if (_sys_extendfile(filename.reset(), fpos))
            f = SD.open(filename.reset().toString(), SD.O_RDWR);
        if (f != null) {
            if (f.seek(fpos)) {
                // if (f.write(mem._RamSysAddr(cpm.dmaAddr), 128))
                if (f.write(mem._RamSysAddr(cpm.dmaAddr, 128), 128) != 0)
                    result = 0x00;
            } else {
                result = 0x01;
            }
            f.close();
        } else {
            result = 0x10;
        }
        _driveLedOff();
        return(result);
    }

}