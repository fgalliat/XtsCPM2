/**
 * 
 * Generic Input Console class
 * 
 * Xtase-fgalliat @Apr2020
 */


public class XtsJ80Keyb {

    public static final char EOL = '\r';

    String buffer;

    public XtsJ80Keyb(XtsJ80System system) {
        reset();
    }

    public void reset() {
        buffer = "";
    }

    public synchronized void injectChar(char ch) {
        buffer += ch;
    }

    public void injectString(String str) {
        for (int i = 0; i < str.length(); i++) {
            injectChar(str.charAt(i));
        }
    }

    public int available() {
        return buffer.length();
    }

    protected char peek() {
        char result = available() <= 0 ? 255 : buffer.charAt(0);
        return result;
    }

    public synchronized char read() {
        char result = peek();
        if (available() >= 1) {
            buffer = buffer.substring(1);
        }
        return result;
    }

}