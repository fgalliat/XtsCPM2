/**
 * YATDB WiFi Layer
 * 
 * Xtase - fgalliat @Mar 2020
 */

class WiFi {
    private:

    public:
        WiFi();
        bool setup();

        bool resetAdapter();

        bool connectToAp(int confNum);
        bool connectToAp(char* ssid, char* PSK=NULL);
        // bool startSoftAP(..)

        // \n separated ?
        char* listAp();

        char* getIp();
        char* getSSID();

        // char* ggetHomeServer();
        // bool isAtHome();

        // returns HTTP code
        // if apiKeyName startsWith "Authorization" : use value as direct HttpHeader
        int wget(char* host, int port, char* url, char* dest, int maxDestLen, char* apiKeyName=NULL);
};