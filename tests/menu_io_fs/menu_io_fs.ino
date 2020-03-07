/**
 * Menu I/O test w/ Fs support
 * 
 * I/O Console + Joystick + FsLayer
 * 
 * Xtase - fgalliat @Mar2020
 */

#include "xts_dev_joystick.h"
Joystick joystick;

#include "xts_res_console.h"
#include "xts_soft_console.h"
IOConsole console( CONSOLE_MODE_SERIAL_VT100 | CONSOLE_MODE_TFT );

#include "xts_arduino_dev_fs.h"
Fs fileSystem;

void setup() {
    joystick.setup();

    // use console...
    // Serial.begin(115200);
    console.setup();

    bool allOk = true;
    bool sdOk = fileSystem.setup();
    allOk &= sdOk;

    console.cls();
    console.println("Joystick ... OK");
    console.println("Console  ... OK");
    if ( sdOk ) {
        console.println("FileSyst ... OK");
    } else {
        console.println("FileSyst ... NOK");
    }
    console.println("");

    delay(300);

    if ( allOk ) {
        console.cls();
        console.print( splash_screen_SD );
    }

}

void xts_handler() {
    joystick.poll();
}


void loop() {
    xts_handler();

    if ( joystick.hasChangedState() ) {
        console.println( joystick.toString() );
    }

    delay(100);
}