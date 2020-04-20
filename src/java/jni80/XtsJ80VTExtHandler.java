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


    public void put_ch(char ch) {
        if (ch == '\r') {
            renderer.br();
            return;
        } else if (ch == (char) 26) {
            renderer.cls();
            return;
        } else if (ch == '\b') {
            renderer.backspace();
            return;
        } else if ( ch == (char)7 ) {
            renderer.bell();
            return;
        }

        // manage Esc (27)

        renderer.write(ch);
    }


}