/**
 * Xtase Main & Sub menus definition
 * 
 * 
 * Xtase - fgalliat @Mar2020
 */

#include "xts_string.h"

// sub menus returns true if need to kill caller menu
bool audioMenu();
bool wifiMenu();
bool wifiConnectMenu();
bool consoleMenu();
bool videoTest();

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

    int nbItems = 6;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"Console ->",
        (char*)"WiFi    ->",
        (char*)"Audio   ->",
        (char*)"Video Check",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems, x1, y1, x2, y2, clearBehindWindow);
        
        if ( choice == 0 ) {
            consoleMenu();
        } else if ( choice == 1 ) {
            wifiMenu();
        } else if ( choice == 2 ) {
            audioMenu();
        } else if ( choice == 3 ) {
            videoTest();
        } else if ( choice == -1 ) {
            break;
        } 
    }

    inMenu = false;
}

bool audioMenu() {
    char* title = (char*)"[ Audio Menu ]";

    int nbItems = 7;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"MUTE Speaker  ",
        (char*)"MUTE Music  ",
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
                items[choice] = (char*)"MUTE Speaker  ";
                buzzer.unmute();
                buzzer.tone(400, 200);
            } else {
                items[choice] = (char*)"UNMUTE Speaker";
                buzzer.noTone();
                buzzer.mute();
            }
        } else if ( choice == 1 ) {
            if ( snd.isMute() ) {
                items[choice] = (char*)"MUTE Music  ";
                snd.unmute();
            } else {
                items[choice] = (char*)"UNMUTE Music";
                snd.mute();
            }
        } else if ( choice == 2 ) {
            console.println(" VOLUME MP3 (slider) ");
        } else if ( choice == 3 ) {
            buzzer.playTuneFile( (char*)"MONKEY.T5K");
        } else if ( choice == 4 ) {
            snd.play(1);
            delay(100);
            while( snd.isPlaying() ) {
                console.gotoXY(1,1);
                console.print("PLAY");

                xts_handler();
                if ( joystick.isBtn1() ) {
                    while (joystick.isBtn1()) {
                        joystick.poll();
                        delay(50);
                    }
                    break;
                }

                delay(100);
            }
            snd.stop();
            console.gotoXY(1,1);
            console.print("    ");    
        } else if ( choice == 5 ) {
            // ...
        } else if ( choice == 6 ) {
            return false;
        } else if ( choice == -1 ) {
            return true;
        }
    }
    return false; // false, doesn't kill previous menu
}

const int maxHttpResponseLen = 1024;
char httpResponse[maxHttpResponseLen+1];

bool wifiMenu() {
    char* title = (char*)"[ WiFi Main Menu ]";

    int nbItems = 7;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"Activate WiFi  ",  // in facts : module is always active ...
        (char*)"Reset WiFi",
        (char*)"Connect AP ->",
        (char*)"Test WGet temp",
        (char*)"Test WGet rss",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);

        // must be reset : if change the WiFi config ...
        int homePort = 8000;
        if ( ! wifi.isAtHome() ) {
            homePort = 8090;
        }
        char* apiKeyName = (char*)"sensors";

        if ( choice == 0 ) {
            // if ( wifi is activated ) ...
            items[choice] = (char*)"DeActivate WiFi";
        } else if ( choice == 1 ) {
            // reset wifi
            wifi.resetAdapter();
        } else if ( choice == 2 ) {
            wifiConnectMenu();
        } else if ( choice == 3 ) {
            // wget temp
            memset(httpResponse, 0x00, maxHttpResponseLen+1);
            int rc = wifi.wget( (char*)"$home", homePort, (char*)"/sensors/sensor/1", httpResponse, maxHttpResponseLen, apiKeyName);
            if ( rc >= 400 ) {
                console.attr_accent();
                console.print( "HTTP-ERR " );
                console.println( rc );
                console.attr_none();
                console.println(httpResponse);
            } else {
                console.println(httpResponse);
            }
        } else if ( choice == 4 ) {
            // wget rss
            memset(httpResponse, 0x00, maxHttpResponseLen+1);
            int rc = wifi.wget( (char*)"$home", homePort, (char*)"/rss/titles/1/arduino", httpResponse, maxHttpResponseLen, apiKeyName);
            if ( rc >= 400 ) {
                console.attr_accent();
                console.print( "HTTP-ERR " );
                console.println( rc );
                console.attr_none();
                console.println(httpResponse);
            } else {
                console.println(httpResponse);
            }
        } else if ( choice == 5 ) {
            // ...
        } else if ( choice == 6 ) {
            return false;
        } else if ( choice == -1 ) {
            return true;
        }
    }
    return false; // false, doesn't kill previous menu
}

bool wifiConnectMenu() {
    char* title = (char*)"[ WiFi SSID Menu ]";

    char* ssids = wifi.listAp();
    if ( ssids == NULL ) {
        led.clr_red();
        console.warn((char*)"No WiFi SSID Found !");
        return true;
    }
    int nbSsids = str_count(ssids, '\n');
    char* items[ nbSsids +2 ];
    items[ nbSsids ] = (char*)"";
    items[ nbSsids+1 ] = (char*)"Exit"; 
    int nbItems = nbSsids+2;

    for(int i=0; i < nbSsids; i++) {
        items[i] = str_split(ssids, '\n', i);
    }

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);
        if ( choice == -1 ) {
            return true;
        } else if ( choice == nbItems-1 ) {
            return false;
        } else {
            if ( strlen( items[choice] ) > 0 ) {
                bool ok = wifi.connectToAp(choice);

                if ( !ok ) {
                    console.warn((char*)"Not connected !");
                } else {
                    console.print( "IP : " );
                    console.println( wifi.getIp() );
                    console.print( "SSID : " );
                    console.println( wifi.getSSID() );

                    return false;
                }
            }
        }
    }

    return false;
}

const char* activSerial = "Activate Serial  ";
const char* deactivSerial = "DeActivate Serial  ";

const char* activScreen = "Activate Screen  ";
const char* deactivScreen = "DeActivate Screen  ";
bool conSerialRaw = false;

bool consoleMenu() {
    char* title = (char*)"[ Console Menu ]";

    int nbItems = 6;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)activSerial,
        (char*)deactivScreen,
        (char*)"Serial mode ->",
        (char*)"Serial Input",
        (char*)"",
        (char*)"Exit",
    };

    if ( console.hasSerial() ) {
        items[0] = (char*)deactivSerial;
    }

    if ( !console.hasScreen() ) {
        items[1] = (char*)activScreen;
    }

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);
        
        if ( choice == 0 ) {
            if ( console.hasSerial() ) {
                // remove serial
                console.setMode( CONSOLE_MODE_TFT );
                items[choice] = (char*)activSerial;
            } else {
                // add serial
                console.setMode( CONSOLE_MODE_TFT | (conSerialRaw ? CONSOLE_MODE_SERIAL_DUMMY : CONSOLE_MODE_SERIAL_VT100) );
                items[choice] = (char*)deactivSerial;
            }
        } else if ( choice == 1 ) {
            // TODO : add confirm dialog
            if ( console.hasScreen() ) {
                // remove screen
                console.setMode( (conSerialRaw ? CONSOLE_MODE_SERIAL_DUMMY : CONSOLE_MODE_SERIAL_VT100) );
                items[choice] = (char*)activScreen;
            } else {
                // add screen
                console.setMode( CONSOLE_MODE_TFT | (conSerialRaw ? CONSOLE_MODE_SERIAL_DUMMY : CONSOLE_MODE_SERIAL_VT100) );
                items[choice] = (char*)deactivScreen;
            }
        } else if ( choice == 2 ) {
            // serial mode menu
        } else if ( choice == 3 ) {
            console.println("Console input Test > (q to quit) ");
            uint8_t ch;
            while( ( ch = console.getche() ) != 'q' ) { ; }
        } else if ( choice == 4 ) {
            // ...
        } else if ( choice == 5 ) {
            return false;
        } else if ( choice == -1 ) {
            return true;
        }
    }
    return false; // false, doesn't kill previous menu
}


bool videoTest() {
    int w = 64;
    int h = 64;
    // int tlen = (480-20)*(320-20); // mem overflow
    int tlen = w * h;
    uint16_t raster[ tlen ];
    for(int i=0; i < tlen; i++) { raster[i] = i % 3 == 0 ? screen.mapColor(15) : ( i % 3 == 1 ? screen.mapColor(5) : screen.mapColor(3) ); }

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


    screen.fillCircle( 100, 100, 30, 4 ); // 4 BLUE
    screen.drawCircle( 100, 100, 30, 5 ); // 5 YELLOW


    screen.loadBMPSpriteBoard( (char*) "Z/0/sprite1.bmp");
    screen.defineSprite(0, 0, 1, 19, 19); // upperL
    screen.defineSprite(1, 40, 1, 19, 19); //{ upperR }
    screen.defineSprite(2, 40,20, 19, 19); //{ lowerR }

    screen.drawSprite(0, 100, 100);
    screen.drawSprite(1, 100+19+3, 100);
    screen.drawSprite(2, 100+19+3+19+3, 100);

    screen.fillRect( 10, 10, w, h, raster );
    // while(true) delay(10000);

    delay(1000);

return false;
}
