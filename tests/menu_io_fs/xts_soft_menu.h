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


bool wifiMenu() {
    char* title = (char*)"[ WiFi Main Menu ]";

    int nbItems = 6;
    char* items[nbItems] = {
        //      12345678901234567890123456789012
        (char*)"Activate WiFi  ",
        (char*)"Connect AP ->",
        (char*)"Test WGet temp",
        (char*)"Test WGet rss",
        (char*)"",
        (char*)"Exit",
    };

    int choice = -1;
    while ( choice != nbItems-1 ) {
        choice = console.menu(title, items, nbItems);
        
        if ( choice == 0 ) {
            // if ( wifi is activated ) ...
            items[choice] = (char*)"DeActivate WiFi";
        } else if ( choice == 1 ) {
            // ssid sub menu
        } else if ( choice == 2 ) {
            // wget temp
        } else if ( choice == 3 ) {
            // wget rss
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



