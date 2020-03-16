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

// bool VideoCard::loadPCTSpriteBoard(int spriteBoardNum, char* filename) { return false; }
// bool VideoCard::loadBMPSpriteBoard(int spriteBoardNum, char* filename) { return false; }

// bool VideoCard::drawSprite(int spriteBoardNum, int xDest, int yDest, int xSrc, int ySrc, int wSrc, int hSrc, bool transparent, uint16_t transparentColor) { return false; }



// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
// @@ Sprites
// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


  #define SPRITES_SUPPORT 1
  // #define SPRITES_SUPPORT 0

  #if SPRITES_SUPPORT
    #define SPRITE_AREA_WIDTH 160
    #define SPRITE_AREA_HEIGHT 120
    #define SPRITE_AREA_SIZE (SPRITE_AREA_WIDTH*SPRITE_AREA_HEIGHT)
    
    int spriteInstanceCounter=0;

    class Sprite {
       private:
         int idx;
         int addr;
       public:
         int x, y, w, h;

         Sprite() {
            this->invalid();
            this->idx = spriteInstanceCounter++;
            this->addr = -1;
         }
         ~Sprite() {}
         void setBounds(int x, int y, int w, int h) {
            this->x = x; this->y = y;
            this->w = w; this->h = h;
         }
         bool isValid() {
            return this->x > -1 && this->y > -1; 
         }
         void invalid() {
            this->x = -1;
            this->y = -1;
            this->addr = -1;
         }
         
         void drawClip(int x, int y);
    };

    #define NB_SPRITES 15

   extern VideoCard screen;
   // uint16_t color565(uint8_t r,uint8_t g,uint8_t b) { return (uint16_t)( (( r *31/255 )<<11) | (( g *63/255 )<<5) | ( b *31/255 ) );}

   uint16_t spriteArea[ SPRITE_AREA_SIZE ];
   Sprite sprites[NB_SPRITES];

   int lastAddr = 0;

   void Sprite::drawClip(int x, int y) {
      if ( w < 0 || h < 0 ) { return; }
      if ( !isValid() ) { return; }

      uint16_t row[ w ];
      for(int i=0; i < h; i++) {
         if ( i+y >= screen.getScreenHeight() ) { break; }
         // *2 cf uint16_t
         memcpy( &row[0], &spriteArea[ ( (this->y+i) * SPRITE_AREA_WIDTH )+this->x ], w*2 );
         //  tft.drawRGBBitmap(x, i+y, row, w, 1);
         screen.fillRect(x, i+y, w, 1, row);
      }
   }
   
    void cleanSprites() {
       //memset(spriteArea, 0, SPRITE_AREA_SIZE);
       for(int i=0; i < SPRITE_AREA_SIZE; i++) { spriteArea[i] = 0; }

       spriteInstanceCounter = 0; 
       lastAddr = 0;
       for(int i=0; i < NB_SPRITES; i++) {
          sprites[i].invalid();
       }
    }

    bool _feedSprites(char* filename, int x, int y);


    bool grabbSprites(char* imageName, int offsetX, int offsetY) {
       //char* fileName = Fs.getAssetsFileEntry( imageName );
       char* fileName = imageName;
       cleanSprites();
       return _feedSprites(fileName, offsetX, offsetY);
    }

    void grabbSpritesOfSize(char* imageName, int offsetX, int offsetY, int width, int height) {
      //char* fileName = yat4l_fs_getAssetsFileEntry( imageName );
      char* fileName = imageName;

       cleanSprites();
       int nbW = 160/width;
       int nbH = 120/height;
       int howMany = nbW * nbH;
       if ( howMany > NB_SPRITES ) { howMany = NB_SPRITES; }
       int cpt = 0;
       for(int y=0; y < nbH; y++) {
         for(int x=0; x < nbW; x++) {
            sprites[cpt].x = x*width;
            sprites[cpt].y = y*height;
            sprites[cpt].w = width;
            sprites[cpt].h = height;
            cpt++;
            if ( cpt >= howMany ) { break; }
         }
       }
       _feedSprites(fileName, offsetX, offsetY);
    }

    #define BUFFPIXEL 80

   // will takes only 160x120 px of bmp file
   bool _feedSprites(char* filename, int x, int y) {
      if ( filename == NULL || strlen(filename) <= 0 || strlen(filename) >= 32 ) {
         console.warn( (char*) "Wrong BMP filename !");
         return false;
      }
 
      File bmpFile;
      int bmpWidth, bmpHeight;             // W+H in pixels
      uint8_t bmpDepth;                    // Bit depth (currently must be 24)
      uint32_t bmpImageoffset;             // Start of image data in file
      uint32_t rowSize;                    // Not always = bmpWidth; may have padding
      uint8_t sdbuffer[3 * BUFFPIXEL];     // pixel buffer (R+G+B per pixel)
      uint16_t buffidx = sizeof(sdbuffer); // Current position in sdbuffer
      boolean goodBmp = false;             // Set to true on valid header parse
      boolean flip = true;                 // BMP is stored bottom-to-top
      int w, h, row, col;
      uint8_t r, g, b;
      uint32_t pos = 0;//, startTime = millis();


      uint16_t awColors[320]; // hold colors for one row at a time...
      // uint16_t awColors[160]; // hold colors for one row at a time...
      for(int i=0; i < 320; i++) { awColors[i] = (uint16_t)0; }

      // if ((x >= tft.width()) || (y >= tft.height()))
      //    return;

      // Open requested file on SD card
      if (!(bmpFile = SD.open(filename)))
      {
         console.warn( (char*) "BMP File not found");
         return false;
      }
      // Parse BMP header
      if (read16(bmpFile) == 0x4D42) { // BMP signature
         (void)read32(bmpFile);

         (void)read32(bmpFile);            // Read & ignore creator bytes
         bmpImageoffset = read32(bmpFile); // Start of image data
         (void)read32(bmpFile);

         bmpWidth = read32(bmpFile);
         bmpHeight = read32(bmpFile);
         if (read16(bmpFile) == 1)     { // # planes -- must be '1'
            bmpDepth = read16(bmpFile); // bits per pixel
            if ((bmpDepth == 24) && (read32(bmpFile) == 0)) { // 0 = uncompressed
            goodBmp = true; // Supported BMP format -- proceed!

            // BMP rows are padded (if needed) to 4-byte boundary
            rowSize = (bmpWidth * 3 + 3) & ~3;

            // If bmpHeight is negative, image is in top-down order.
            // This is not canon but has been observed in the wild.
            if (bmpHeight < 0) {
               bmpHeight = -bmpHeight;
               flip = false;
            }

            if ((x >= bmpWidth) || (y >= bmpHeight)) {
               console.warn( (char*) "Sprite OutOfBounds");
               return false;
            }

            // Crop area to be loaded
            // w = bmpWidth;
            // h = bmpHeight;
            w = SPRITE_AREA_WIDTH; h = SPRITE_AREA_HEIGHT;
            if ((x + w - 1) >= bmpWidth)
               w = bmpWidth - x;
            if ((y + h - 1) >= bmpHeight)
               h = bmpHeight - y;

            for (row = 0; row < h; row++) { // For each scanline...

               // Seek to start of scan line.  It might seem labor-
               // intensive to be doing this on every line, but this
               // method covers a lot of gritty details like cropping
               // and scanline padding.  Also, the seek only takes
               // place if the file position actually needs to change
               // (avoids a lot of cluster math in SD library).
               if (flip) // Bitmap is stored bottom-to-top order (normal BMP)
                  pos = bmpImageoffset + (bmpHeight - 1 - row) * rowSize;
               else // Bitmap is stored top-to-bottom
                  pos = bmpImageoffset + row * rowSize;
               if (bmpFile.position() != pos) { // Need seek?
                  bmpFile.seek(pos);
                  buffidx = sizeof(sdbuffer); // Force buffer reload
               }

               for (col = 0; col < w; col++) { // For each pixel...
                  // Time to read more pixel data?
                  if (buffidx >= sizeof(sdbuffer)) { // Indeed
                     bmpFile.read(sdbuffer, sizeof(sdbuffer));
                     buffidx = 0; // Set index to beginning
                  }

                  // Convert pixel from BMP to TFT format, push to display
                  b = sdbuffer[buffidx++];
                  g = sdbuffer[buffidx++];
                  r = sdbuffer[buffidx++];

                  awColors[col] = screen.color565(r, g, b);
               } // end pixel

              // *2 Cf uint16_t
              //  memcpy( &spriteArea[ (row*SPRITE_AREA_WIDTH)+col ], &awColors[x], w*2 );
              // HERE IS A BUG -- DEADLOCK
              for(int i=0; i < w; i++) {
                // fails if goes up to 120 
                if ( row >= SPRITE_AREA_HEIGHT-1  ) { break; }
                spriteArea[ ((row*SPRITE_AREA_WIDTH)+col)+i ] = awColors[x+i];
              }

            } // end scanline
            // long timeElapsed = millis() - startTime;
            } // end goodBmp
         }
      }
      bmpFile.close();
      if (!goodBmp) {
         console.warn( (char*) "BMP format not recognized.");
         return false;
      }
      return true;
   }
   #endif

// Sprites public routines
bool VideoCard::loadBMPSpriteBoard(char* filename) {
  bool ok = grabbSprites(filename, 0, 0);
  return ok;
}
bool VideoCard::defineSprite(int spriteNum, int x, int y, int w, int h) {
  if ( spriteNum < 0 || spriteNum >= NB_SPRITES ) { return false; }
  sprites[spriteNum].setBounds(x, y, w, h);
  return true;
}
bool VideoCard::drawSprite(int spriteNum, int x, int y) {
  if ( spriteNum < 0 || spriteNum >= NB_SPRITES ) { return false; }
  sprites[spriteNum].drawClip(x, y);
  return true;
}


