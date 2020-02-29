/**
 * Generic OutputConsole lib.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */

#define CONSOLE_MODE_SERIAL_DUMMY 1
#define CONSOLE_MODE_SERIAL_VT100 2
#define CONSOLE_MODE_TFT          4

class GenericConsole : public Print {

    private:
        uint8_t mode;
        bool serialVtMode;

        bool hasSerial() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY) || 
                                  ((mode & CONSOLE_MODE_SERIAL_VT100) == CONSOLE_MODE_SERIAL_VT100); }

        bool isSerialDummy() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY); }

        bool hasScreen() { return ((mode & CONSOLE_MODE_TFT) == CONSOLE_MODE_TFT); }

    public:

    GenericConsole(uint8_t mode) : Print() {
        this->mode = mode;
        this->serialVtMode = true;
    }   

    size_t write(uint8_t character) {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                return con_dum()->write(character);
            } else {
                return con_ser()->write(character);
            }
        }
        if ( this->hasScreen() ) {
            con_dum()->write('!');
            return con_dum()->write(character);
        }
        return con_dum()->write(character);
    }
};