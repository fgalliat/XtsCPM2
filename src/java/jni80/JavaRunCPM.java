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
        return 0xFF;
    }

    public void test_XtsBdosCall(int reg, int value) {
        System.out.println("called from C++ ("+reg+", "+value+")");
    }

    public native void startCPM();

    // ======================================

    public static void main(String[] args) {
        if ( !libraryLoaded ) {
            System.exit(1);
        }
        DBUG("Hello World");

        JavaRunCPM emul = new JavaRunCPM();

        emul.startCPM();

        DBUG("GoodBye World");
    }

}