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
        if ((x >= screen.getScreenWidth()) || (y >= screen.getScreenHeight())) return false;
      
        FileInputStream bmpFS;
      
        led.drive_led(true);
        // Open requested file on SD card
        File f= fs.resolveAssetPath(filename);
        
        if (f == null || !f.exists())
        {
            System.out.println(f+" was NOT found !");
            console.warn("File not found");
          led.drive_led(false);
          return false;
        }
      
System.out.println(f+" was found !");


        bmpFS = new FileInputStream(f);

        int seekOffset;
        int w, h, row; //, col;
        int  r, g, b;
      
        int sign = read16(bmpFS);

        System.out.println(Integer.toHexString(sign)+" instead of 0x4D42");

        if (sign == 0x4D42)
        {
          read32(bmpFS);
          read32(bmpFS);
          seekOffset = read32(bmpFS);
          read32(bmpFS);
          w = read32(bmpFS);
          h = read32(bmpFS);
      
          if ( rotated ) {
              // en fait : devient X
              y = screen.getScreenWidth() - y;
          }
      
          if ((read16(bmpFS) == 1) && (read16(bmpFS) == 24) && (read32(bmpFS) == 0))
          {
            //   y += h - 1;
      
            // TFT_eSPI specific may cause WEIRD_MODE on drawPAK !!!!!
            // tft.setSwapBytes(true);
      
            // bmpFS.seek(seekOffset);
            bmpFS.skip(seekOffset);
      
            int padding = (4 - ((w * 3) & 3)) & 3;
            byte[] lineBuffer = new byte[w * 3];
      
            if ( rotated ) { screen.setRotated(true); }
      
            for (row = 0; row < h; row++) {
              bmpFS.read(lineBuffer, 0, sizeof(lineBuffer));

              byte[] bptr = lineBuffer;
            //   uint16_t* tptr = (uint16_t*)lineBuffer;
              int[] tptr = new int[lineBuffer.length];

              // Convert 24 to 16 bit colours
              int ii=0;
              for (int col = 0; col < w; col++)
              {
                b = bptr[ii++];
                g = bptr[ii++];
                r = bptr[ii++];
                tptr[col] = ((r & 0xF8) << 8) | ((g & 0xFC) << 3) | (b >> 3);
              }
              // Read any line padding
              byte[] gargabe = new byte[lineBuffer.length];
              if (padding != 0) bmpFS.read(gargabe, 0, padding);
              // Push the pixel row to screen, pushImage will crop the line if needed
              // tft.pushImage(x, y--, w, 1, (uint16_t*)lineBuffer);
              // tft.drawRGBBitmap(x, y--, (uint16_t*)lineBuffer, w, 1);
              
              //screen.fillRect(x, y--, w, 1, lineBuffer);
               System.out.println("drawRaster("+x+", "+y+", "+w+", 1, raster)");
              screen.fillRect(x, y--, w, 1, tptr);
            }
      
            if ( rotated ) { screen.setRotated(false); }
      
          }
          else console.warn("BMP format not recognized.");
        }
        bmpFS.close();
        led.drive_led(false);
        return true;
      }

}