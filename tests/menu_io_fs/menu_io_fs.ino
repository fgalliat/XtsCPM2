#include "top.h"

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

#include "xts_dev_fs.h"
Fs fileSystem;

#include "xts_dev_buzzer.h"
Buzzer buzzer;


void once();

void setup() {
    joystick.setup();
    buzzer.setup();

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

// =====================================================
bool audioMenu() {
    char* title = (char*)"[ Audio Menu ]";

    int nbItems = 7;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"MUTE Speaker",
        (char*)"MUTE MP3",
        (char*)"VOLUME MP3",
        (char*)"Test Speaker",
        (char*)"Test MP3",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);
        
        console.print( "Ya choosed : " );
        console.println( choice );

        if ( choice == 0 ) {
            if ( buzzer.isMute() ) {
                buzzer.unmute();
                buzzer.tone(400, 200);
            } else {
                buzzer.noTone();
                buzzer.mute();
            }
        } else if ( choice == 1 ) {
            console.println(" (UN)MUTE MP3 ");
        } else if ( choice == 2 ) {
            console.println(" VOLUME MP3 (slider) ");
        } else if ( choice == 3 ) {
            buzzer.playTuneFile( (char*)"MONKEY.T5K");
        } else if ( choice == 4 ) {
            console.println(" TEST MP3 ");
        } else if ( choice == 5 ) {
            buzzer.playTuneFile( (char*)"MONKEY.T5K");
        } else if ( choice == 6 ) {
        } else if ( choice == 7 ) {
            return false;
        } else if ( choice == -1 ) {
            return true;
        }
    }
    return false; // false, doesn't kill previous menu
}


bool inMenu = false;
void menu() {
    if ( inMenu ) { return; }
    inMenu = true;

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

    const int AUDIO_SUBMENU_ITEM = 1;

    int nbItems = 7;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"Play MONKEY.T5K",
        (char*)"Audio ->",
        (char*)"Enable Serial Console",
        (char*)"Disable Serial Console",
        (char*)"WiFi menu",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems, x1, y1, x2, y2, clearBehindWindow);
        
        console.print( "Ya choosed : " );
        console.println( choice );

        if ( choice == 0 ) {
            buzzer.playTuneFile( (char*) "MONKEY.T5K");
        } else if ( choice == AUDIO_SUBMENU_ITEM ) {
            // if ( audioMenu() ) { break; }
            audioMenu();
        }
    }

    inMenu = false;
}

void xts_handler() {
    joystick.poll();

    if ( joystick.hasChangedState() ) {
        if ( joystick.isBtnMenu() ) {
            menu();
        }
    }
}

void once() {
    menu();
}

void loop() {
    xts_handler();

    if ( joystick.hasChangedState() ) {
        console.println( joystick.toString() );
    }

    delay(100);
}