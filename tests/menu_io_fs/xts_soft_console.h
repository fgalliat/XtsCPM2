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

    int getWidth();
    int getHeight();

    // 1-based
    void cursor(int row, int col);
    // 1-based
    void gotoXY(int col, int row);

    void attr_accent();

    void attr_none();

    void splashScreen_SD();

    // coords : 1 based
    void drawTextBox(int x1, int y1, int x2, int y2, bool clear=true);
    void drawHline(int x1, int y1, int x2, char fill='-');

    void window(int x1, int y1, int x2, int y2, char* title, bool clearBehind=true);
    int menu(char* title, char* items[], int nbItems, int x1=-1, int y1=-1, int x2=-1, int y2=-1, bool clearBehind=true);

    void warn(char* message);

    // inheritance
    size_t write(uint8_t character);
};