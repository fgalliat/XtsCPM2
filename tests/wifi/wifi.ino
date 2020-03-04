#include "connect.h"


#include "xts_string.h"

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

    Serial.println( "setup" );
    wifi_setup();

    Serial.println( "reset" );
    wifi_resetModule();

    Serial.println( "init" );
    wifi_init();

}


void loop() {

    // Serial.println( "loop" );

    // char* api = (char*)"'http://192.168.1.134:8666/sensors/sensor/1'";
    char* api = (char*)"/sensors/sensor/1";

    char* ignored = wifi_wget((char*)"$home", 8666, api, (char*)HEADERS);
    Serial.println( ignored );

    // MUST HALT on "SEND OK", too fast, get +IPD ...
    wifi_wget("arduino.cc", 80, "/asciilogo.txt", NULL);

Serial.println( "-- EOF --" );

    while(true) delay(20000);

}