package com.xtase.jni80;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XtsJ80ImageDecoder {

  XtsJ80Video screen = null;
  XtsJ80Video console = null;

  XtsJ80FileSystem fs = null;
  XtsJ80RgbLed led = null;

  public XtsJ80ImageDecoder(XtsJ80Video video, XtsJ80FileSystem fs, XtsJ80RgbLed led) {
    this.screen = video;
    this.console = this.screen;
    this.fs = fs;
    this.led = led;
  }

  int read16(FileInputStream f) throws IOException {
    int result;
    int d0 = f.read(); // LSB
    int d1 = f.read(); // MSB
    result = (d1 * 256) + d0;
    return result;
  }

  int read32(FileInputStream f) throws IOException {
    int result;
    int d0 = f.read(); // LSB
    int d1 = f.read();
    int d2 = f.read();
    int d3 = f.read(); // MSB

    result = (d3 * 256 * 256 * 256) + (d2 * 256 * 256) + (d1 * 256) + d0;
    return result;
  }

  int byteToUint8(byte b) {
    int result = b;
    if (result < 0) {
      result = 256 + result;
    }
    return result;
  }

  int sizeof(byte[] bs) {
    return bs.length;
  }

  boolean drawBitmapFile(String filename, int x, int y, boolean rotated) throws IOException {
    if ((x >= screen.getScreenWidth()) || (y >= screen.getScreenHeight()))
      return false;

    // filename = "girl2.bmp";

    FileInputStream bmpFS;

    led.drive_led(true);
    // Open requested file on SD card
    File f = fs.resolveAssetPath(filename);

    if (f == null || !f.exists()) {
      System.out.println(f + " was NOT found !");
      console.warn("File not found");
      led.drive_led(false);
      return false;
    }

    // System.out.println(f+" was found !");

    bmpFS = new FileInputStream(f);

    int seekOffset;
    int w, h, row; // , col;
    int r, g, b;

    int sign = read16(bmpFS);

    // System.out.println(Integer.toHexString(sign)+" instead of 0x4D42");

    if (sign == 0x4D42) {
      read32(bmpFS);
      read32(bmpFS);
      seekOffset = read32(bmpFS);
      read32(bmpFS);
      w = read32(bmpFS);
      h = read32(bmpFS);

      if (rotated) {
        // en fait : devient X
        y = screen.getScreenWidth() - y;
        // spe
        // x = screen.getScreenHeight() - x;
      }

      if ((read16(bmpFS) == 1) && (read16(bmpFS) == 24) && (read32(bmpFS) == 0)) {
        // y += h - 1;

        // TFT_eSPI specific may cause WEIRD_MODE on drawPAK !!!!!
        // tft.setSwapBytes(true);

        // System.out.println("seekOffset="+seekOffset);
        seekOffset -= 32; // beware : spe

        // bmpFS.seek(seekOffset);
        bmpFS.skip(seekOffset);

        int padding = (4 - ((w * 3) & 3)) & 3;
        byte[] lineBuffer = new byte[w * 3];

        // should be 0 for 320x240
        // System.out.println("padding="+padding);

        if (rotated) {
          screen.setRotated(true);
        }

        for (row = 0; row < h; row++) {
          bmpFS.read(lineBuffer, 0, sizeof(lineBuffer));

          byte[] bptr = lineBuffer;
          // uint16_t* tptr = (uint16_t*)lineBuffer;
          int[] tptr = new int[lineBuffer.length];

          // Convert 24 to 16 bit colours
          int ii = 0;
          for (int col = 0; col < w; col++) {
            r = byteToUint8(bptr[ii++]);
            g = byteToUint8(bptr[ii++]);
            b = byteToUint8(bptr[ii++]);

            // rgb to color 565
            tptr[col] = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >> 3);
          }
          // Read any line padding
          byte[] gargabe = new byte[lineBuffer.length];
          if (padding != 0)
            bmpFS.read(gargabe, 0, padding);

          // Push the pixel row to screen, pushImage will crop the line if needed
          // tft.pushImage(x, y--, w, 1, (uint16_t*)lineBuffer);
          // tft.drawRGBBitmap(x, y--, (uint16_t*)lineBuffer, w, 1);

          // screen.fillRect(x, y--, w, 1, lineBuffer);
          // System.out.println("drawRaster("+x+", "+y+", "+w+", 1, raster)");
          screen.fillRect(x, y--, w, 1, tptr);

          // Zzz(10);

        }

        if (rotated) {
          screen.setRotated(false);
        }

      } else
        console.warn("BMP format not recognized.");
    }
    bmpFS.close();
    led.drive_led(false);
    return true;
  }

  boolean drawPakFile(String filename, int x, int y, int numInPak) throws IOException {
    if (filename == null || filename.length() <= 0 || filename.length() >= 32) {
      console.warn("Wrong PAK filename !");
      return false;
    }

    // File pakFile;

    led.drive_led(true);
    File f = fs.resolveAssetPath(filename);

    if (f == null) {
      console.warn("PAK File not found");
      led.drive_led(false);
      return false;
    }

    FileInputStream pakFile = new FileInputStream(f);

    int w = (pakFile.read() * 256) + pakFile.read();
    int h = (pakFile.read() * 256) + pakFile.read();
    int nbImgs = pakFile.read();

    if (x < 0) {
      x = (screen.getScreenWidth() - w) / 2;
    }
    if (y < 0) {
      y = (screen.getScreenHeight() - h) / 2;
    }

    if (numInPak < 0) {
      numInPak = 0;
    }
    if (numInPak > nbImgs) {
      numInPak = nbImgs - 1;
    }

    pakFile.skip(numInPak * (w * h * 2)); // beware : seems to be relative ?
    // uint16_t scanLine[w];

    final int SCAN_ARRAY_HEIGHT = 8;
    byte[] scanArray = new byte[w * SCAN_ARRAY_HEIGHT * 2]; // 32KB bytes // 1KB for 2 rows
    int[] _scanArray16 = new int[w * SCAN_ARRAY_HEIGHT];

    for (int yy = 0; yy < h; yy += SCAN_ARRAY_HEIGHT) {

      int ct = pakFile.read(scanArray, 0, SCAN_ARRAY_HEIGHT * w * 2); // *2 cf U16
      if (ct <= 0) {
        console.warn("Oups EOF !");
        break;
      }

      for (int i = 0; i < _scanArray16.length; i++) {
        _scanArray16[i] = (byteToUint8(scanArray[(i * 2) + 0]) << 8) + byteToUint8(scanArray[(i * 2) + 1]);
      }

      int usedHeight = SCAN_ARRAY_HEIGHT;
      usedHeight = ct / 2 / w;
      screen.fillRect(x, yy + y, w, usedHeight, _scanArray16);

    } // for yy

    pakFile.close();
    led.drive_led(false);

    return true;
  }

  void Zzz(long millis) {
    try {
      Thread.sleep(millis);
    } catch (Exception ex) {
    }
  }

  // ========== Sprites support ============

  final int SPRITE_AREA_WIDTH = 160;
  final int SPRITE_AREA_HEIGHT = 120;
  final int SPRITE_AREA_SIZE = (SPRITE_AREA_WIDTH * SPRITE_AREA_HEIGHT);

  int spriteInstanceCounter = 0;

  class Sprite {
    private int idx;
    private int addr;

    public int x, y, w, h;

    Sprite() {
      this.invalid();
      this.idx = spriteInstanceCounter++;
      this.addr = -1;
    }

    void setBounds(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    boolean isValid() {
      return this.x > -1 && this.y > -1;
    }

    void invalid() {
      this.x = -1;
      this.y = -1;
      this.addr = -1;
    }

    void drawClip(int x, int y) {
      if (w < 0 || h < 0) {
        return;
      }
      if (!isValid()) {
        return;
      }

      // uint16_t row[ w ];
      int[] row = new int[w];
      for (int i = 0; i < h; i++) {
        if (i + y >= screen.getScreenHeight()) {
          break;
        }

        // // *2 cf uint16_t
        // memcpy( &row[0], &spriteArea[ ( (this->y+i) * SPRITE_AREA_WIDTH )+this->x ],
        // w*2 );
        for (int col = 0; col < w; col++) {
          row[col] = spriteArea[((this.y + i) * SPRITE_AREA_WIDTH) + this.x + col];
        }

        screen.fillRect(x, i + y, w, 1, row);
        // System.out.println(".");
      }
    }

  };

  void cleanSprites() {
    // memset(spriteArea, 0, SPRITE_AREA_SIZE);
    for (int i = 0; i < SPRITE_AREA_SIZE; i++) {
      spriteArea[i] = 0;
    }

    System.out.println("cleanSprites()");

    spriteInstanceCounter = 0;
    lastAddr = 0;
    for (int i = 0; i < NB_SPRITES; i++) {
      // Java specific
      sprites[i] = new Sprite();

        System.out.println("feeding sprite #"+i);

      sprites[i].invalid();
    }
  }

  final int BUFFPIXEL = 80;

  // will takes only 160x120 px of bmp file
  boolean _feedSprites(String filename, int x, int y) throws IOException {
    if (filename == null || (filename.length()) <= 0 || (filename.length()) >= 32) {
      console.warn("Wrong BMP filename !");
      return false;
    }

    FileInputStream bmpFS;

    led.drive_led(true);
    // Open requested file on SD card
    File f = fs.resolveAssetPath(filename);

    if (f == null || !f.exists()) {
      System.out.println(f + " was NOT found !");
      console.warn("File not found");
      led.drive_led(false);
      return false;
    }

    // System.out.println(f+" was found !");

    bmpFS = new FileInputStream(f);

    int seekOffset;
    int w, h, row; // , col;
    int r, g, b;

    int sign = read16(bmpFS);

    // System.out.println(Integer.toHexString(sign)+" instead of 0x4D42");

    if (sign == 0x4D42) {
      read32(bmpFS);
      read32(bmpFS);
      seekOffset = read32(bmpFS);
      read32(bmpFS);
      w = read32(bmpFS);
      h = read32(bmpFS);

      if ((read16(bmpFS) == 1) && (read16(bmpFS) == 24) && (read32(bmpFS) == 0)) {
        // y += h - 1;

        // TFT_eSPI specific may cause WEIRD_MODE on drawPAK !!!!!
        // tft.setSwapBytes(true);

        // System.out.println("seekOffset="+seekOffset);
        seekOffset -= 32; // beware : spe

        // bmpFS.seek(seekOffset);
        bmpFS.skip(seekOffset);

        int padding = (4 - ((w * 3) & 3)) & 3;
        byte[] lineBuffer = new byte[w * 3];

        // should be 0 for 320x240
        // System.out.println("padding="+padding);

        if ( h > 120 ) { h = 120; }
        if ( w > 160 ) { w = 160; }

        for (row = 0; row < h; row++) {
          bmpFS.read(lineBuffer, 0, sizeof(lineBuffer));

          byte[] bptr = lineBuffer;
          // uint16_t* tptr = (uint16_t*)lineBuffer;
          int[] tptr = new int[lineBuffer.length];

          // Convert 24 to 16 bit colours
          int ii = 0;
          for (int col = 0; col < w; col++) {
            r = byteToUint8(bptr[ii++]);
            g = byteToUint8(bptr[ii++]);
            b = byteToUint8(bptr[ii++]);

            // rgb to color 565
            tptr[col] = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >> 3);
          }
          // Read any line padding
          byte[] gargabe = new byte[lineBuffer.length];
          if (padding != 0)
            bmpFS.read(gargabe, 0, padding);

          // screen.fillRect(x, y--, w, 1, tptr);

          for (int col = 0; col < w; col++) {
            if ( row >= SPRITE_AREA_HEIGHT-1 ) { continue; } // there is a misterous bug here
            spriteArea[ (row*SPRITE_AREA_WIDTH) + col ] = tptr[col];
          }

          // Zzz(10);

        }

      } else
        console.warn("BMP format not recognized.");
    }
    bmpFS.close();
    led.drive_led(false);
    return true;

    // FileInputStream bmpFile;
    // int bmpWidth, bmpHeight; // W+H in pixels
    // int bmpDepth; // Bit depth (currently must be 24)
    // int bmpImageoffset; // Start of image data in file
    // int rowSize; // Not always = bmpWidth; may have padding
    // byte[] sdbuffer = new byte[3 * BUFFPIXEL]; // pixel buffer (R+G+B per pixel)
    // int buffidx = sizeof(sdbuffer); // Current position in sdbuffer
    // boolean goodBmp = false; // Set to true on valid header parse
    // boolean flip = true; // BMP is stored bottom-to-top
    // int w, h, row, col;
    // int r, g, b;
    // int pos = 0;// , startTime = millis();

    // int[] awColors = new int[320]; // hold colors for one row at a time...
    // // uint16_t awColors[160]; // hold colors for one row at a time...
    // for (int i = 0; i < 320; i++) {
    //   awColors[i] = 0;
    // }

    // // if ((x >= tft.width()) || (y >= tft.height()))
    // // return;

    // // Open requested file on SD card
    // File f = fs.resolveAssetPath(filename);
    // if (!(f.exists())) {
    //   console.warn("BMP File not found");
    //   return false;
    // }

    // bmpFile = new FileInputStream(f);

    // // Parse BMP header
    // if (read16(bmpFile) == 0x4D42) { // BMP signature
    //   read32(bmpFile);

    //   read32(bmpFile); // Read & ignore creator bytes
    //   bmpImageoffset = read32(bmpFile); // Start of image data
    //   read32(bmpFile);

    //   bmpWidth = read32(bmpFile);
    //   bmpHeight = read32(bmpFile);
    //   if (read16(bmpFile) == 1) { // # planes -- must be '1'
    //     bmpDepth = read16(bmpFile); // bits per pixel
    //     if ((bmpDepth == 24) && (read32(bmpFile) == 0)) { // 0 = uncompressed
    //       goodBmp = true; // Supported BMP format -- proceed!

    //       // BMP rows are padded (if needed) to 4-byte boundary
    //       rowSize = (bmpWidth * 3 + 3) & ~3;

    //       // If bmpHeight is negative, image is in top-down order.
    //       // This is not canon but has been observed in the wild.
    //       if (bmpHeight < 0) {
    //         bmpHeight = -bmpHeight;
    //         flip = false;
    //       }

    //       if ((x >= bmpWidth) || (y >= bmpHeight)) {
    //         console.warn("Sprite OutOfBounds");
    //         return false;
    //       }

    //       // Crop area to be loaded
    //       // w = bmpWidth;
    //       // h = bmpHeight;
    //       w = SPRITE_AREA_WIDTH;
    //       h = SPRITE_AREA_HEIGHT;
    //       if ((x + w - 1) >= bmpWidth)
    //         w = bmpWidth - x;
    //       if ((y + h - 1) >= bmpHeight)
    //         h = bmpHeight - y;

    //       for (row = 0; row < h; row++) { // For each scanline...

    //         // Seek to start of scan line. It might seem labor-
    //         // intensive to be doing this on every line, but this
    //         // method covers a lot of gritty details like cropping
    //         // and scanline padding. Also, the seek only takes
    //         // place if the file position actually needs to change
    //         // (avoids a lot of cluster math in SD library).
    //         if (flip) // Bitmap is stored bottom-to-top order (normal BMP)
    //           pos = bmpImageoffset + (bmpHeight - 1 - row) * rowSize;
    //         else // Bitmap is stored top-to-bottom
    //           pos = bmpImageoffset + row * rowSize;

    //         // see if need to restore that code
    //         // if (bmpFile.position() != pos) { // Need seek?
    //         // bmpFile.seek(pos);
    //         // buffidx = sizeof(sdbuffer); // Force buffer reload
    //         // }

    //         for (col = 0; col < w; col++) { // For each pixel...
    //           // Time to read more pixel data?
    //           if (buffidx >= sizeof(sdbuffer)) { // Indeed
    //             bmpFile.read(sdbuffer, 0, sizeof(sdbuffer));
    //             buffidx = 0; // Set index to beginning
    //           }

    //           // Convert pixel from BMP to TFT format, push to display
    //           b = byteToUint8(sdbuffer[buffidx++]);
    //           g = byteToUint8(sdbuffer[buffidx++]);
    //           r = byteToUint8(sdbuffer[buffidx++]);

    //           awColors[col] = screen.color565(r, g, b);
    //         } // end pixel

    //         // *2 Cf uint16_t
    //         // memcpy( &spriteArea[ (row*SPRITE_AREA_WIDTH)+col ], &awColors[x], w*2 );
    //         // HERE IS A BUG -- DEADLOCK
    //         for (int i = 0; i < w; i++) {
    //           // fails if goes up to 120
    //           if (row >= SPRITE_AREA_HEIGHT - 1) {
    //             break;
    //           }
    //           spriteArea[((row * SPRITE_AREA_WIDTH) /*+ col*/) + i] = awColors[/*x +*/ i];
    //         }

    //       } // end scanline
    //       // long timeElapsed = millis() - startTime;
    //     } // end goodBmp
    //   }
    // }
    // bmpFile.close();
    // if (!goodBmp) {
    //   console.warn("BMP format not recognized.");
    //   return false;
    // }
    // return true;
  }

  boolean grabbSprites(String imageName, int offsetX, int offsetY) throws IOException {
    // char* fileName = Fs.getAssetsFileEntry( imageName );
    String fileName = imageName;
    cleanSprites();
    return _feedSprites(fileName, offsetX, offsetY);
  }

  final int NB_SPRITES = 15;
  Sprite[] sprites = new Sprite[NB_SPRITES];

  int[] spriteArea = new int[SPRITE_AREA_SIZE];
  int lastAddr = 0;

  boolean loadBMPSpriteBoard(String filename) throws IOException {
    boolean ok = grabbSprites(filename, 0, 0);
    return ok;
  }

  boolean defineSprite(int spriteNum, int x, int y, int w, int h) {
    if (spriteNum < 0 || spriteNum >= NB_SPRITES) {
      return false;
    }
    if ( sprites[spriteNum] == null ) {
      System.out.println("null sprite #"+spriteNum);
      return false;
    }
    sprites[spriteNum].setBounds(x, y, w, h);
    return true;
  }

  boolean drawSprite(int spriteNum, int x, int y) {
    if (spriteNum < 0 || spriteNum >= NB_SPRITES) {
      return false;
    }
    if ( sprites[spriteNum] == null ) {
      // System.out.println("null sprite #"+spriteNum);
      return false;
    }
    sprites[spriteNum].drawClip(x, y);
    return true;
  }

}