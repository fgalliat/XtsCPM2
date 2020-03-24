#include "top.h"

/**
 * Screen hardware (gfx) routines
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

#include "Arduino.h"

#include "Adafruit_ILI9486_Teensy.h"

// TODO : swap 'extern' w/ xts_dev_tft_console.h
extern Adafruit_ILI9486_Teensy tft;

#include "xts_dev_gfx.h"

VideoCard::VideoCard() {
    // ...    
}

bool VideoCard::setup() {
    // TODO : swap 'setup()' w/ xts_dev_tft_console.h
    return true;
}

void VideoCard::setRotated(bool r) {
    tft.setRotation( r ? 2 : 1 );
}

#define SCR_WIDTH 480
#define SCR_HEIGHT 320
#define CLS_COLOR BLACK

uint16_t VideoCard::mapColor(uint16_t color) {
    if ( color == 0 ) { return BLACK; }

    if ( color >= 16 ) { return color; }
    
    if ( color == 1 ) { return WHITE; }
    if ( color == 2 ) { return RED; }
    if ( color == 3 ) { return GREEN; }
    if ( color == 4 ) { return BLUE; }
    if ( color == 5 ) { return YELLOW; }
    if ( color == 6 ) { return PURPLE; }
    if ( color == 7 ) { return CYAN; }
    if ( color == 8 ) { return ORANGE; }
    if ( color == 9 ) { return MAGENTA; }

    return PINK;
    // return color;
}

uint16_t VideoCard::color565(uint8_t r, uint8_t g, uint8_t b) {
    return tft.color565(r,g,b);
}

int VideoCard::getScreenWidth() { return SCR_WIDTH; }
int VideoCard::getScreenHeight() { return SCR_HEIGHT; }


void VideoCard::drawRect(int x, int y, int w, int h, uint16_t color) {
    tft.drawRect( x, y, w, h, mapColor(color) );
}

void VideoCard::cls() { 
    // tft.fillScreen( CLS_COLOR ); 
    this->fillScreen( CLS_COLOR );
}
void VideoCard::fillScreen(uint16_t color) { 
    // tft.fillScreen( mapColor( color ) ); 
    this->fillRect(0,0,getScreenWidth(), getScreenHeight(), mapColor(color));
}


// BEWARE : hardware dependent
extern void writedata16(uint16_t c);

// BEWARE : hardware dependent
void VideoCard::fillRect(int x, int y, int w, int h, uint16_t color) {
    // tft.fillRect( x, y, w, h, mapColor(color) );
    color = mapColor(color);

    tft.setAddrWindow(x, y, x + w - 1, y + h-1);
    SPI.beginTransaction(SPISET);

    for(int yy=0; yy < h; yy++) {
        for(int xx=0; xx < w; xx++) {
            writedata16( color );    
        }
    }

    SPI.endTransaction();
}



// if scanW is < 0 :> scanW = w
// BEWARE : hardware dependent
void VideoCard::fillRect(int x, int y, int w, int h, uint16_t* colors, int scanW, int offset) {
    if ( scanW <= 0 ) { scanW = w; }

    // faster replacement for :
    // tft.drawRGBBitmap(int16_t x, int16_t y, uint16_t *bitmap, int16_t w, int16_t h)

    // (!!) that code is not fully certified
    int addr = offset;
    bool scanWLessThanW = scanW < w;

    // BEWARE : w/ memcpy() & memset() those works in u8 not u16

    tft.setAddrWindow(x, y, x + w - 1, y + h-1);
    SPI.beginTransaction(SPISET);

    for(int yy=0; yy < h; yy++) {
        
        for(int xx=0; xx < w; xx++) {
            if ( scanWLessThanW && xx >= scanW) {
                // TODO : use BLACK after debug ..
                writedata16( GREEN );    
            } else {
                writedata16( colors[addr] );
                addr++;
            }
        }
    }

    SPI.endTransaction();
}

void VideoCard::drawCircle(int x, int y, int radius, uint16_t color) {
    tft.drawCircle(x, y, radius, mapColor( color ) );
}
void VideoCard::fillCircle(int x, int y, int radius, uint16_t color) {
    tft.fillCircle(x, y, radius, mapColor( color ) );
}


void VideoCard::drawLine(int x, int y, int x2, int y2, uint16_t color) {
    tft.drawLine(x, y, x2, y2, mapColor( color ) );
}

void VideoCard::drawPixel(int x, int y, uint16_t color) {
    tft.drawPixel(x, y, mapColor( color ) );
}