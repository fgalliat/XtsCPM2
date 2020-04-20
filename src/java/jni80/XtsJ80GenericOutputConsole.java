public interface XtsJ80GenericOutputConsole extends XtsJ80Device {


    void cls();
    void write(char ch);

    void br();
    void backspace();

    void bell();

    // 1 based
    void cursor(int col, int row);

    void charAttr(int attrValue);

    void eraseUntilEOL();

    XtsJ80VTExtHandler getVtExtHandler();

}