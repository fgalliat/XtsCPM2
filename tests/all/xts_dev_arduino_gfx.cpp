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

#define SCR_WIDTH 480
#define SCR_HEIGHT 320
#define CLS_COLOR BLACK

uint16_t VideoCard::mapColor(uint16_t color) {
    // TODO : ....

    return color;
}


int VideoCard::getScreenWidth() { return SCR_WIDTH; }
int VideoCard::getScreenHeight() { return SCR_HEIGHT; }

void VideoCard::cls() { tft.fillScreen( CLS_COLOR ); }

void VideoCard::fillScreen(uint16_t color) { tft.fillScreen( mapColor( color ) ); }

void VideoCard::drawRect(int x, int y, int w, int h, uint16_t color) {
    tft.drawRect( x, y, w, h, mapColor(color) );
}
void VideoCard::fillRect(int x, int y, int w, int h, uint16_t color) {
    tft.fillRect( x, y, w, h, mapColor(color) );
}

// if scanW is < 0 -> scanW = w
void VideoCard::fillRect(int x, int y, int w, int h, uint16_t* colors, int scanW, int offset) {
    if ( scanW < 0 ) { scanW = w; }

    // TODO : finish that code
    // (!!) that code is not certified
    int addr = offset;
    uint16_t memseg[ scanW ];
    int minW = min(w, scanW);
    bool wLessThanScanW = w < wLessThanScanW;
    for(int yy=0; yy < h; yy++) {
        if ( wLessThanScanW ) { memset( memseg, 0x00, scanW ); }
        memcpy( &memseg[0], &colors[addr], minW )
        for(int xx=0; xx < w; xx++) {
            tft.pushColor(x+xx, y+yy, &memseg[xx]);
        }
        addr += scanW;
    }
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