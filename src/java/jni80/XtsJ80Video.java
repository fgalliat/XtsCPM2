import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

import java.awt.Font;
import java.awt.Dimension;

public class XtsJ80Video extends JLabel {

    protected static final int zoom = 2;

    protected static final int SCREEN_WIDTH = 480 * zoom;
    protected static final int SCREEN_HEIGHT = 320 * zoom;

    protected static final int FONT_HEIGHT = 8 * zoom;
    protected static final int FONT_WIDTH = 6 * zoom;

    protected static final int TTY_COLS = 80;
    protected static final int TTY_ROWS = 40;

    protected XtsJ80System system;

    protected Font monospaced;

    protected char[][] tty = new char[TTY_ROWS][TTY_COLS];
    protected int ttyCursorX = 0;
    protected int ttyCursorY = 0;
    protected boolean ttyDirty = false;
    protected boolean bufDirty = false;

    public XtsJ80Video(XtsJ80System system) {
        super("");
        this.system = system;

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

        ttyCursorY = 0;
        ttyCursorX = 0;
    }

    protected void _scrollUp() {
        for (int i = 1; i < TTY_ROWS; i++) {
            tty[i - 1] = tty[i];
        }
        tty[TTY_ROWS - 1] = new char[TTY_COLS];
        ttyCursorY = TTY_ROWS - 1;
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

    public synchronized void put_ch(char ch) {

        if (ch == '\r') {
            _br();
            return;
        } else if (ch == (char) 26) {
            cls();
            return;
        } else if (ch == '\b') {
            ttyCursorX--;
            if (ttyCursorX < 0) {
                ttyCursorX = TTY_COLS - 1;
                ttyCursorY--;
                if (ttyCursorY < 0) {
                    ttyCursorY = 0;
                    ttyCursorX = 0;
                }
            }
            return;
        } else if ( ch == (char)7 ) {
            system.bell();
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