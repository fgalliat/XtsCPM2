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

    public int XtsBdosCall(int reg, int value) {
        System.out.println("called XtsBdosCall from C++ ("+reg+", "+value+")");
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