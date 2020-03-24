/**
 * TFT console behavior
 * vt100 console
 * 
 * Xtase - fgalliat @Feb2020
 */

#include <SPI.h>
#include "Adafruit_ILI9486_Teensy.h"

// for pin definitions, please refer to the header file
Adafruit_ILI9486_Teensy tft;

#define TFT_CAP_WIDTH 80
#define TFT_CAP_HEIGHT 40

#define CLR_TXT_BCK BLACK
#define CLR_TXT_DEF WHITE
#define CLR_TXT_ACC GREEN

uint16_t tty_colors[] = {
  CLR_TXT_DEF, CLR_TXT_ACC
};

uint16_t tty_bg_colors[] = {
  CLR_TXT_BCK, CLR_TXT_BCK
};

char ttyMEMSEG[TFT_CAP_WIDTH*TFT_CAP_HEIGHT];
char ttyAttrMEMSEG[TFT_CAP_WIDTH*TFT_CAP_HEIGHT];

#define TFT_DESC tft

int con_tft_width() {
    return TFT_CAP_WIDTH;
}

int con_tft_height() {
    return TFT_CAP_HEIGHT;
}

bool _inited = false;

void con_tft_init() {
    _inited = false;

    // erase console mem
    int mlen = TFT_CAP_WIDTH * TFT_CAP_HEIGHT;
    memset( ttyMEMSEG, 0x00, mlen );
    memset( ttyAttrMEMSEG, 0x00, mlen );

    SPI.begin();
    tft.begin();
    tft.setRotation(1);
    tft.setTextSize(1);
    tft.fillScreen(CLR_TXT_BCK);
    tft.setCursor(0,0);
    tft.setTextColor(CLR_TXT_DEF);

    _inited = true;
}

bool con_tft_ready() {
  return _inited;
}

Print* con_tft() {
  return &TFT_DESC;
}


char currTtyAttr = 0x00;
int ttyCursorX = 0;
int ttyCursorY = 0;

// 1-based
void con_tft_cursor(int row, int col) {
  // force cursor position
  ttyCursorX = col-1;
  ttyCursorY = row-1;

  if ( ttyCursorX < 0 ) { ttyCursorX = 0; }
  if ( ttyCursorY < 0 ) { ttyCursorY = 0; }
  if ( ttyCursorX >= TFT_CAP_WIDTH ) { ttyCursorX = TFT_CAP_WIDTH-1; }
  if ( ttyCursorY >= TFT_CAP_HEIGHT ) { ttyCursorY = TFT_CAP_HEIGHT-1; }

  // tft.setCursor( ttyCursorX*6, ttyCursorY*8);
}

void con_tft_cls() {
  // do cls
  tft.fillScreen(BLACK);

  // erase console mem
  int mlen = TFT_CAP_WIDTH * TFT_CAP_HEIGHT;
  memset( ttyMEMSEG, 0x00, mlen );
  memset( ttyAttrMEMSEG, 0x00, mlen );

  // set cursor Home
  // tft.setCursor(0, 0);
  con_tft_cursor(1,1);
}

void con_tft_eraseTillEOL() {
  for(int i=ttyCursorX; i < TFT_CAP_WIDTH; i++) {
    con_tft_writeOneChar(' ');
  }
}


void con_tft_attr_accent() {
    tft.setTextColor(CLR_TXT_ACC);
    currTtyAttr = 0x01;
}

void con_tft_attr_none() {
    tft.setTextColor(CLR_TXT_DEF);
    currTtyAttr = 0x00;
}

void _redrawWholeFrame();

void _scrollTop(int howMany=1) {
  // scroll mem
  int mLen = ( (TFT_CAP_HEIGHT - howMany)*TFT_CAP_WIDTH);
  int mAddr = (howMany*TFT_CAP_WIDTH);
  int mlen = ( (howMany)*TFT_CAP_WIDTH);

  memmove( &ttyMEMSEG[0], &ttyMEMSEG[ mAddr ], mLen );
  memmove( &ttyAttrMEMSEG[0], &ttyAttrMEMSEG[ mAddr ], mLen );
  // blank mem
  memset( &ttyMEMSEG[mAddr], 0x00, mlen );
  memset( &ttyAttrMEMSEG[mAddr], 0x00, mlen );

  tft.fillRect(0, 0, 480, (TFT_CAP_HEIGHT-howMany)*8, CLR_TXT_BCK);
  _redrawWholeFrame();

  con_tft_cursor( TFT_CAP_HEIGHT, 1 );
}

bool _needToScroll() {
  return ttyCursorY >= TFT_CAP_HEIGHT;
}


void con_tft_writeOneChar(char ch) {
  int memAddr = (ttyCursorY * TFT_CAP_WIDTH)+ttyCursorX;
  // TODO : protect memAddr
  // for now : protected by _cursor(row,col)
  ttyAttrMEMSEG[memAddr] = currTtyAttr;
  ttyMEMSEG[memAddr] = ch;

  if ( ch == 0x0D ) { // \r
    ttyCursorX = 0;
    ttyCursorY++;
    if ( _needToScroll() ) { _scrollTop(1); }
    return;
  }
  if ( ch == 0x0A ) { // \n
    return;
  }
  if ( ch == '\b' ) {
    // is generally used as '\b'+' '+'\b' so no need to render it
    ttyCursorX--;
    if ( ttyCursorX < 0 ) {
        ttyCursorY--;
        if ( ttyCursorY < 0 ) {
            ttyCursorY = 0;
        }
    }
    return;
  }
  uint16_t col = tty_colors[ (int)currTtyAttr ];
  uint16_t bg  = tty_bg_colors[ (int)currTtyAttr ];
  int x = ttyCursorX * 6;
  int y = ttyCursorY * 8;
  tft.drawChar(x, y, ch, col, bg, 1);
  ttyCursorX++;
  if ( ttyCursorX >= TFT_CAP_WIDTH ) {
    ttyCursorX = 0;
    ttyCursorY++;
    if ( _needToScroll() ) { _scrollTop(1); }
  }
}

void _redrawWholeFrame() {
  int mlen = TFT_CAP_WIDTH * TFT_CAP_HEIGHT;
  char ch; int attr; uint16_t fg,bg;
  int x = 0, y = 0;
  for(int i=0; i < mlen; i++) {
    ch = ttyMEMSEG[i];
    if ( !( ch == 0x00 || ch == 0x0A || ch == 0x0D )) { 
      attr = (int)ttyAttrMEMSEG[i];
      fg = tty_colors[ attr ];
      bg = tty_bg_colors[ attr ];
      tft.drawChar(x, y, ch, fg, bg, 1);
    } 

    x += 6; if ( x >= 480 ) { x = 0; y += 8; }
  }
}

