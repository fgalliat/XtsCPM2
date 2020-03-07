/**
 * Menu I/O test w/ Fs support
 * 
 * I/O Console + Joystick + FsLayer
 * 
 * Xtase - fgalliat @Mar2020
 */

#include "xts_dev_joystick.h"
Joystick joystick;

#include "xts_soft_console.h"
IOConsole console( CONSOLE_MODE_SERIAL_VT100 | CONSOLE_MODE_TFT );

#include "xts_arduino_dev_fs.h"
Fs fileSystem;

void once();

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
        // remove Serial VTconsole
        console.setMode( CONSOLE_MODE_TFT );

        console.cls();
        console.splashScreen_SD();
        console.cursor(10, 40);
        console.attr_accent();
        console.println("System is OK !");
        console.attr_none();

        console.cursor(17, 1);

        once();
    } else {
        console.println("");
        console.println(" - System is not OK -");
        console.println(" Halting.");
        while( true ) { delay(1000); }
    }

}

void xts_handler() {
    joystick.poll();
}


void once() {
    int margin = 5;

    int hMargin = margin;
    int wMargin = margin;

    bool clearBehindWindow = true;    

    if ( console.getWidth() > 40 ) {
        wMargin = 10;
    }
    if ( console.getHeight() > 25 ) {
        hMargin = 10;
        clearBehindWindow = false;
    }

    int x1 = wMargin;
    int y1 = hMargin;
    int x2 = console.getWidth()-wMargin;
    int y2 = console.getHeight()-hMargin;

    char* title = (char*)"[ Main Menu ]";

    int nbItems = 5;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"Enable Serial Console",
        (char*)"Disable Serial Console",
        (char*)"WiFi menu",
        (char*)"",
        (char*)"Exit",
    };

    int choice = console.menu(x1, y1, x2, y2, title, items, nbItems, clearBehindWindow);

    console.print( "Ya choosed : " );
    console.println( choice );
}

void loop() {
    xts_handler();

    if ( joystick.hasChangedState() ) {
        console.println( joystick.toString() );
    }

    delay(100);
}