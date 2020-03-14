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

bool VideoCard::drawBitmapFile(char* bmpFile, int x, int y, bool rotated) {
    // TODO
    return false;
}

bool VideoCard::drawPctFile(char* pctFile, int x, int y) {
    // TODO
    return false;
}

bool VideoCard::drawPakFile(char* pctFile, int x, int y, int numImage) {
    // TODO
    return false;
}

bool VideoCard::loadPCTSpriteBoard(int spriteBoardNum, char* filename) { return false; }
bool VideoCard::loadBMPSpriteBoard(int spriteBoardNum, char* filename) { return false; }

bool VideoCard::drawSprite(int spriteBoardNum, int xDest, int yDest, int xSrc, int ySrc, int wSrc, int hSrc, bool transparent, uint16_t transparentColor) { return false; }