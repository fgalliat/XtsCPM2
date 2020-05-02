package com.xtase.jni80;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseListener;

/**
 * JNI 80 - CPM Emulator GFX frontend <br/>
 * by Xtase - fgalliat @Apr2020 <br/>
 * cpp code from Xtase-fgalliat (XtsCPM/XtsCPM2) (based on MockbaTheBorg RunCPM)
 * <br/>
 */

public class JavaRunCPM_GFX extends JavaRunCPM implements XtsJ80System {

    // ========= Devices =============
    // protected XtsJ80Video vid;
    protected XtsJ80GenericOutputConsole console;
    protected XtsJ80Keyb keyb;
    protected XtsJ80RgbLed led;

    protected XtsJ80BdosHandler bdosHdl;

    protected XtsJ80FileSystem fs = new XtsJ80FileSystem();
    protected XtsJ80MP3Player musicPlayer = new XtsJ80MP3Player();

    protected XtsJ80Joypad joypad;

    @Override
    public XtsJ80FileSystem getFs() {
        return fs;
    }

    @Override
    public XtsJ80MP3Player getMusicPlayer() {
        return musicPlayer;
    }

    // ===============================

    protected boolean inRun = false;
    protected JFrame frm = null;
    protected boolean forceExit = true;

    public void setDefaultClosingMode(boolean forceExit) {
        this.forceExit = forceExit;
    }

    protected void initGUI() {
        frm = new JFrame("JNI80 JavaRunCPM (Xtase - fgalliat Apr2020)");

        JPanel mainPanel = new JPanel();

        mainPanel.add(joypad.getLeftPanel()); // PAD

        mainPanel.add((XtsJ80Video) console);
        mainPanel.add(led);

        mainPanel.add(joypad.getRightPanel()); // PAD

        frm.setContentPane(mainPanel);

        frm.pack();

        frm.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                halt();
            }

            public void windowOpened(WindowEvent e) {
                // ready();
            }
        });

        frm.setVisible(true);

        // don't remember if Linux support KeyListener on whole JFrame...
        // Yes : not on the JLabel itself
        frm.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                char ch = e.getKeyChar();

                // if ( e.getModifiers() != 0 ) {
                // System.out.println( "Shift:"+ ((int)ch) );
                // }
                if (ch == 65535) {
                    // just SHIFT/ALT/CTRL PRESSED
                    return;
                }

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
                inRun = true;
                while (inRun) {
                    ((XtsJ80Video) console).refresh();
                    Zzz(100);

                    if (!readyFlag && ((XtsJ80Video) console).dblbuffReady()) {
                        ready();
                    }
                }
                // System.out.println("Exit redraw Thread.");
            }
        }.start();

        while (!readyFlag) {
            Zzz(100);
        }

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

        // if ( keyb.available() == 0 ) {
        // // System.out.println("GET_CH request");
        // }

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
        // System.out.println("J> Init the console.\n");
    }

    protected void _ext_conrelease() {
        // System.out.println("J> Release the console.\n");
    }

    protected void _ext_clrscr() {
        console.cls();
    }

    // ============================================

    public void halt(boolean kill) {
        inRun = false;
        frm.setVisible(false);
        // if (!kill) {
        // // FIXME : nice C++ code shutdown !
        // return;
        // }

        if (kill || forceExit) {
            System.exit(0);
        }
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
        // System.out.println("Got my 1st Keyb REQUEST !!!!");

        String autorun = AUTORUN();
        if (autorun != null) {
            keyb.injectString(autorun);
        }
    }

    protected String autorunSeq = null;

    public void setAUTORUN(String seq) {
        this.autorunSeq = seq;
    }

    // the autorun sequence
    protected String AUTORUN() {
        return this.autorunSeq;
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

    // @Override
    // public XtsJ80Video getVideo() {
    // return (XtsJ80Video)console;
    // }

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

    public JavaRunCPM_GFX() {

        // == init devices ==
        console = new XtsJ80Video(this);
        keyb = new XtsJ80Keyb(this);

        led = new XtsJ80RgbLed(this);
        led.setup();

        bdosHdl = new XtsJ80BdosHandler(this);

        joypad = new XtsJ80Joypad(this);
        joypad.setup();
        // ==================

        initGUI();

        // vid.put_str("Hello World from Xtase !");
    }

    boolean readyFlag = false;

    public void ready() {
        // ((XtsJ80Video) console).requestFocus();
        frm.requestFocus(); // in order to make keyListener
        ((XtsJ80Video) console).addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                frm.requestFocus();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                frm.requestFocus();
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
        });

        readyFlag = true;
        System.out.println("GUI ready");
    }

    public void runCPM(String autorun, boolean forceKill) throws IOException {
        if (!libraryLoaded) {
            throw new IOException("Could not load native library (jni80)");
        }

        this.setDefaultClosingMode(forceKill);
        this.setAUTORUN(autorun);

        this.startCPM();

        // todo : detect reboot code

        this.halt();
        DBUG("CPM Halted");
    }

    public static void main(String[] args) throws Exception {
        DBUG("Starting Gfx version");
        if (!libraryLoaded) {
            System.exit(1);
        }
        JavaRunCPM_GFX emul = new JavaRunCPM_GFX();

        emul.runCPM("DIR" + XtsJ80Keyb.EOL, true);

        DBUG("Ending Gfx version");
    }

}