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

#define CLR_TXT_DEF WHITE
#define CLR_TXT_ACC GREEN

#define TFT_DESC tft

int con_tft_width() {
    return TFT_CAP_WIDTH;
}

int con_tft_height() {
    return TFT_CAP_HEIGHT;
}

void con_tft_init() {
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

// 1-based
void con_tft_cursor(int row, int col) {
  // force cursor position
  tft.setCursor( (col-1)*6, (row-1)*8);
}

void con_tft_attr_accent() {
    tft.setTextColor(CLR_TXT_ACC);
}

void con_tft_attr_none() {
    tft.setTextColor(CLR_TXT_DEF);
}