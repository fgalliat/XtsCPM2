/**
 * Generic Console lib. impl.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */

#include <Arduino.h>

#include "xts_soft_console.h"

#include "xts_dev_serial_term_console.h"
#include "xts_dev_serial_stream_console.h"
#include "xts_dev_tft_console.h"


// private members
bool IOConsole::hasSerial() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY) || 
                            ((mode & CONSOLE_MODE_SERIAL_VT100) == CONSOLE_MODE_SERIAL_VT100); }

bool IOConsole::isSerialDummy() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY); }

bool IOConsole::hasScreen() { return ((mode & CONSOLE_MODE_TFT) == CONSOLE_MODE_TFT); }


// public members
IOConsole::IOConsole(uint8_t mode) : Print() {
    this->mode = mode;
}

void IOConsole::setMode(uint8_t mode) {
    this->mode = mode;
}

void IOConsole::setup() {
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

void IOConsole::cls() {
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

// 1-based
void IOConsole::cursor(int row, int col) {
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

void IOConsole::attr_accent() {
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

void IOConsole::attr_none() {
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
size_t IOConsole::write(uint8_t character) {
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