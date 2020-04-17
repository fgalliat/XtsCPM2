public class JavaRunCPM {

    static boolean libraryLoaded = false;

    static {
        try {
            System.loadLibrary("cpm80");
            libraryLoaded = true;
        } catch (UnsatisfiedLinkError ex) {
            // ex.printStackTrace();
            DBUG("Could not load CPM lib");
        }
    }

    static void DBUG(Object o) {
        System.out.println("(ii) " + o);
    }

    // ======================================

    int  _ext_kbhit()        { return 0; }
    char _ext_getch()        { return (char)0; }
    void _ext_putch(char ch) { System.out.print(ch); }

    void _ext_coninit()    { System.out.println("J> Init the console.\n"); }
    void _ext_conrelease() { System.out.println("J> Release the console.\n"); }
    void _ext_clrscr()     { System.out.println("J> -CLS-\n"); }

    // ======================================

    public int XtsBdosCall(int reg, int value) {
        System.out.println("J> called XtsBdosCall from C++ ("+reg+", "+value+")");
        return 0xFF;
    }

    public native void startCPM();

    // ======================================

    public static void main(String[] args) {
        if ( !libraryLoaded ) {
            System.exit(1);
        }
        JavaRunCPM emul = new JavaRunCPM();

        emul.startCPM();
    }

}