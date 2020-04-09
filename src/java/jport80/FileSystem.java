
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


    // char*, uint8*
    protected class charP {
        char[] ptr = null;
        int ptrA = 0; // cursor 

        public charP(int len) {
            ptr = new char[len];
        }

        public charP(char[] content) {
            ptr = content;
        }

        public charP(String content) {
            ptr = content.toCharArray();
        }

        charP reset() { ptrA = 0; return this; }

        char get() { return ptr[ ptrA ]; }
        char get(int addr) { return ptr[ addr ]; }
        void set(char x) { ptr[ ptrA ] = x; }
        void set(int addr, char x) { ptr[ addr ] = x; }
        char inc(int i) { ptrA+=i; return get(); }
        char dec(int i) { ptrA-=i; return get(); }
        char inc() { return inc(1); }
        char dec() { return dec(1); }

        public String toString() {
            return new String( ptr );
        }
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

}