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
            b = byteToUint8(bptr[ii++]);
            g = byteToUint8(bptr[ii++]);
            r = byteToUint8(bptr[ii++]);

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
        _scanArray16[i] = (scanArray[(i * 2) + 0] << 8) + scanArray[(i * 2) + 1];
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
}