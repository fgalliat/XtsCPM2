
public class FileSystem {

    protected static int TRUE = DataUtils.TRUE;
    protected static int FALSE = DataUtils.FALSE;
    protected static final char FOLDERCHAR = '/';

    protected CPM cpm;

    public FileSystem(CPM cpm) {
        this.cpm = cpm;
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
        uint8 result = 0xff;
        uint8 dirname[13];
        bool isfile;
    
        _driveLedOn();
        while (f = root.openNextFile()) {
            f.getName((char*)&dirname[0], 13);
            isfile = !f.isDirectory();
            f.close();
            if (!isfile)
                continue;
            _HostnameToFCBname(dirname, fcbname);
            if (match(fcbname, pattern)) {
                if (isdir) {
                    _HostnameToFCB(dmaAddr, dirname);
                    _RamWrite(dmaAddr, 0x00);
                }
                _RamWrite(tmpFCB, filename[0] - '@');
                _HostnameToFCB(tmpFCB, dirname);
                result = 0x00;
                break;
            }
        }
        _driveLedOff();
        return(result);
    }
    
    char _findfirst(char isdir) {
        uint8 path[4] = { '?', FOLDERCHAR, '?', 0 };
        path[0] = filename[0];
        path[2] = filename[2];
        if (root)
            root.close();
        root = SD.open((char *)path); // Set directory search to start from the first position
        _HostnameToFCBname(filename, pattern);
        return(_findnext(isdir));
    }


}