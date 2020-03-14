#include "top.h"

/**
 * WiFi layer API impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include "Arduino.h"

#include "xts_string.h"

#include "xts_dev_fs.h"
extern Fs fileSystem;

#include "xts_dev_rgbled.h"
extern RGBLed led;

#include "xts_soft_console.h"
extern IOConsole console;


#include "xts_dev_wifi.h"

// ESP12 via Serial routines impl. 
#include "xts_dev_arduino_wifi_esp.h"

// Extra Software layer routines
#include "xts_soft_wifi.h"

#define LED_BEGIN_OP  { led.clr_green(); }
#define LED_END_OP    { led.off(); }
#define LED_FAILED_OP { led.clr_red(); }


WiFi::WiFi() {
    // ....
}

bool WiFi::setup() {
    LED_BEGIN_OP;
    if ( wifi_setup() ) {
        bool ok;
        ok = wifi_setWifiMode( WIFI_MODE_STA );
        ok &= wifi_testModule();
        if (ok) { LED_END_OP; }
        else { LED_FAILED_OP; }
        return ok;
    }
    LED_FAILED_OP;
    return false;
}

bool WiFi::resetAdapter() {
    LED_BEGIN_OP;
    bool ok = wifi_resetModule();
    if (ok) { LED_END_OP; }
    else { LED_FAILED_OP; }
    return ok;
}

bool WiFi::connectToAp(char* ssid, char* PSK) {
    LED_BEGIN_OP;
    bool ok = wifi_connectToAP(ssid, PSK);
    if (ok) { LED_END_OP; }
    else { LED_FAILED_OP; }
    return ok;
}

bool WiFi::connectToAp(int confNum) {
    LED_BEGIN_OP;
    bool ok = wifi_connectToAP(confNum);
    if (ok) { LED_END_OP; }
    else { LED_FAILED_OP; }
    return ok;
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
bool WiFi::isAtHome() {
    return wifi_isAtHome();
}

// returns HTTP code
// if apiKeyName startsWith "Authorization" : use value as direct HttpHeader
int WiFi::wget(char* host, int port, char* url, char* dest, int maxDestLen, char* apiKeyName) {
    LED_BEGIN_OP;
    char* headers = NULL;
    if ( apiKeyName != NULL ) {
        if ( startsWith(apiKeyName, (char*)"Authorization") ) {
            headers = apiKeyName;
        } else {
            headers = getHttpAuthorizationForAPI(apiKeyName);
        }
    }
    int rc = wifi_wget( host, port, url, dest, maxDestLen, headers);
    LED_END_OP;
    return rc;
}