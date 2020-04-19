public interface XtsJ80System {

    XtsJ80Video getVideo();

    XtsJ80Keyb getKeyb();

    XtsJ80RgbLed getLed();

    void halt();
    void reboot();

    void bell();

    void delay(long millis);

    int XtsBdosCall(int reg, int value);

    /** returns 8bit value */
    int readRAM(int addr);

    /** takes 8bit value */
    void writeRAM(int addr, int value);

}