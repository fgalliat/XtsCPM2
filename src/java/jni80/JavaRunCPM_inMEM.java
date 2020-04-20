import java.util.ArrayList;
import java.util.List;

/**
 * JNI 80 - CPM Emulator ~headless(inMem) frontend <br/>
 * by Xtase - fgalliat @Apr2020 <br/>
 * cpp code from Xtase-fgalliat (XtsCPM/XtsCPM2) (based on MockbaTheBorg RunCPM)
 * <br/>
 */

public class JavaRunCPM_inMEM extends JavaRunCPM implements XtsJ80System {

    // ========= Devices =============
    protected XtsJ80GenericOutputConsole console;
    protected XtsJ80Keyb keyb;
    protected XtsJ80RgbLed led;

    protected XtsJ80BdosHandler bdosHdl;
    // ===============================

    // ============================================

    protected boolean firstKeybRequest = true;

    protected int _ext_kbhit() {
        int res = keyb.available();
        return res;
    }

    protected List<String> cmdBuffer = new ArrayList<String>();

    void ISR_AnotherKeyReq() {
        if ( cmdBuffer.isEmpty() ) { return; }
        keyb.injectString( cmdBuffer.get(0) );
        cmdBuffer.remove(0);
    }


    // blocking char read
    protected char _ext_getch() {
        if (firstKeybRequest) {
            ISR_1stKeyReq();

            cmdBuffer.add("DIR c:"+XtsJ80Keyb.EOL);

            cmdBuffer.add("c:"+XtsJ80Keyb.EOL);
            cmdBuffer.add("b:turbo"+XtsJ80Keyb.EOL);
            cmdBuffer.add("y");
            cmdBuffer.add("q");

            cmdBuffer.add("a:EXIT"+XtsJ80Keyb.EOL);

            firstKeybRequest = false;
        }

        if ( keyb.available() == 0 ) {
            ISR_AnotherKeyReq();
        }
        

        while (keyb.available() <= 0) {
            Zzz(5);
        }
        char res = keyb.read();
        return res;
    }

    protected void _ext_putch(char ch) {
        console.getVtExtHandler().put_ch(ch);
    }

    protected void _ext_coninit() {
        System.out.println("J> Init the console.\n");
    }

    protected void _ext_conrelease() {
        System.out.println("J> Release the console.\n");
    }

    protected void _ext_clrscr() {
        console.cls();
    }

    // ============================================

    public void halt(boolean kill) {
        if (!kill) {
            // FIXME : nice C++ code shutdown !
        }
        System.exit(0);
    }

    public void halt() {
        halt(false);
    }

    public void reboot() {
        System.out.println("-REBOOT- NYI !!");
    }

    public void delay(long millis) {
        Zzz(millis);
    }

    public void bell() {
        System.out.println("!! BELL !!");
    }

    // .....

    protected void ISR_1stKeyReq() {
        System.out.println("Got my 1st Keyb REQUEST !!!!");

        String autorun = AUTORUN();
        if (autorun != null) {
            keyb.injectString(autorun);
        }
    }

    // the autorun sequence
    protected String AUTORUN() {
        return "DIR"+XtsJ80Keyb.EOL;
    }

    @Override
    public int XtsBdosCall(int reg, int value) {
        return bdosHdl.XtsBdosCall(reg, value);
    }

    // ============================================

    @Override
    public XtsJ80Keyb getKeyb() {
        return keyb;
    }

    @Override
    public XtsJ80GenericOutputConsole getConsole() {
        return console;
    }

    @Override
    public XtsJ80RgbLed getLed() {
        return led;
    }

    // ============================================

    public static void Zzz(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
        }
    }

    public JavaRunCPM_inMEM() {

        // == init devices ==
        console = new XtsJ80TextOnlyOutputConsole(this);
        keyb = new XtsJ80Keyb(this);

        led = new XtsJ80RgbLed(this);
        led.setup();

        bdosHdl = new XtsJ80BdosHandler(this);
        // ==================
    }

    public static void main(String[] args) {
        DBUG("Starting inMem version");
        if (!libraryLoaded) {
            System.exit(1);
        }
        JavaRunCPM emul = new JavaRunCPM_inMEM();

        emul.startCPM();

        // todo : detect reboot code

        emul.halt();
    }

}