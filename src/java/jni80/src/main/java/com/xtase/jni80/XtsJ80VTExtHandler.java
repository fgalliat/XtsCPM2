package com.xtase.jni80;

/**
 * 
 * Console Emulator (from VtExt to GenericOutputConsole)
 * 
 * Xtase-fgalliat @Apr2020
 */
public class XtsJ80VTExtHandler {

    protected XtsJ80GenericOutputConsole renderer;

    public XtsJ80VTExtHandler(XtsJ80GenericOutputConsole renderer) {
        this.renderer = renderer;
    }

    protected boolean inVt100Mode = false;
    protected String vt100Esc = "";

    public void put_ch(char ch) {
        if (inVt100Mode) {
            if (vt100Esc.isEmpty()) {
                if (ch == '=') {
                    // cursor control
                    vt100Esc += ch;
                    return;
                }
                inVt100Mode = false;
                vt100Esc = "";
            } else {
                if ( vt100Esc.startsWith("=") ) {
                    vt100Esc += ch;
                    if ( vt100Esc.length() >= 3 ) {
                        int y = ((int)vt100Esc.charAt(1)) - 31;
                        int x = ((int)vt100Esc.charAt(2)) - 31;
                        renderer.cursor(x, y);
                        inVt100Mode = false;
                        vt100Esc = "";
                    }
                    return;
                }
            }
        }

        if (ch == '\n') {
            return;
        } else if (ch == '\r') {
            renderer.br();
            return;
        } else if (ch == (char) 26) {
            renderer.cls();
            return;
        } else if (ch == '\b') {
            renderer.backspace();
            return;
        } else if (ch == (char) 7) {
            renderer.bell();
            return;
        } else if (ch == (char) 27) {
            // manage Esc (27)
            inVt100Mode = true;
            return;
        }

        renderer.write(ch);
    }

}