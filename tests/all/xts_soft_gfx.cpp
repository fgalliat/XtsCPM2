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

// ==============================
// try to remove that platform dependent deps
#include "SdFat.h"
extern FS_CLASS SD;
// ==============================

#include "xts_soft_console.h"
extern IOConsole console;

#include "xts_dev_rgbled.h"
extern RGBLed led;

uint16_t read16(File &f) {
  uint16_t result;
  ((uint8_t *)&result)[0] = f.read(); // LSB
  ((uint8_t *)&result)[1] = f.read(); // MSB
  return result;
}


uint32_t read32(File &f) {
  uint32_t result;
  ((uint8_t *)&result)[0] = f.read(); // LSB
  ((uint8_t *)&result)[1] = f.read();
  ((uint8_t *)&result)[2] = f.read();
  ((uint8_t *)&result)[3] = f.read(); // MSB
  return result;
}

bool VideoCard::drawBitmapFile(char* filename, int x, int y, bool rotated) {
  if ((x >= getScreenWidth()) || (y >= getScreenHeight())) return false;

  File bmpFS;

  led.drive_led(true);
  // Open requested file on SD card
  bmpFS = SD.open(filename);

  if (!bmpFS)
  {
    console.warn((char*)"File not found");
    led.drive_led(false);
    return false;
  }

  uint32_t seekOffset;
  uint16_t w, h, row; //, col;
  uint8_t  r, g, b;

//   uint32_t startTime = millis();

  if (read16(bmpFS) == 0x4D42)
  {
    read32(bmpFS);
    read32(bmpFS);
    seekOffset = read32(bmpFS);
    read32(bmpFS);
    w = read32(bmpFS);
    h = read32(bmpFS);

    if ( rotated ) {
        // en fait : devient X
        y = getScreenWidth() - y;
    }

    if ((read16(bmpFS) == 1) && (read16(bmpFS) == 24) && (read32(bmpFS) == 0))
    {
      //   y += h - 1;

      // TFT_eSPI specific may cause WEIRD_MODE on drawPAK !!!!!
      // tft.setSwapBytes(true);

      bmpFS.seek(seekOffset);

      uint16_t padding = (4 - ((w * 3) & 3)) & 3;
      uint8_t lineBuffer[w * 3];

      if ( rotated ) { setRotated(true); }

      for (row = 0; row < h; row++) {
        bmpFS.read(lineBuffer, sizeof(lineBuffer));
        uint8_t*  bptr = lineBuffer;
        uint16_t* tptr = (uint16_t*)lineBuffer;
        // Convert 24 to 16 bit colours
        for (uint16_t col = 0; col < w; col++)
        {
          b = *bptr++;
          g = *bptr++;
          r = *bptr++;
          *tptr++ = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >> 3);
        }
        // Read any line padding
        if (padding) bmpFS.read((uint8_t*)tptr, padding);
        // Push the pixel row to screen, pushImage will crop the line if needed
        // tft.pushImage(x, y--, w, 1, (uint16_t*)lineBuffer);
        // tft.drawRGBBitmap(x, y--, (uint16_t*)lineBuffer, w, 1);
        fillRect(x, y--, w, 1, (uint16_t*)lineBuffer);
      }

      if ( rotated ) { setRotated(false); }

    //   Serial.print("Loaded in "); Serial.print(millis() - startTime);
    //   Serial.println(" ms");
    }
    else console.warn((char*)"BMP format not recognized.");
  }
  bmpFS.close();
  led.drive_led(false);
  return true;
}



bool VideoCard::drawPctFile(char* pctFile, int x, int y) {
    // TODO
    return false;
}



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
        fillRect(x, yy+y, w, usedHeight, scanArray);

    } // for yy

    pakFile.close();
    led.drive_led(false);

    return true;
}

bool VideoCard::loadPCTSpriteBoard(int spriteBoardNum, char* filename) { return false; }
bool VideoCard::loadBMPSpriteBoard(int spriteBoardNum, char* filename) { return false; }

bool VideoCard::drawSprite(int spriteBoardNum, int xDest, int yDest, int xSrc, int ySrc, int wSrc, int hSrc, bool transparent, uint16_t transparentColor) { return false; }