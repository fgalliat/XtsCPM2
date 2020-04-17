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

    protected Video vid = new Video();

    protected class Video extends JLabel {
        protected Font monospaced;

        protected char[][] tty = new char[TTY_ROWS][TTY_COLS];
        protected int ttyCursorX = 0;
        protected int ttyCursorY = 0;


        public Video() {
            super("");
            setOpaque(true);
            setBackground(Color.BLACK);
            setForeground(Color.BLUE);
            setPreferredSize( new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT) );
            monospaced = new Font("Monospaced",Font.BOLD,8*zoom);
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.BLUE);
            g.setFont(monospaced);

            g.drawString("= XtsJ80 =", 10, FONT_HEIGHT+10);
        }

        public void put_ch(char ch) {

        }

    }


    protected void initGUI() {
        JFrame frm = new JFrame("GUI JavaRunCPM");

        JPanel mainPanel = new JPanel();
        mainPanel.add(vid);

        frm.setContentPane(mainPanel);

        frm.pack();

        frm.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // TODO : really better
                System.exit(0);
            }
        } );

        frm.setVisible(true);

        new Thread() {
            public void run() {
                boolean inRun = true;
                while( inRun ) {
                    vid.repaint();
                    Zzz(100);
                }
            }
        }.start();


    }

    public static void Zzz(long millis) {
        try { Thread.sleep(millis); }
        catch(Exception ex) {}
    }


    public JavaRunCPM_GFX() {
        initGUI();
    }



    public static void main(String[] args) {
        DBUG("Starting Gfx version");
        if ( !libraryLoaded ) {
            System.exit(1);
        }
        JavaRunCPM_GFX emul = new JavaRunCPM_GFX();

        emul.startCPM();
    }



}