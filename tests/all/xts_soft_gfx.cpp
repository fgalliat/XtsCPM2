#include "top.h"

/**
 * Screen software (gfx) routines
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

#include "Arduino.h"

#include "xts_dev_gfx.h"

#include "xts_dev_fs.h"
extern Fs fileSystem;

#include "xts_soft_console.h"
extern IOConsole console;

#include "xts_dev_rgbled.h"
extern RGBLed led;

bool VideoCard::drawBitmapFile(char* bmpFile, int x, int y, bool rotated) {
    // TODO
    return false;
}

bool VideoCard::drawPctFile(char* pctFile, int x, int y) {
    // TODO
    return false;
}

// try to remove that platform dependent deps
#include "SdFat.h"
extern FS_CLASS SD;

bool VideoCard::drawPakFile(char* filename, int x, int y, int numInPak) {
    if ( filename == NULL || strlen(filename) <= 0 || strlen(filename) >= 32 ) {
        console.warn((char*)"Wrong PAK filename !");
        return false;
    }

    File pakFile;

    led.drive_led(true);
    if (!(pakFile = SD.open(filename))) {
        console.warn((char*)"PAK File not found");
        led.drive_led(false);
        return false;
    }

    uint16_t w = ( pakFile.read() * 256 ) + pakFile.read();
    uint16_t h = ( pakFile.read() * 256 ) + pakFile.read();
    uint8_t nbImgs = pakFile.read();

    if ( x < 0 ) { x = (getScreenWidth()-w)/2; }
    if ( y < 0 ) { y = (getScreenHeight()-h)/2; }

    if ( numInPak < 0 ) { numInPak=0; }
    if ( numInPak > nbImgs ) { numInPak=nbImgs-1; }

    pakFile.seek( numInPak * ( w*h*2 ) ); // beware : seems to be relative ? 
    // uint16_t scanLine[w];

    #define SCAN_ARRAY_HEIGHT 8
    uint16_t scanArray[w*SCAN_ARRAY_HEIGHT]; // 32KB bytes // 1KB for 2 rows

    for(int yy=0; yy < h; yy+=SCAN_ARRAY_HEIGHT) {

        int ct = pakFile.read( (uint8_t*)scanArray, SCAN_ARRAY_HEIGHT * w*2 ); // *2 cf U16
        if ( ct <= 0 ) { console.warn((char*)"Oups EOF !"); break; }

        int usedHeight = SCAN_ARRAY_HEIGHT;
        usedHeight = ct / 2 / w;
        // tft.pushRect(x, yy+y, w, usedHeight, scanArray);
        // tft.drawRGBBitmap(x, yy+y, scanArray, w, usedHeight);
        fillRect(x, yy+y, w, usedHeight, scanArray);

    } // for yy


    pakFile.close();
    led.drive_led(false);

    return true;
}

bool VideoCard::loadPCTSpriteBoard(int spriteBoardNum, char* filename) { return false; }
bool VideoCard::loadBMPSpriteBoard(int spriteBoardNum, char* filename) { return false; }

bool VideoCard::drawSprite(int spriteBoardNum, int xDest, int yDest, int xSrc, int ySrc, int wSrc, int hSrc, bool transparent, uint16_t transparentColor) { return false; }