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

  int getStringFromRam(int32 addr, char* dest, int maxLen) {
      return getPascalStringFromRam(addr, dest, maxLen);
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




int32 XtsBdosCall(uint8 regNum, int32 value) {
    if ( regNum == 225 ) { return bdosDraw(value); }
    console.print("Call Bdos #");
    console.print(regNum);
    console.print("->");
    console.println(value);
    return 0;
}

#endif