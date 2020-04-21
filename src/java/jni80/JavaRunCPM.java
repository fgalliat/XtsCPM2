public class JavaRunCPM {

    static boolean libraryLoaded = false;

    static {
        try {
            System.loadLibrary("cpm80");
            libraryLoaded = true;
            DBUG("Loaded CPM lib");
        } catch (UnsatisfiedLinkError ex) {
             ex.printStackTrace();
            DBUG("Could not load CPM lib");
        }
    }

    static void DBUG(Object o) {
        System.out.println("(ii) " + o);
    }

    // ======================================

    protected int  _ext_kbhit()        { return 0; }
    protected char _ext_getch()        { return (char)0; }
    protected void _ext_putch(char ch) { System.out.print(ch); }

    protected void _ext_coninit()    { System.out.println("J> Init the console.\n"); }
    protected void _ext_conrelease() { System.out.println("J> Release the console.\n"); }
    protected void _ext_clrscr()     { System.out.println("J> -CLS-\n"); }

    // ======================================

    public int XtsBdosCall(int reg, int value) {
        System.out.println("J> called XtsBdosCall from C++ ("+reg+", "+value+")");
        return 0xFF;
    }

    public native void startCPM();

    /** returns 8bit value */
    public native int readRAM(int addr);

    /** takes 8bit value */
    public native void writeRAM(int addr, int value);

    public void halt() {
        // FIXME : Dirty ...
        // System.exit(0);
    }

    // ======================================

    public static void main(String[] args) throws Exception {
        if ( !libraryLoaded ) {
            System.exit(1);
        }
        JavaRunCPM emul = new JavaRunCPM();

        emul.startCPM();
    }

}