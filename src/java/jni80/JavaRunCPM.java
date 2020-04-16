public class JavaRunCPM {

    static {
        try {
            System.loadLibrary("cpm80");
        } catch (UnsatisfiedLinkError ex) {
            // ex.printStackTrace();
            DBUG("Could not load CPM lib");
        }
    }

    static void DBUG(Object o) {
        System.out.println("(ii) " + o);
    }

    public int XtsBdosCall(int reg, int value) {
        return 0xFF;
    }

    public static void main(String[] args) {
        DBUG("Hello World");

        // ...

        DBUG("GoodBye World");
    }

}