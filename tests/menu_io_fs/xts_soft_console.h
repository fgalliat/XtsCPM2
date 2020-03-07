/**
 * Generic OutputConsole lib.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */


#define CONSOLE_MODE_SERIAL_DUMMY 1
#define CONSOLE_MODE_SERIAL_VT100 2
#define CONSOLE_MODE_TFT          4

class IOConsole : public Print {

    private:
        uint8_t mode;

        bool hasSerial();
        bool isSerialDummy();

        bool hasScreen();

    public:

    IOConsole(uint8_t mode);

    void setMode(uint8_t mode);

    void setup();

    void cls();

    // 1-based
    void cursor(int row, int col);

    void attr_accent();

    void attr_none();

    // inheritance
    size_t write(uint8_t character);
};