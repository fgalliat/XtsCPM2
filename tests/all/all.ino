#include "top.h"

/**
 * Menu I/O test w/ Fs support
 * 
 * I/O Console + Joystick + FsLayer
 * 
 * Xtase - fgalliat @Mar2020
 */

/*
 avant le WiFi :
 Le croquis utilise 65336 octets (6%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 Les variables globales utilisent 18464 octets (7%) de mémoire dynamique, ce qui laisse 243680 octets pour les variables locales. Le maximum est de 262144 octets.

 apres :
 Le croquis utilise 69008 octets (6%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 Les variables globales utilisent 19048 octets (7%) de mémoire dynamique, ce qui laisse 243096 octets pour les variables locales. Le maximum est de 262144 octets.

 apres appel a connectToAp() + wget()
 Le croquis utilise 75696 octets (7%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 Les variables globales utilisent 23664 octets (9%) de mémoire dynamique, ce qui laisse 238480 octets pour les variables locales. Le maximum est de 262144 octets.

 apres ajout de l'API GFX
 
*/

// forward symbols
void xts_handler();

#include "xts_dev_joystick.h"
Joystick joystick;

#include "xts_dev_rgbled.h"
RGBLed led;

#include "xts_dev_gfx.h"
VideoCard screen;

#include "xts_soft_console.h"
IOConsole console( CONSOLE_MODE_SERIAL_VT100 | CONSOLE_MODE_TFT );

#include "xts_dev_fs.h"
Fs fileSystem;

#include "xts_dev_buzzer.h"
Buzzer buzzer;

#include "xts_dev_soundcard.h"
SoundCard snd;

#define WIFI_AT_START 1
#include "xts_dev_wifi.h"
WiFi wifi;

#include "xts_soft_menu.h"

void once();

void setup() {
    joystick.setup();
    led.setup();
    buzzer.setup();

    screen.setup();

    // use console...
    // Serial.begin(115200);
    console.setup();

    bool allOk = true;
    bool sdOk = fileSystem.setup();
    allOk &= sdOk;

    bool sndOk = snd.setup();
    allOk &= sndOk;

    bool wifOk = false;
    #if WIFI_AT_START
        wifOk = wifi.setup();
        // don't affect the allOk flag ...
    #endif

    console.cls();
    console.println("Joystick ... OK");
    console.println("Leds     ... OK");
    console.println("Console  ... OK");
    if ( sdOk ) {
        console.println("FileSyst ... OK");
    } else {
        console.println("FileSyst ... NOK");
    }
    if ( sndOk ) {
        console.println("SoundCard ... OK");
    } else {
        console.println("SoundCard ... NOK");
    }
    if ( wifOk ) {
        console.println("WiFi      ... OK");
    } else {
        console.println("WiFi      ... NOK");
    }
    console.println("");

    delay(300);

    if ( allOk ) {
        led.rgb( 209, 228, 194 ); // pastel green
        
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
        led.rgb( 187,  74, 230 ); // purple
        
        console.println("");
        console.println(" - System is not OK -");
        console.println(" Halting.");
        while( true ) { delay(1000); }
    }

}

// =====================================================


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