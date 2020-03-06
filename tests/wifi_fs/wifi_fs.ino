/*
 Xtase - fgalliat @Mar 2020
 ESP12 AT lib demo + Fs Support for WiFi config & REST API key

 known issue : wget() w/ more than 1 +IPD packet ....
*/

#include "xts_string.h"

// ==================================================
// SD FS Section
#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

// SdFatSdio SD;
SdFat SD;

#include "xts_arduino_dev_fs.h"


bool fs_setup() {
    if ( ! SD.begin(BUILTIN_SDCARD) ) {
        return false;
    }
    return true;
}

// ==================================================
// Console Section

#define dbug Serial.println

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

// ==================================================
// Network Section

// forward
bool wifi_isAtHome(bool refresh=false);
char* wifi_getHomeServer(bool refresh=false);
char* __WIFI_GET_PSK(char* ssid);
char* __WIFI_GET_KNWON_SSIDS();

#include "xts_arduino_dev_wifi_esp_at.h"
#include "xts_arduino_soft_wifi.h"

// ============================================

void setup() {
    Serial.begin(115200);

    while(!Serial) {}

    if ( !fs_setup() ) {
        Serial.println("Fs init error");
        Serial.println("Halting");
        while(true) delay(20000);
    }

    // Serial.println( fs_getAssetsFileEntry( (char*)"ishar.pak") );
    // Serial.println( fs_getAssetsFileEntry( (char*)"y:pack1.pak") );
    // Serial.println( fs_getDiskFileName( (char*)"D:") );
    // Serial.println( fs_getDiskFileName( (char*)"D:zork.com") );

    // char* wifiPsksFile = fs_getDiskFileName( (char*)"Z:WIFI.PSK");
    // char wifiPsksConf[ 1024+1 ]; wifiPsksConf[1024]=0x00;
    // int read = fs_readTextFile(wifiPsksFile, wifiPsksConf, 1024);
    // // Serial.println( wifiPsksConf );


    Serial.println( "WiFi setup" );
    if ( ! wifi_setup() ) {
        Serial.println("WiFi init error");
        Serial.println("Halting");
        while(true) delay(20000);
    }

    // Serial.println( "reset" );
    // wifi_resetModule();


    Serial.println( "WiFi init" );
    wifi_init();

}


void loop() {
    Serial.println( "loop" );

    const int maxHttpResponseLen = 1024;
    char httpResponse[maxHttpResponseLen+1];

    char* HEADERS = getHttpAuthorizationForAPI( (char*) "sensors");
    // Serial.println("Authorization:");
    // Serial.println(HEADERS);

    int port = 8000;
    if ( !wifi_isAtHome() ) {
        port = 8090;
    }
    Serial.println("Socket port:");
    Serial.println(port);

    // Serial.println( "halt" ); while(true) delay(20000);

    char* api = (char*)"/sensors/sensor/1";
    memset(httpResponse, 0x00, maxHttpResponseLen+1);
    int httpCode = wifi_wget((char*)"$home", port, api, httpResponse, maxHttpResponseLen, (char*)HEADERS);
    Serial.print( "HTTP-CODE:" );
    Serial.println( httpCode );
    Serial.println( "__________________________" );
    Serial.println( httpResponse );
    Serial.println( "__________________________" );

    api = (char*)"/rss/titles/1/arduino";
    memset(httpResponse, 0x00, maxHttpResponseLen+1);
    httpCode = wifi_wget((char*)"$home", port, api, httpResponse, maxHttpResponseLen, (char*)HEADERS);
    Serial.println( httpResponse );

    // get +IPD ... in previous packet !!!!! => but w/ multiple packets
    // wifi_wget("arduino.cc", 80, "/asciilogo.txt", NULL);

    Serial.println( "-- EOF --" );
    while(true) delay(20000);

}