
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

    int _sys_makefile(charP filename) {
        // FIXME : TODO ...
        return -1;
    }

    char _sys_makedisk(char drive) {
        // FIXME : TODO ...
        return 0x00;
    }

    abstract void _HostnameToFCBname(charP from, charP to);
    abstract void _HostnameToFCB(int fcbaddr, charP filename);



    void _MakeUserDir() {
        char dFolder = (char) ((int)cpm.cDrive + (int)'A');
        char uFolder = DataUtils.toupper(DataUtils.tohex(cpm.userCode));
    
        charP path = new charP( new char[] { dFolder, FOLDERCHAR, uFolder, 0 } );
    
        _driveLedOn();
        SD.mkdir(path.toString());
        _driveLedOff();
    }

    char _findnext(char isdir) {
        File f;
        char result = 0xff;
        charP dirname = new charP(13);
        // uint8 dirname[13];
        boolean isfile;
    
        _driveLedOn();
        while (f = root.openNextFile()) {
            //f.getName((char*)&dirname[0], 13);
            f.getName( dirname.reset(), 13);
            isfile = !f.isDirectory();
            f.close();
            if (!isfile)
                continue;
            _HostnameToFCBname(dirname, cpm.fcbname);
            if (match(cpm.fcbname, cpm.pattern)) {
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
    
    char _findfirst(char isdir) {
        // uint8 path[4] = { '?', FOLDERCHAR, '?', 0 };
        charP path = new charP( new char[] { '?', FOLDERCHAR, '?', 0 } );

        path.set(0, cpm.filename.get(0) );
        // path[0] = filename[0];
        // path[2] = filename[2];
        path.set(2, cpm.filename.get(2) );

        if (root)
            root.close();

        root = SD.open((char *)path); // Set directory search to start from the first position

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


}