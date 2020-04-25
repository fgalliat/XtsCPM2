package com.xtase.jni80;

/**
 * 
 * GFX(Swing) Output Console renderer
 * 
 * Xtase-fgalliat @Apr2020
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JLabel;

public class XtsJ80Video extends JLabel implements XtsJ80GenericOutputConsole {

    protected static final int zoom = 2;

    protected static final int SCREEN_WIDTH = 480;
    protected static final int SCREEN_HEIGHT = 320;

    protected static final int zoomed_SCREEN_WIDTH = SCREEN_WIDTH * zoom;
    protected static final int zoomed_SCREEN_HEIGHT = SCREEN_HEIGHT * zoom;

    protected static final int FONT_HEIGHT = 8;
    protected static final int FONT_WIDTH = 6;

    protected static final int TTY_COLS = 80;
    protected static final int TTY_ROWS = 40;

    protected XtsJ80System system;
    protected XtsJ80VTExtHandler consoleEmulator;

    protected Font monospaced;

    protected char[][] tty = new char[TTY_ROWS][TTY_COLS];
    protected char[][] ttyAttrs = new char[TTY_ROWS][TTY_COLS];

    protected int ttyCursorX = 0;
    protected int ttyCursorY = 0;
    protected boolean ttyDirty = false;
    protected boolean bufDirty = false;

    protected Graphics dblBuff = null;
    protected Image dblBuffSupport = null;

    public XtsJ80Video(XtsJ80System system) {
        super("");
        this.system = system;

        consoleEmulator = new XtsJ80VTExtHandler(this);

        setOpaque(true);
        setBackground(Color.BLACK);
        setForeground(Color.BLUE);
        setPreferredSize(new Dimension(zoomed_SCREEN_WIDTH, zoomed_SCREEN_HEIGHT));
        monospaced = new Font("Monospaced", Font.PLAIN, FONT_HEIGHT);

    }

    // ==========================
    @Override
    public void reset() {
        cls();
    }

    @Override
    public void setup() {
        cls();
    }

    // ==========================
    protected Color BGCOLOR = Color.BLACK;
    protected Color FGCOLOR = Color.BLUE;
    protected Color ACCCOLOR = Color.PINK;

    protected boolean dblbuffReady() {
        return dblBuff != null;
    }

    protected void buffCls() {
        dblBuff.setColor(BGCOLOR);
        dblBuff.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void paint(Graphics g) {
        if (dblBuffSupport == null) {
            dblBuffSupport = createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
            dblBuff = dblBuffSupport.getGraphics();
            dblBuff.setFont(monospaced);

            buffCls();
            render(); // for the 1st time
        }

        g.setColor(BGCOLOR);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g.drawImage(dblBuffSupport, 0, 0, zoomed_SCREEN_WIDTH, zoomed_SCREEN_HEIGHT, null);
    }

    protected char[] _chs = { 0x00 };

    protected void drawOneChar(int x, int y, char ch, int attr) {
        if (ch == ' ') {
            dblBuff.setColor(BGCOLOR);
            dblBuff.fillRect(x * FONT_WIDTH, (y * FONT_HEIGHT), FONT_WIDTH, FONT_HEIGHT);
        }
        if (attr == 0x00) {
            dblBuff.setColor(FGCOLOR);
        } else if (attr == 0x01) {
            dblBuff.setColor(ACCCOLOR);
        } else {
            dblBuff.setColor(Color.RED);
        }
        _chs[0] = ch;
        dblBuff.drawChars(_chs, 0, 1, x * FONT_WIDTH, FONT_HEIGHT + (y * FONT_HEIGHT));
    }

    public void render() {
        buffCls();
        for (int y = 0; y < TTY_ROWS; y++) {
            for (int x = 0; x < TTY_COLS; x++) {
                char ch = tty[y][x];
                if (ch != 0) {
                    drawOneChar(x, y, ch, ttyAttrs[y][x]);
                }
            }
        }
    }

    public void cls() {
        for (int y = 0; y < TTY_ROWS; y++) {
            for (int x = 0; x < TTY_COLS; x++) {
                tty[y][x] = 0x00;
            }
        }
        ttyCursorY = 0;
        ttyCursorX = 0;

        ttyDirty = true;
        // ttyDirty = false; // to speed up
        bufDirty = true;

        if (dblbuffReady()) {
            buffCls();
        }

    }

    protected void _scrollUp() {
        for (int i = 1; i < TTY_ROWS; i++) {
            tty[i - 1] = tty[i];
        }
        tty[TTY_ROWS - 1] = new char[TTY_COLS];
        ttyCursorY = TTY_ROWS - 1;
        ttyDirty = true;

        render();
    }

    protected void _br() {
        ttyCursorX = 0;
        ttyCursorY++;
        if (ttyCursorY >= TTY_ROWS) {
            _scrollUp();
        }
        ttyDirty = true;
    }

    // =======================
    @Override
    public XtsJ80VTExtHandler getVtExtHandler() {
        return consoleEmulator;
    }

    @Override
    public void br() {
        _br();
    }

    @Override
    public void bell() {
        system.bell();
    }

    @Override
    public void backspace() {
        ttyCursorX--;
        if (ttyCursorX < 0) {
            ttyCursorX = TTY_COLS - 1;
            ttyCursorY--;
            if (ttyCursorY < 0) {
                ttyCursorY = 0;
                ttyCursorX = 0;
            }
        }
    }

    @Override
    public void write(char ch) {
        tty[ttyCursorY][ttyCursorX] = ch;

        if (dblbuffReady()) {
            drawOneChar(ttyCursorX, ttyCursorY, ch, ttyAttrs[ttyCursorY][ttyCursorX]);
        }

        ttyCursorX++;
        if (ttyCursorX >= TTY_COLS) {
            _br();
        }
        ttyDirty = true;
    }

    @Override
    public void cursor(int col, int row) {
        ttyCursorX = col - 1;
        if (ttyCursorX < 0) {
            ttyCursorX = 0;
        }
        if (ttyCursorX >= TTY_COLS) {
            ttyCursorX = TTY_COLS - 1;
        }
        ttyCursorY = row - 1;
        if (ttyCursorY < 0) {
            ttyCursorY = 0;
        }
        if (ttyCursorY >= TTY_ROWS) {
            ttyCursorY = TTY_ROWS - 1;
        }
    }

    @Override
    public void charAttr(int attrValue) {
        ttyAttrs[ttyCursorY][ttyCursorX] = (char) attrValue;
    }

    @Override
    public void eraseUntilEOL() {
        for (int i = 0; i < TTY_COLS; i++) {
            tty[ttyCursorY][i] = 0x00;
        }
        ttyDirty = true;
    }

    // =======================

    public Color mapColor(int colorNum) {
        if ( colorNum >= 16 ) {
            System.out.println("(!!) Color 565("+ colorNum +")");
            return Color.PINK;
        }

        if ( colorNum == 0 ) { return Color.BLACK; }
        if ( colorNum == 1 ) { return Color.WHITE; }
        if ( colorNum == 2 ) { return Color.RED; }
        if ( colorNum == 3 ) { return Color.GREEN; }
        if ( colorNum == 4 ) { return Color.BLUE; }
        if ( colorNum == 5 ) { return Color.YELLOW; }
        //if ( colorNum == 6 ) { return Color.PURPLE; }
        if ( colorNum == 6 ) { return Color.PINK; }
        if ( colorNum == 7 ) { return Color.CYAN; }
        if ( colorNum == 8 ) { return Color.ORANGE; }
        if ( colorNum == 9 ) { return Color.MAGENTA; }
        return Color.PINK;
    }

    public void drawLine(int x1, int y1, int x2, int y2, int colorNum) {
        dblBuff.setColor( mapColor(colorNum) );
        dblBuff.drawLine(x1, y1, x2, y2);
    }

    public void drawRect(int x1, int y1, int w, int h, int colorNum) {
        dblBuff.setColor( mapColor(colorNum) );
        dblBuff.drawRect(x1, y1, w, h);
    }

    public void fillRect(int x1, int y1, int w, int h, int colorNum) {
        dblBuff.setColor( mapColor(colorNum) );
        dblBuff.fillRect(x1, y1, w, h);
    }

    // =======================

    public synchronized void put_ch(char ch) {
        consoleEmulator.put_ch(ch);
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