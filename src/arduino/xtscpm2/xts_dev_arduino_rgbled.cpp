#include "top.h"

/**
 * RGB led driver API
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

#include "Arduino.h"

#include "xts_dev_rgbled.h"

#define RED_PIN   21
#define GREEN_PIN 22
#define BLUE_PIN  23

RGBLed::RGBLed() {
}

bool RGBLed::setup() {
    pinMode(RED_PIN, OUTPUT);   digitalWrite(RED_PIN, LOW);
    pinMode(GREEN_PIN, OUTPUT); digitalWrite(GREEN_PIN, LOW);
    pinMode(BLUE_PIN, OUTPUT);  digitalWrite(BLUE_PIN, LOW);

    off(); // saves init values
    return true;
}

void RGBLed::off() {
    rgb(0x00, 0x00, 0x00);
}

void RGBLed::save(uint8_t r, uint8_t g, uint8_t b) {
    red = r;
    green = g;
    blue = b;
}
void RGBLed::restore() {
    setState(red, green, blue);
}
void RGBLed::setState(uint8_t r, uint8_t g, uint8_t b) {
    analogWrite(RED_PIN, r);
    analogWrite(GREEN_PIN, g);
    analogWrite(BLUE_PIN, b);
}

void RGBLed::rgb(uint8_t r, uint8_t g, uint8_t b) {
    save(r, g, b);
    setState(r, g, b);
}

void RGBLed::clr_red() {
    rgb(0xFF, 0x00, 0x00);
}
void RGBLed::clr_green() {
    rgb(0x00, 0xFF, 0x00);
}
void RGBLed::clr_blue() {
    rgb(0x00, 0x00, 0xFF);
}

void RGBLed::drive_led(bool state) {
    if ( state ) {
        setState(0xFF, 0x00, 0x00);
    } else {
        restore();
    }
}