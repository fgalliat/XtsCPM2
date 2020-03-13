#include "top.h"

/**
 * WiFi layer API impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include "Arduino.h"

#include "xts_string.h"

#include "xts_dev_fs.h"
extern Fs fs;

#include "xts_dev_rgbled.h"
extern RGBLed led;

#include "xts_soft_console.h"
extern IOConsole console;


#include "xts_dev_wifi.h"

// ESP12 via Serial routines impl. 
#include "xts_dev_arduino_wifi_esp.h"

// Extra Software layer routines
#include "xts_soft_wifi.h"

WiFi::WiFi() {
    // ....
}

bool WiFi::setup() {
    if ( wifi_setup() ) {
        bool ok;
        ok = wifi_setWifiMode( WIFI_MODE_STA );
        ok &= wifi_testModule();
        return ok;
    }
    return false;
}

bool WiFi::resetAdapter() {
    return wifi_resetModule();
}

bool WiFi::connectToAp(char* ssid, char* PSK) {
    return wifi_connectToAP(ssid, PSK);
}

bool WiFi::connectToAp(int confNum) {
    return wifi_connectToAP(confNum);
}

// \n separated + list ONLY known SSID even if not available...
char* WiFi::listAp() {
    return __WIFI_GET_KNWON_SSIDS();
}

char* WiFi::getIp() {
    return wifi_getIP();
}
char* WiFi::getSSID() {
    return wifi_getSSID();
}

// returns HTTP code
// if apiKeyName startsWith "Authorization" : use value as direct HttpHeader
int WiFi::wget(char* host, int port, char* url, char* dest, int maxDestLen, char* apiKeyName) {
    char* headers = NULL;
    if ( apiKeyName != NULL ) {
        if ( startsWith(apiKeyName, (char*)"Authorization") ) {
            headers = apiKeyName;
        } else {
            headers = getHttpAuthorizationForAPI(apiKeyName);
        }
    }
    int rc = wifi_wget( host, port, url, dest, maxDestLen, headers);
    return rc;
}