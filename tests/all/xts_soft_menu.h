/**
 * Xtase Main & Sub menus definition
 * 
 * 
 * Xtase - fgalliat @Mar2020
 */

// sub menus returns true if need to kill caller menu
bool audioMenu();
bool wifiMenu();
bool consoleMenu();

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
        (char*)"Audio   ->",
        (char*)"WiFi    ->",
        (char*)"Console ->",
        (char*)"WiFi menu",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems, x1, y1, x2, y2, clearBehindWindow);
        
        // console.print( "Ya choosed : " );
        // console.println( choice );

        if ( choice == 0 ) {
            // if ( audioMenu() ) { break; }
            audioMenu();
        } else if ( choice == 1 ) {
            wifiMenu();
        } else if ( choice == 2 ) {
            consoleMenu();
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

    int homePort = 8000;
    if ( ! wifi.isAtHome() ) {
        homePort = 8090;
    }
    char* apiKeyName = (char*)"sensors";

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);
        
        if ( choice == 0 ) {
            // if ( wifi is activated ) ...
            items[choice] = (char*)"DeActivate WiFi";
        } else if ( choice == 1 ) {
            // reset wifi
            wifi.resetAdapter();
        } else if ( choice == 2 ) {
            // ssid sub menu
            // temp code !!! 
            bool ok = wifi.connectToAp(0);
            if ( !ok ) {
                console.warn((char*)"Not connected !");
            } else {
                console.print( "IP : " );
                console.println( wifi.getIp() );
                console.print( "SSID : " );
                console.println( wifi.getSSID() );
            }
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

const char* activSerial = "Activate Serial  ";
const char* deactivSerial = "DeActivate Serial  ";

const char* activScreen = "Activate Screen  ";
const char* deactivScreen = "DeActivate Screen  ";
bool conSerialRaw = false;

bool consoleMenu() {
    char* title = (char*)"[ Console Menu ]";

    int nbItems = 5;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)activSerial,
        (char*)deactivScreen,
        (char*)"Serial mode ->",
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
            // ...
        } else if ( choice == 4 ) {
            return false;
        } else if ( choice == -1 ) {
            return true;
        }
    }
    return false; // false, doesn't kill previous menu
}



