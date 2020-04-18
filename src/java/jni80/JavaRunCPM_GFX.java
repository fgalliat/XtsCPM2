import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JavaRunCPM_GFX extends JavaRunCPM {

    protected static final int zoom = 2;

    protected static final int SCREEN_WIDTH = 480 * zoom;
    protected static final int SCREEN_HEIGHT = 320 * zoom;

    protected static final int FONT_HEIGHT = 8 * zoom;
    protected static final int FONT_WIDTH = 6 * zoom;

    protected static final int TTY_COLS = 80;
    protected static final int TTY_ROWS = 40;

    // ========= Devices =============
    protected Video vid = new Video();
    protected XtsJ80Keyb keyb = new XtsJ80Keyb();
    // ===============================

    protected class Video extends JLabel {
        protected Font monospaced;

        protected char[][] tty = new char[TTY_ROWS][TTY_COLS];
        protected int ttyCursorX = 0;
        protected int ttyCursorY = 0;
        protected boolean ttyDirty = false;
        protected boolean bufDirty = false;

        public Video() {
            super("");
            setOpaque(true);
            setBackground(Color.BLACK);
            setForeground(Color.BLUE);
            setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            monospaced = new Font("Monospaced", Font.BOLD, 8 * zoom);
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.BLUE);
            g.setFont(monospaced);

            // g.drawString("= XtsJ80 =", 10, FONT_HEIGHT+10);

            // TODO : use a dblBuff

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            if (true || ttyDirty) {
                g.setColor(Color.BLUE);
                for (int y = 0; y < TTY_ROWS; y++) {
                    // for(int x=0; x < TTY_COLS; x++) {
                    // char ch = tty[y][x];
                    // if ( ch != 0 ) {
                    // g.drawChars(data, offset, length, x, y);
                    // }
                    // }
                    // FIXME : escapes char ....
                    g.drawChars(tty[y], 0, TTY_COLS, 0, FONT_HEIGHT + (y * FONT_HEIGHT));
                }
                ttyDirty = false;
            }

        }

        public void cls() {
            for (int y = 0; y < TTY_ROWS; y++) {
                for (int x = 0; x < TTY_COLS; x++) {
                    tty[y][x] = 0x00;
                }
            }
            // dblBuff.erase
            ttyDirty = true;
            // ttyDirty = false; // to speed up
            bufDirty = true;
        }

        protected void _scrollUp() {
            for (int i = 1; i < TTY_ROWS; i++) {
                tty[i - 1] = tty[i];
            }
            tty[TTY_ROWS - 1] = new char[TTY_COLS];
            ttyDirty = true;
        }

        protected void _br() {
            ttyCursorX = 0;
            ttyCursorY++;
            if (ttyCursorY >= TTY_ROWS) {
                _scrollUp();
            }
            ttyDirty = true;
        }

        public void put_ch(char ch) {

            if (ch == '\r') {
                _br();
                return;
            }

            tty[ttyCursorY][ttyCursorX] = ch;
            ttyCursorX++;
            if (ttyCursorX >= TTY_COLS) {
                _br();
            }
            ttyDirty = true;
        }

        public void put_str(String str) {
            for (int i = 0; i < str.length(); i++) {
                put_ch(str.charAt(i));
            }
            ttyDirty = true;
        }

        public void refresh() {
            repaint();
            ttyDirty = false;
        }

    }

    protected void initGUI() {
        JFrame frm = new JFrame("GUI JavaRunCPM");

        JPanel mainPanel = new JPanel();
        mainPanel.add(vid);

        frm.setContentPane(mainPanel);

        frm.pack();

        frm.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // TODO : really better
                System.exit(0);
            }
        });

        frm.setVisible(true);

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

    protected void ISR_1stKeyReq() {
        System.out.println("Got my 1st Keyb REQUEST !!!!");

        keyb.injectString("DIR\r");

        firstKeybRequest = false;
    }

    protected int _ext_kbhit() {
        int res = keyb.available();
        return res;
    }

    // blocking char read
    protected char _ext_getch() {
        if (firstKeybRequest) {
            ISR_1stKeyReq();
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

    public static void Zzz(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
        }
    }

    public JavaRunCPM_GFX() {
        initGUI();

        vid.put_str("Hello World");
    }

    public static void main(String[] args) {
        DBUG("Starting Gfx version");
        if (!libraryLoaded) {
            System.exit(1);
        }
        JavaRunCPM_GFX emul = new JavaRunCPM_GFX();

        emul.startCPM();
    }

}