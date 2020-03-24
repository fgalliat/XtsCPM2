/**
 * Generic Console lib. impl.
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */

#include <Arduino.h>

#include "xts_string.h"

#include "xts_soft_console.h"
#include "xts_res_console.h"

#include "xts_dev_serial_term_console.h"
#include "xts_dev_serial_stream_console.h"
#include "xts_dev_tft_console.h"

extern void xts_handler();
#include "xts_dev_joystick.h"
extern Joystick joystick;

#include "xts_dev_buzzer.h"
extern Buzzer buzzer;

int ttyWidth=0,ttyHeight=0;

bool IOConsole::hasSerial() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY) || 
                            ((mode & CONSOLE_MODE_SERIAL_VT100) == CONSOLE_MODE_SERIAL_VT100); }

bool IOConsole::isSerialDummy() { return ((mode & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY); }

bool IOConsole::hasScreen() { return ((mode & CONSOLE_MODE_TFT) == CONSOLE_MODE_TFT); }

bool IOConsole::hasSerialInput() { return ((modeInput & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY) || 
                            ((modeInput & CONSOLE_MODE_SERIAL_VT100) == CONSOLE_MODE_SERIAL_VT100); }

bool IOConsole::isSerialInputDummy() { return ((modeInput & CONSOLE_MODE_SERIAL_DUMMY) == CONSOLE_MODE_SERIAL_DUMMY); }




// public members
IOConsole::IOConsole(uint8_t mode, uint8_t modeInput) : Print() {
    setMode( mode, modeInput );
}

void IOConsole::setMode(uint8_t mode, uint8_t modeInput) {
    this->mode = mode;
    this->modeInput = modeInput;

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
    if ( this->hasSerial() || this->hasSerialInput() ) {
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

void IOConsole::eraseTillEOL() {
    // TODO : I think that there is a speChar or esp for SerialTerm
    if ( this->hasScreen() ) {
        con_tft_eraseTillEOL();
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

// forward symbol
size_t handleVTExtchar(IOConsole* console, uint8_t character);

// inheritance
size_t IOConsole::write(uint8_t character) {
    size_t res = 0;

    if ( handleVTExtchar(this, character) == 0 ) {
        return 1;
    }

    // just speed mode if only screen
    if ( this->mode == CONSOLE_MODE_TFT ) {
        con_tft_writeOneChar(character);
        return 1;
    }

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
        // else 
        {
            // Serial Keys handling (PS. arrows are handled inside Joystick)
            if ( hasSerial() && !isSerialDummy() ) {
                if ( kbhit() > 0 ) {
                    char ch = con_ser()->peek();
                    if ( ch == 13 ) {
                        con_ser()->read();
                        // release cursor
                        gotoXY( x1, y2+1 );
                        return selectedItem;
                    } else if ( ch == 27 ) {
                        con_ser()->read();
                        // release cursor
                        gotoXY( x1, y2+1 );
                        return -1;
                    } 
                }
            }
        }

        delay(50);
    }

    return -1;
}

// =========== Input routines ===========

const long maxTimeInput = 500L;
long lastTimeInput = 0L;

void __softInterrupt() {
    if ( millis() - lastTimeInput >= maxTimeInput ) { 
        xts_handler();
        lastTimeInput = millis();
    }
}

int IOConsole::kbhit() {
    int res = 0;
    if ( this->hasSerialInput() ) {
        if ( this->isSerialInputDummy() ) {
            res = con_dum()->available();
        } else {
            res = con_ser()->available();
        }
    }
    __softInterrupt();
    return res;
}

// blocking key read
uint8_t IOConsole::getch() {
    uint8_t res = 0;
    if ( this->hasSerialInput() ) {
        int avail;
        if ( this->isSerialInputDummy() ) {
            while( (avail = con_dum()->available()) <= 0 ) {
                __softInterrupt();
            }
            res = con_dum()->read();
        } else {
            while( (avail = con_ser()->available()) <= 0 ) {
                __softInterrupt();
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

void bell() {
    buzzer.beep(440, 300);
}

const int vt100seqLen = 16;
char vt100seq[vt100seqLen + 1];

const int vtMusicLen = 64;
char vtMusicSeq[ vtMusicLen + 1 ];

bool inVt100Seq = false;
bool inCursorVtSeq = false;
bool inAttrVtSeq = false;
bool inRegularVtSeq = false;

// assumes that vt100seq is 0x00 filled
bool addToVt100Seq(uint8_t character) {
    int tlen = strlen( vt100seq ); 
    if ( tlen >= vt100seqLen ) { return false; }
    vt100seq[ tlen+1 ] = character;
    return true;
}

// returns 0 if handled else 1
size_t handleVTExtchar( IOConsole* console, uint8_t character) {

    if ( inVt100Seq ) {

        if ( inCursorVtSeq ) { // ^=(..)
            if ( vt100seq[2] == 0x00 ) {
                vt100seq[2] = character;
                return 0;
            } else if ( vt100seq[3] == 0x00 ) {
                vt100seq[3] = character;
                // process caret pos

                uint8_t y = vt100seq[2] - 31;
                uint8_t x = vt100seq[3] - 31; // -1 again ???

                console->gotoXY(x, y);

                inCursorVtSeq = false;
                inVt100Seq = false;
                return 0;
            } 
        } else if ( inRegularVtSeq ) {
            if ( character == 'K' ) {
                // ^[K
                console->eraseTillEOL();
                inRegularVtSeq = false;
                inVt100Seq = false;
                return 0;
            } else if ( character == 'H' ) {
                if ( strlen( vt100seq ) > 2 ) {
                    // ^[<row>;<col>H -> set cursor position
                    char* expr = &vt100seq[2];
                    int sepa = indexOf( expr, ';' );
                    if ( sepa > -1 ) {
                        char rowS = str_split(expr, ';', 0);
                        char colS = str_split(expr, ';', 1);
                        int row = atoi( rowS );
                        int col = atoi( colS );
                        free( row ); 
                        free( col );
                        console->gotoXY( col, row );
                    } else {
                        // Oups ....
                    }
                } else {
                    // ^[H
                    // return to Home
                    console->gotoXY(1, 1);
                }

                inRegularVtSeq = false;
                inVt100Seq = false;
                return 0;
            } else if ( character == 'J' ) {
                addToVt100Seq(character); // can be ^J or ^2J
                console->cls();
                inRegularVtSeq = false;
                inVt100Seq = false;
                return 0;
            } else if ( (character >= '0' && character <= '9') || character == ';') {
                addToVt100Seq(character);
                return 0;
            }
        } else if ( inAttrVtSeq ) {
            // ex. ^B1 // ^C1
            if ( (character >= '0' && character <= '9')) {
                addToVt100Seq(character);
            }
            inAttrVtSeq = false;
            inVt100Seq = false;
            return 0;
        }


        if ( character == 'C' ) {
            vt100seq[1] = character;
            con_tft_attr_none();
            inAttrVtSeq = true;
            return 0;
        } else if ( character == 'B' ) {
            vt100seq[1] = character;
            con_tft_attr_accent();
            inAttrVtSeq = true;
            return 0;
        } else if ( character == '[' ) {
            // regular esc
            inRegularVtSeq = true;
            vt100seq[1] = character;
            return 0;
        } else if ( character == '=' ) {
            // cursor pos
            inCursorVtSeq = true;
            vt100seq[1] = character;
            return 0;
        } else if ( character == '$' ) {
            // vt music - until '!' or music buff overflow
            memset( vtMusicSeq, 0x00, vtMusicLen+1 );
        }

        inVt100Seq = false; // TODO : remove
        return 1;
    }


    // is generally used as '\b'+' '+'\b' so no need to render it
    if ( character == '\b' ) { 
        return 1; // let it handle by screen or SerialTerm 
    } else if ( character == 0x07 ) { 
        bell();
        return 0;
    } else if ( character == 26 ) {
        // Ctrl Z old style cls
        console->cls();
        return 0;
    } else if ( character == 27 ) {
        memset( vt100seq, 0x00, vt100seqLen+1 );
        inVt100Seq = true;
        vt100seq[0] = character;
        return 0;
    }

    return 1;
}

