import java.io.InputStream;

public class Console {

    // Beware : LF blocking by default
    InputStream in = System.in; 

    public void _putcon(char c) {
        System.out.print(c);
    }

    void _puts(String str) {
        System.out.print(str);
    }

    void _puthex8(char ch) {
        System.out.print(Integer.toHexString(ch));
    }

    // wait a char w/o echo
    public char _getch() {
        try {
            int ch = in.read();
            // don't display it
            return (char)ch;
        } catch(Exception ex) {
            return 0x00;
        }
    }

    // FIXME : not certified
    public char _getchNB() {
        char ch = _getch();
        return (char)((int)ch-(int)'0');
    }

    // wait a char w/ echo
    public char _getche() {
        char ch = _getch();
        // display it
        return ch;
    }

    // FIXME : not certified
    public int _chready() {
        try {
            return in.available();
        } catch(Exception ex) {
            return 0;
        }
    }




}