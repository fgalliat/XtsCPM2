#ifndef __XTS_BDOS__
#define __XTS_BDOS__ 1

  // from TurboPascal 3 strings are 255 long max
  // & starts @ 1 ( 'cause @0 contains length)
  int getPascalStringFromRam(int32 addr, char* dest, int maxLen) {
      memset(dest, 0x00, maxLen);
      uint8_t *F = (uint8_t*)_RamSysAddr(addr);

      uint8_t len = F[0]; // seems to be init len (not strlen)
      memcpy(dest, &F[1], min(len,maxLen) );

      return len;
  } 

  int setPascalStringToRam(int32 addr, char* src, bool overwriteLen=false) {
      const int maxPascalStringLen = 255;
      int tlen = min( maxPascalStringLen, strlen(src) );

      uint8_t *F = (uint8_t*)_RamSysAddr(addr);

      if (overwriteLen) {
        F[0] = (uint8_t)tlen;
      }
      uint8_t len = F[0]; // seems to be init len (not strlen)

      len = min(len, tlen);
      memcpy(&F[1], src , len );

      return len;
  } 


  int getStringFromRam(int32 addr, char* dest, int maxLen) {
      return getPascalStringFromRam(addr, dest, maxLen);
  }

  int setStringToRam(int32 addr, char* src, bool overwriteLen=false) {
      return setPascalStringToRam(addr, src, overwriteLen);
  }


  int32 xbdos_console(int32 value) {
     int a0 = HIGH_REGISTER( value );
     int a1 = LOW_REGISTER( value ); 
// TODO : make it work again
// TODO : a 25max height console mode

    //  if ( a0 == 0 ) {
    //      // set the console colorSet
    //      if ( a1 == 0 ) {
    //         // to reset
    //         yatl.getScreen()->consoleColorSet(); 
    //      } else {
    //         // set as old monochrome LCD (no backlight) 
    //         yatl.getScreen()->consoleColorSet( rgb(126, 155, 125), rgb(69,80,110), rgb(108-30,120-30,195-30) );
    //      }
    //  } else if ( a0 == 1 ) {
    //      // set the console font size & cols mode
    //      if ( a1 == 0 ) {
    //         // to reset
    //         yatl.getScreen()->consoleSetMode(LCD_CONSOLE_80_COLS, true);
    //      } else {
    //         // set as 53 cols (big font)
    //         yatl.getScreen()->consoleSetMode(LCD_CONSOLE_40_COLS, true);
    //      }
    //  }
     return 0;
  }

int32 drawRoutine(char* test) {
    // then draw it !!!!
    uint8_t OpType = test[1];
    uint8_t shapeType = test[2];
    uint8_t fillType = test[3]; // 0 draw / 1 fill

    uint16_t color = (uint16_t) ((uint8_t)test[4] << 8) + (uint8_t)test[5];
    // color is mapped
    // color = screen.mapColor( color );

    if ( OpType == 0x7F ) {
      // drawShapes
      uint16_t x = (uint16_t) ((uint8_t)test[6] << 8) + (uint8_t)test[7];
      uint16_t y = (uint16_t) ((uint8_t)test[8] << 8) + (uint8_t)test[9];
      if ( shapeType == 0x01 ) {
        // Shape : rectangle
        uint16_t w = (uint16_t) ((uint8_t)test[10] << 8) + (uint8_t)test[11];
        uint16_t h = (uint16_t) ((uint8_t)test[12] << 8) + (uint8_t)test[13];
        if ( fillType == 0x00 ) {
          // draw outlines
          screen.drawRect( x, y, w, h, color );
        } else if ( fillType == 0x01 ) {
          // fills the rect

// if ( y > 220 ) {
// char str[64]; sprintf(str,"drawRect(%d,%d,%d,%d,%d)", x, y, w, h, color);
// yat4l_dbug( (const char*)str );
// }

          screen.fillRect( x, y, w, h, color );
        }
      } else if ( shapeType == 0x02 ) {
        // Shape : circle
        uint16_t r = (uint16_t)((uint8_t)test[10] << 8) + (uint8_t)test[11];
        if ( fillType == 0x00 ) {
          // draw outlines
          screen.drawCircle( x,y,r, color );
        } else {
          screen.fillCircle( x,y,r, color );
        }
      } else if ( shapeType == 0x03 ) {
        // Shape : line
        uint16_t x2 = (uint16_t) ((uint8_t)test[10] << 8) + (uint8_t)test[11];
        uint16_t y2 = (uint16_t) ((uint8_t)test[12] << 8) + (uint8_t)test[13];
        screen.drawLine( x, y, x2, y2, color );
      } 
    } else if ( OpType == 0x80 ) {
      // manage Sprite
      uint16_t x = (uint16_t)((uint8_t)test[6] << 8) + (uint8_t)test[7];
      uint16_t y = (uint16_t)((uint8_t)test[8] << 8) + (uint8_t)test[9];

      if ( shapeType == 0x01 ) {
        // define sprite
        uint16_t w = (uint16_t)((uint8_t)test[10] << 8) + (uint8_t)test[11];
        uint16_t h = (uint16_t)((uint8_t)test[12] << 8) + (uint8_t)test[13];

        uint8_t num = test[14];

        //sprites[num].setBounds( x, y, w, h );
        screen.defineSprite(num, x, y, w, h);
      } else if ( shapeType == 0x02 ) {
        // draw sprite
        uint8_t num = test[10];

        //sprites[num].drawClip( x, y );
        screen.drawSprite(num, x, y);
      }

    }

    return 0;
  }



int32 bdosDraw(int32 value) {
    char test[256];
    getStringFromRam(value, test, 256);

    if ( (unsigned char)test[1] >= 0x7F ) {
        return drawRoutine( test );
    }

    // yat4l_dbug("/===== BDos PString call =====\\");
    // yat4l_dbug(test);

    upper(test);

    if ( endsWith(test, (char*)".BMP") ) {
    if ( test[0] == '!' ) {
        // yat4l_dbug("|  Wanna grabb a BMP SpriteBoard |");
        screen.loadBMPSpriteBoard( fileSystem.getAssetsFileEntry( &test[1] ) );
    } else {
        // yat4l_dbug("|  Wanna draw a BMP wallpaper |");
        // yatl.getScreen()->drawWallpaper( test );
        // TODO -1,-1 => centers wallpaper ...
        screen.drawBitmapFile( fileSystem.getAssetsFileEntry( test ), 0, 0 );
    }

    } else if ( endsWith(test, (char*)".PAK") ) {
        // yat4l_dbug("|  Wanna draw a PAK image |");

        int numImg = (int)test[0]-1; // 1 based
        int x = -1;
        int y = -1; // centered

        char* filename = &test[1];
        int tmp, lastTmp;
        if ( (tmp = indexOf( test, ',', 1 )) > -1 ) {
            char xx[ tmp - 1 ]; // -1 cf frameNum#
            memcpy(xx, &test[1], tmp-1);
            x = atoi(xx);
            lastTmp = tmp;
            tmp = indexOf( test, ',', tmp+1 );
            if ( tmp > -1 ) {
            char yy[ tmp - lastTmp ];
            memcpy(yy, &test[lastTmp+1], tmp-lastTmp);
            y = atoi(yy);
            }
            filename = &test[tmp+1];
        }
        screen.drawPakFile( fileSystem.getAssetsFileEntry( filename ), x, y, numImg);

    } else if ( endsWith(test, (char*)".PCT") ) {
        console.warn("Wanna draw a PCT wallpaper NYI");
    } else if ( endsWith(test, (char*)".BPP") ) {
        console.warn("Wanna draw a BPP wallpaper NYI");
    } else {
        console.warn("Wanna draw a UNKNOWN wallpaper");
        // yat4l_dbug("| Wanna draw a ");
        // yat4l_dbug( test );
        // printf( "[ %d, %d ]", (int)test[0], (int)test[1] );
        // yat4l_dbug(" -type wallpaper? |");
    }

    // yat4l_dbug("\\===== BDos PString call =====/");
    return 0; 
} 

// ==============] mp3 Hardware Control [==========
  uint8_t mp3BdosCall(int32 value) {
      uint8_t a0 = HIGH_REGISTER(value);
      uint8_t a1 = LOW_REGISTER(value);

      if ( a0 >= (1 << 6) ) {
         // 11000000 -> 11 play+loop -> 64(10)
         // still 16000 addressable songs
         bool loopMode = a0 >= (1 << 7);

         if ( a0 >= 128 ) { a0 -= 128; }

         a0 -= 64;
         int trkNum = (a0<<8) + a1;

         if ( loopMode ) console.warn("mp3 LOOP NYI");

         if ( loopMode ) { ; }
         else { snd.play(trkNum); }
      } else if (a0 == 0x00) {
          snd.stop();
      } else if (a0 == 0x01) {
          snd.pause();
      } else if (a0 == 0x02) {
          snd.next();
      } else if (a0 == 0x03) {
          snd.prev();
      } else if (a0 == 0x04) {
          snd.volume( a1 );
      } else if (a0 == 0x05) {
          // for now : just for demo
          snd.play( 65 );
      } else if (a0 == 0x06) {
          return snd.isPlaying() ? 1 : 0;
      }

    return 0;
  }

  // ==============] Deep Hardware Control [==========
  // 0 should not be possible, used as
  // undefined value
  #define MEMXCHANGE_NOTINIT 0
  int32 memXchangeAddr = MEMXCHANGE_NOTINIT;

  int32 setSystemExchangeAddr(int32 addr) {
    memXchangeAddr = addr;
    return 1;
  }

  int32 subSystemBdosCall(int32 value) {
      // Serial.println("bridge Bdos call");
      uint8_t hiB = HIGH_REGISTER(value);
      if ( hiB == 0 ) {
        // uint8_t volts = (uint8_t)( yatl.getPWRManager()->getVoltage() * 256.0f / 5.0f );
        uint8_t volts = (uint8_t)0xFF; // 5v
        return volts;
      } else if ( hiB == 1 ) {
        reboot();
      } else if ( hiB == 2 ) {
        halt();
      } else if ( hiB == 3 ) {
        uint8_t loB = LOW_REGISTER(value);
        uint8_t r = 0x00;
        uint8_t g = 0x00;
        uint8_t b = 0x00;
        if ( (loB & 4) == 4 ) { r = 0xFF; }
        if ( (loB & 2) == 2 ) { g = 0xFF; }
        if ( (loB & 1) == 1 ) { b = 0xFF; }
        
        led.rgb( r, g, b );

      } else if ( hiB == 4 ) {
        return fileSystem.downloadFromSerial() ? 1 : 0;
      } else if ( hiB == 5 ) {
        // return yatl.getFS()->downloadFromSubMcu() ? 1 : 0;
        console.warn( (char*)"downloadFromWifi() NYI" );
        return 0;
      } else if ( hiB == 6 ) {
        // real delay(x/10) because as we don't emulate
        // CPU cycles ... Pascal.delay() is instable in time 
        uint8_t loB = LOW_REGISTER(value);

        int timeToSleep = loB * 10;
        delay( timeToSleep );

        return 0;
      } // Wifi Device calls -> 64+
      else if ( hiB == 64 ) {
        // Start the telnet server in APmode
        // See later for better

        console.warn( (char*)"Telnet Server NYI" );

        // // just to ensure WiFi will run...
        // // Serial.println("Wasting IP");
        // yat4l_wifi_getIP();
        // // Serial.println("Closing WiFi");
        // yat4l_wifi_close();

        // // Serial.println("Opening WiFi APmode");
        // int ok = yat4l_wifi_beginAP();
        // if ( ok <= 0 ) {
        //   _puts("(!!) Wifi has not started !\n");
        //   return 0;
        // } else {
        //   _puts("(ii) Wifi has started :");
        //   _puts( (const char*) yat4l_wifi_getIP() );
        //   _puts(" !\n");
        // }
        // ok = yat4l_wifi_startTelnetd();
        // if ( ok <= 0 ) {
        //   _puts("(!!) Telnetd has not started !\n");
        //   return 0;
        // } else {
        //   _puts("(ii) Telnetd has started :23 !\n");
        // }

        // return 1;
        return 0;
      }
      else if ( hiB == 65 ) {
        // Get IP

        if ( memXchangeAddr == MEMXCHANGE_NOTINIT ) {
          console.warn( (char*)"MEMXCHANGE not Init" );
          return 0;
        }

        char* ip = wifi.getIp();
        if ( ip == NULL ) {
          console.warn( (char*)"Could not get Ip" );
          return 0;
        }

        setStringToRam( memXchangeAddr, ip, true ); 

        return 1;
      }
      else if ( hiB == 66 ) {
        // Get SSID

        if ( memXchangeAddr == MEMXCHANGE_NOTINIT ) {
          console.warn( (char*)"MEMXCHANGE not Init" );
          return 0;
        }

        char* ssid = wifi.getSSID();
        if ( ssid == NULL ) {
          console.warn( (char*)"Could not get SSID" );
          return 0;
        }

        setStringToRam( memXchangeAddr, ssid, true ); 
        return 1;
      }
      else if ( hiB == 67 ) {
        // Connect to configured SSID:PSK (via conf-index)
        uint8_t loB = LOW_REGISTER(value);
        bool ok = wifi.connectToAp( loB );
        return ok ? 1 : 0;
      } 
      else if ( hiB == 68 ) {
        // Get all configurated SSIDs
        if ( memXchangeAddr == MEMXCHANGE_NOTINIT ) {
          console.warn( (char*)"MEMXCHANGE not Init" );
          return 0;
        }

        char* aps = wifi.listAp();
        if ( aps == NULL ) {
          console.warn( (char*)"Could not get APs" );
          return 0;
        }

        setStringToRam( memXchangeAddr, aps, true ); 
        return 1;
      }
      else if ( hiB == 69 ) {
        // Get all available SSIDs
        if ( memXchangeAddr == MEMXCHANGE_NOTINIT ) {
          console.warn( (char*)"MEMXCHANGE not Init" );
          return 0;
        }
        // for now as same as known SSIDs
        char* aps = wifi.listAp();
        if ( aps == NULL ) {
          console.warn( (char*)"Could not get APs" );
          return 0;
        }

        setStringToRam( memXchangeAddr, aps, true ); 
        return 1;
      }
      else if ( hiB == 70 ) {
        // Open a Soft AP w/ predef. settings
        // return yat4l_wifi_openAnAP((char*) "Yat4L_net", "yatl1234") ? 1 : 0;
        console.warn( (char*)"Open SoftAP NYI" );
        return 0;
      }
      else if ( hiB == 71 ) {
        return wifi.isAtHome() ? 1 : 0;
      }

      return 0;
  }

int32 XtsBdosCall(uint8 regNum, int32 value) {
    if ( regNum == 225 ) { 
        return bdosDraw(value); 
    } else if ( regNum == 226 ) {
        return xbdos_console(value);
    } else if ( regNum == 227 ) {
        return mp3BdosCall(value);
    } else if ( regNum == 228 ) {
        return subSystemBdosCall(value);
    } else if ( regNum == 229 ) {
        // no more a test mode !!
        return setSystemExchangeAddr(value);
        //   yat4l_dbug( "BdosCall 229 NYI => Test Mode" );
        //BdosTest229(value);
    }
    console.print("Call Bdos #");
    console.print(regNum);
    console.print("->");
    console.println(value);
    return 0;
}

#endif