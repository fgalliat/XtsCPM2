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
 Le croquis utilise 76184 octets (7%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 Les variables globales utilisent 23664 octets (9%) de mémoire dynamique, ce qui laisse 238480 octets pour les variables locales. Le maximum est de 262144 octets.

 apres ajout de l'API GFX:Sprites
 Le croquis utilise 78108 octets (7%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 Les variables globales utilisent 62404 octets (23%) de mémoire dynamique, ce qui laisse 199740 octets pour les variables locales. Le maximum est de 262144 octets.

*/

// forward symbols
void xts_handler();

#include "xts_dev_joystick.h"
Joystick joystick;

#include "xts_dev_rgbled.h"
RGBLed led;

// just for colors -- TEMP ????
#include "Adafruit_ILI9486_Teensy.h"

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

// ======================
int w = 64;
int h = 64;
// int tlen = (480-20)*(320-20); // mem overflow
int tlen = w * h;
uint16_t raster[ tlen ];
for(int i=0; i < tlen; i++) { raster[i] = i % 3 == 0 ? PINK : ( i % 3 == 1 ? YELLOW : RED ); }

for(int i=0; i < 10; i++) {
    int x = random( 480 - w );
    int y = random( 320 - h );

    screen.fillRect( x, y, w, h, raster );
}

screen.drawPakFile( (char*) "Z/0/ISHAR.PAK", 20, 20, 0);

screen.drawBitmapFile( (char*) "Z/0/GIRL.BMP", 40, 40);

/*
GFX.PAS

 drawBmp('!sprite1.bmp'); {* loadSomeSprites *}
 defineSprite(0, 0, 1, 19, 19); { upperL }
 defineSprite(1, 40, 1, 19, 19); { upperR }
 defineSprite(2, 40,20, 19, 19); { lowerR }

 defineSprite(5, 20, 1, 19, 19); { title bck }

 defineSprite(6, 62, 1, 31, 28); { file }
 defineSprite(7, 95, 1, 31, 28); { folder }

drawSprite( 0, x, y ); ...

*/


screen.fillCircle( 100, 100, 30, BLUE );
screen.drawCircle( 100, 100, 30, GREEN );


screen.loadBMPSpriteBoard( (char*) "Z/0/sprite1.bmp");
screen.defineSprite(0, 0, 1, 19, 19); // upperL
screen.defineSprite(1, 40, 1, 19, 19); //{ upperR }
screen.defineSprite(2, 40,20, 19, 19); //{ lowerR }

screen.drawSprite(0, 100, 100);
screen.drawSprite(1, 100+19+3, 100);
screen.drawSprite(2, 100+19+3+19+3, 100);

screen.fillRect( 10, 10, w, h, raster );
while(true) delay(10000);
// ======================




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