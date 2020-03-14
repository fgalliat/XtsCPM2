/**
 * Generic OutputConsole lib.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */

#include "SerialTermConsole.h"
#include "SerialDummyConsole.h"
#include "TFTConsole.h"


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

    void init() {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                con_dum_init();
            } else {
                con_ser_init();
            }
        }
        
        if ( this->hasScreen() ) {
            con_tft_init();
        }
    }

    void cls() {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                con_dum_cls();
            } else {
                con_ser_cls();
            }
        }
        if ( this->hasScreen() ) {
            con_tft_cls();
        }
    }

    void cursor(int row, int col) {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                con_dum_cursor(row, col);
            } else {
                con_ser_cursor(row, col);
            }
        }
        if ( this->hasScreen() ) {
            con_tft_cursor(row, col);
        }
    }

    void attr_accent() {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                con_dum_attr_accent();
            } else {
                con_ser_attr_accent();
            }
        }
        if ( this->hasScreen() ) {
            con_tft_attr_accent();
        }
    }

    void attr_none() {
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                con_dum_attr_none();
            } else {
                con_ser_attr_none();
            }
        }
        if ( this->hasScreen() ) {
            con_tft_attr_none();
        }
    }

    // inheritance
    size_t write(uint8_t character) {
        size_t res = 0;
        if ( this->hasSerial() ) {
            if ( this->isSerialDummy() ) {
                res = con_dum()->write(character);
            } else {
                res = con_ser()->write(character);
            }
        }
        if ( this->hasScreen() ) {
            // res = con_tft()->write(character);
            con_tft_writeOneChar(character);
            res = 1;
        }
        return res;
    }
};