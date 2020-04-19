import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

/**
 * JNI 80 - CPM Emulator GFX frontend <br/>
 * by Xtase - fgalliat @Apr2020 <br/>
 * cpp code from Xtase-fgalliat (XtsCPM/XtsCPM2) (based on MockbaTheBorg RunCPM)
 * <br/>
 */

public class JavaRunCPM_GFX extends JavaRunCPM implements XtsJ80System {

    // ========= Devices =============
    protected XtsJ80Video vid;
    protected XtsJ80Keyb keyb;
    protected XtsJ80RgbLed led;

    protected XtsJ80BdosHandler bdosHdl;
    // ===============================

    protected void initGUI() {
        JFrame frm = new JFrame("GUI JavaRunCPM (Xtase - fgalliat Apr2020)");

        JPanel mainPanel = new JPanel();
        mainPanel.add(vid);
        mainPanel.add(led);

        frm.setContentPane(mainPanel);

        frm.pack();

        frm.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                halt();
            }
        });

        frm.setVisible(true);

        // don't remember if Linux support KeyListener on whole JFrame...
        // Yes : not on the JLabel itself
        frm.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // FIXME : Handler Esc key ...
                char ch = e.getKeyChar();
                // System.out.println(ch);

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    ch = (char) 27;
                } else

                if (ch == '\n') {
                    ch = '\r';
                }

                keyb.injectChar(ch);
            }
        });

        new Thread() {
            public void run() {
                boolean inRun = true;
                while (inRun) {
                    vid.refresh();
                    Zzz(100);
                }
            }
        }.start();

    }

    // ============================================

    protected boolean firstKeybRequest = true;

    protected int _ext_kbhit() {
        int res = keyb.available();
        return res;
    }

    // blocking char read
    protected char _ext_getch() {
        if (firstKeybRequest) {
            ISR_1stKeyReq();
            firstKeybRequest = false;
        }
        while (keyb.available() <= 0) {
            Zzz(5);
        }
        char res = keyb.read();
        return res;
    }

    protected void _ext_putch(char ch) {
        vid.put_ch(ch);
    }

    protected void _ext_coninit() {
        System.out.println("J> Init the console.\n");
    }

    protected void _ext_conrelease() {
        System.out.println("J> Release the console.\n");
    }

    protected void _ext_clrscr() {
        vid.cls();
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
        return "DIR" + XtsJ80Keyb.EOL;
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
    public XtsJ80Video getVideo() {
        return vid;
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

    public JavaRunCPM_GFX() {

        // == init devices ==
        vid = new XtsJ80Video(this);
        keyb = new XtsJ80Keyb(this);

        led = new XtsJ80RgbLed(this);
        led.setup();

        bdosHdl = new XtsJ80BdosHandler(this);
        // ==================

        initGUI();

        vid.put_str("Hello World from Xtase !");
    }

    public static void main(String[] args) {
        DBUG("Starting Gfx version");
        if (!libraryLoaded) {
            System.exit(1);
        }
        JavaRunCPM_GFX emul = new JavaRunCPM_GFX();

        emul.startCPM();

        // todo : detect reboot code

        emul.halt();
    }

}