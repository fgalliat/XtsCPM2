/*
 Xtase - fgalliat @Mar 2020
 ESP12 AT lib demo

 known issue w/ more than 1 +IPD packet ....
*/

#include "connect.h"

// ==================================================
// SD FS Section
#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

// SdFatSdio SD;
SdFat SD;

bool fs_setup() {
    if ( ! SD.begin(BUILTIN_SDCARD) ) {
        return false;
    }
    return true;
}
// ==================================================

#include "xts_string.h"
#include "xts_arduino_dev_fs.h"

#define dbug Serial.println

#ifndef HEADERS
 #define HEADERS "Authorization: Bearer eyJhbGciOi"
#endif

char* wifi_getHomeServer() {
    #ifdef HOME_SERVER
    return (char*)HOME_SERVER;
    #else
    return (char*)"myserver";
    #endif
}

char* __WIFI_GET_PSK(char* ssid) {
    #ifdef PSK
    return (char*)PSK;
    #else
    return (char*)"MyPSK";
    #endif
}

char* __WIFI_GET_KNWON_SSIDS() {
    #ifdef SSID
    return (char*)SSID;
    #else
    return (char*)"MyBox";
    #endif
}

int _kbhit() { return Serial.available(); }
uint8_t _getch() {
    while (_kbhit() == 0)
    {
        delay(5);
    }
    return (uint8_t)Serial.read();
}
uint8_t _getche() {
    while (_kbhit() == 0)
    {
        delay(5);
    }
    int c = Serial.read();
    Serial.write( (char)c );
    return (uint8_t)c;
}



#include "xts_arduino_dev_wifi_esp_at.h"



void setup() {
    Serial.begin(115200);

    while(!Serial) {}

    if ( !fs_setup() ) {
        Serial.println("Fs init error");
        Serial.println("Halting");
        while(true) delay(20000);
    }

    Serial.println( fs_getAssetsFileEntry( (char*)"ishar.pak") );
    Serial.println( fs_getAssetsFileEntry( (char*)"y:pack1.pak") );
    Serial.println( fs_getDiskFileName( (char*)"D:") );
    Serial.println( fs_getDiskFileName( (char*)"D:zork.com") );

/*
    Serial.println( "setup" );
    wifi_setup();

    Serial.println( "reset" );
    wifi_resetModule();

    Serial.println( "init" );
    wifi_init();
*/
}


void loop() {
    Serial.println( "loop" );
    while(true) delay(20000);

    char* api = (char*)"/sensors/sensor/1";
    char* ignored = wifi_wget((char*)"$home", 8000, api, (char*)HEADERS);
    Serial.println( ignored );

    api = (char*)"/rss/titles/1/arduino";
    wifi_wget((char*)"$home", 8000, api, (char*)HEADERS);

    // get +IPD ... in previous packet !!!!! => but w/ multiple packets
    // wifi_wget("arduino.cc", 80, "/asciilogo.txt", NULL);

Serial.println( "-- EOF --" );

    while(true) delay(20000);

}