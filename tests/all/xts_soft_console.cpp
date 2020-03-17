/**
 * Generic Console lib. impl.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */

#include <Arduino.h>

#include "xts_soft_console.h"
#include "xts_res_console.h"

#include "xts_dev_serial_term_console.h"
#include "xts_dev_serial_stream_console.h"
#include "xts_dev_tft_console.h"

extern void xts_handler();
#include "xts_dev_joystick.h"
extern Joystick joystick;

int ttyWidth=0,ttyHeight=0;

// private members
bool IOConsole::hasSerial() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY) || 
                            ((mode & CONSOLE_MODE_SERIAL_VT100) == CONSOLE_MODE_SERIAL_VT100); }

bool IOConsole::isSerialDummy() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY); }

bool IOConsole::hasScreen() { return ((mode & CONSOLE_MODE_TFT) == CONSOLE_MODE_TFT); }


// public members
IOConsole::IOConsole(uint8_t mode) : Print() {
    setMode( mode );
}

void IOConsole::setMode(uint8_t mode) {
    this->mode = mode;

    if ( mode == 0x00 ) {
        return;
    }

    // compute the smallest ttyBound caps.
    ttyWidth = 500;
    ttyHeight = 500;
    if ( hasSerial() ) {
        if ( isSerialDummy() ) {
            ttyWidth = min( ttyWidth, DUM_CAP_WIDTH );
            ttyHeight = min( ttyHeight, DUM_CAP_HEIGHT );
        } else {
            ttyWidth = min( ttyWidth, SER_CAP_WIDTH );
            ttyHeight = min( ttyHeight, SER_CAP_HEIGHT );
        }
    }
    if ( hasScreen() ) {
        ttyWidth = min( ttyWidth, TFT_CAP_WIDTH );
        ttyHeight = min( ttyHeight, TFT_CAP_HEIGHT );
    }

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

// 1-based
void IOConsole::gotoXY(int col, int row) {
    cursor(row,col);
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

void IOConsole::splashScreen_SD() {
    cls();
    this->print( splash_screen_SD );
}

void IOConsole::drawHline(int x1, int y1, int x2, char fill) {
    gotoXY(x1,y1);
    for(int i=x1; i <= x2; i++) {
        write(fill);
    }
}

void IOConsole::drawTextBox(int x1, int y1, int x2, int y2, bool clear) {
    drawHline(x1, y1, x2);
    drawHline(x1, y2, x2);

    for(int i=y1+1; i < y2; i++) {
        gotoXY(x1,i);
        write('|');
        if ( clear ) {
            for(int j=x1+1; j < x2; j++) {
                write(' ');
            }
        } else { 
            gotoXY(x2, i); 
        }
        write('|');
    }

}


int IOConsole::getWidth() {
    return ttyWidth;
}

int IOConsole::getHeight() {
    return ttyHeight;
}

void IOConsole::window(int x1, int y1, int x2, int y2, char* title, bool clearBehind) {
    drawTextBox( x1, y1, x2, y2, clearBehind ); // box

    drawHline( x1+1, y1+1, x2-1, ':' ); // title line
    int xTitle = x1 + ((x2-x1) / 2) - (strlen(title) / 2);
    gotoXY( xTitle, y1+1 );
    print( title );

    drawHline( x1+1, y1+2, x2-1, '~' ); // under title line
}

void IOConsole::warn(char* message) {
    char* title = (char*)" Warning ";
    int x = 5;
    int y = 5;
    int x2 = x+ (1+1+2+1+ max(strlen(title), strlen(message))+1+2+1+1);
    int y2 = y+5;

    window(x,y,x2,y2,title, true);
    attr_accent();
    gotoXY( x+1, y+3 );
    print( " !! " ); print( message ); print( " !! " );
    attr_none();

    gotoXY( 1, y2+1 );
}



// blocking method, invokes xts_handler when possible
int IOConsole::menu(char* title, char* items[], int nbItems, int x1, int y1, int x2, int y2, bool clearBehind) {
    int w = -1;
    int h = -1;

    if ( y2 < 0 ) {
        h = 4 + nbItems;
    }
    if ( x2 < 0 ) {
        w = 0;
        for(int i=0; i < nbItems; i++) {
            if ( items[i] != NULL ) {
                int t = strlen(items[i]);
                if ( t > w ) { w = t; }
            }
        }

        if ( strlen(title) > w ) {
            w = strlen(title);
        }

        w = 4 + w + 2;
    }

    if ( x1 < 0 ) {
        x1 = ( getWidth() - w ) / 2;
    }

    if ( y1 < 0 ) {
        y1 = ( getHeight() - h ) / 2;
    }

    if ( x2 < 0 ) {
        x2 = x1 + w;
    }
    if ( y2 < 0 ) {
        y2 = y1 + h;
    }



    window(x1, y1, x2, y2, title, clearBehind);

    // draws items
    int yOfItems = y1+3;
    for(int i=0; i < nbItems; i++) {
        if (!clearBehind) { drawHline( x1+1, yOfItems+i, x2-1, ' ' ); }
        if ( items[i] != NULL ) {
            gotoXY( x1+4, yOfItems+i );
            print( items[i] );
        }
    }
    gotoXY( x1+2, yOfItems );
    write( '>' );

    int selectedItem = 0; 

    while( true ) {
        xts_handler();

        if ( joystick.hasChangedState() ) {
            if ( joystick.isBtn0() || joystick.isBtn1() ) {
                // Btn #0, #1 -> OK
                while ( joystick.isBtn0() || joystick.isBtn0() ) {
                    xts_handler();
                    delay(50);
                }
                // release cursor
                gotoXY( x1, y2+1 );
                return selectedItem;
            } else if ( joystick.isBtn2() ) {
                // Btn #2 -> Esc
                while ( joystick.isBtn2() ) {
                    xts_handler();
                    delay(50);
                }
                // release cursor
                gotoXY( x1, y2+1 );
                return -1;
            } else if ( joystick.isDirUp() ) {
                while ( joystick.isDirUp() ) {
                    xts_handler();
                    delay(50);
                }
                gotoXY( x1+2, yOfItems+selectedItem ); write( ' ' );
                selectedItem--;
                if ( selectedItem < 0 ) { selectedItem = nbItems-1; }
                gotoXY( x1+2, yOfItems+selectedItem ); write( '>' );
            } else if ( joystick.isDirDown() ) {
                while ( joystick.isDirDown() ) {
                    xts_handler();
                    delay(50);
                }
                gotoXY( x1+2, yOfItems+selectedItem ); write( ' ' );
                selectedItem++;
                if ( selectedItem > nbItems-1 ) { selectedItem = 0; }
                gotoXY( x1+2, yOfItems+selectedItem ); write( '>' );
            }
        }

        delay(50);
    }

    return -1;
}

// =========== Input routines ===========
int IOConsole::kbhit() {
    int res = 0;
    if ( this->hasSerial() ) {
        if ( this->isSerialDummy() ) {
            res = con_dum()->available();
        } else {
            res = con_ser()->available();
        }
    }
    return res;
}

// blocking key read
uint8_t IOConsole::getch() {
    uint8_t res = 0;
    if ( this->hasSerial() ) {
        int avail;
        if ( this->isSerialDummy() ) {
            while( (avail = con_dum()->available()) <= 0 ) {
                ;
            }
            res = con_dum()->read();
        } else {
            while( (avail = con_dum()->available()) <= 0 ) {
                ;
            }
            res = con_ser()->read();
        }
    }
    return res;
}

// blocking key read - with echo
uint8_t IOConsole::getche() {
    uint8_t res = getch();
    write( res );
    return res;
}