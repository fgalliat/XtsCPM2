#include "xts_string.h"

#define dbug Serial.println

char* wifi_getHomeServer() {
    return (char*)"myserver";
}

char* __WIFI_GET_PSK(char* ssid) {
    return (char*)"MyPSK";
}

char* __WIFI_GET_KNWON_SSIDS() {
    return (char*)"MyBox";
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

    wifi_setup();

}


void loop() {

    Serial.println( "loop" );
    delay(2000);

}