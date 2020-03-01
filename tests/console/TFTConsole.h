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
}

uint16_t tty_bg_colors[] = {
  CLR_TXT_BCK, CLR_TXT_BCK
}

char ttyMEMSEG[TFT_CAP_WIDTH*TFT_CAP_HEIGHT];
char ttyAttrMEMSEG[TFT_CAP_WIDTH*TFT_CAP_HEIGHT];



#define TFT_DESC tft

int con_tft_width() {
    return TFT_CAP_WIDTH;
}

int con_tft_height() {
    return TFT_CAP_HEIGHT;
}

void con_tft_init() {
    for(int i=0; i < TFT_CAP_WIDTH*TFT_CAP_HEIGHT; i++) {
      ttyMEMSEG[i] = 0x00;
      ttyAttrMEMSEG = 0x00;
    }


    SPI.begin();
    tft.begin();
    tft.setRotation(1);
    tft.setTextSize(1);
    tft.fillScreen(BLACK);
    tft.setCursor(0,0);
    tft.setTextColor(CLR_TXT_DEF);
}

bool con_tft_ready() {
  return true;
}

Print* con_tft() {
  return &TFT_DESC;
}


void con_tft_cls() {
  // do cls
  tft.fillScreen(BLACK);

  // set cursor Home
  tft.setCursor(0, 0);
}

char currTtyAttr = 0x00;
int ttyCursorX = 0;
int ttyCursorY = 0;

// 1-based
void con_tft_cursor(int row, int col) {
  // force cursor position
  tft.setCursor( (col-1)*6, (row-1)*8);
  ttyCursorX = col-1;
  ttyCursorY = row-1;
}

void con_tft_attr_accent() {
    tft.setTextColor(CLR_TXT_ACC);
    currTtyAttr = 0x01;
}

void con_tft_attr_none() {
    tft.setTextColor(CLR_TXT_DEF);
    currTtyAttr = 0x00;
}

void con_tty_writeOneChar(char ch) {
  int memAddr = (ttyCursorY * TFT_CAP_WIDTH)+ttyCursorX;
  // TODO : protect memAddr
  ttyAttrMEMSEG[memAddr] = currTtyAttr;
  ttyMEMSEG[memAddr] = ch;

  if ( ch == 0x0C ) { // \r
    ttyCursorX = 0;
    ttyCursorY++;
    // TODO : need to scroll ?
    return;
  }
  if ( ch == 0x0A ) { // \n
    return;
  }
  // int col = tty_colors[ ttyAttrMEMSEG[memAddr] ];
  // int bg = tty_bg_colors[ ttyAttrMEMSEG[memAddr] ];
  uint16_t col = tty_colors[ currTtyAttr ];
  uint16_t bg  = tty_bg_colors[ currTtyAttr ];
  int x = ttyCursorX * 6;
  int y = ttyCursorY * 8;
  tft.drawChar(x, y, ch, col, bg, 1);
  ttyCursorX++;
  if ( ttyCursorX >= TFT_CAP_WIDTH ) {
    ttyCursorX = 0;
    ttyCursorY++;
    // TODO : need to scroll ?
  }
}