/**
 * Menu I/O test w/ Fs support
 * 
 * I/O Console + Joystick + FsLayer
 * 
 * Xtase - fgalliat @Mar2020
 */

#include "xts_dev_joystick.h"
Joystick joystick;

void setup() {
    joystick.setup();

    // use console...
    Serial.begin(115200);
}

void xts_handler() {
    joystick.poll();
}


void loop() {
    xts_handler();


}