/**
 * Yatl&co. Wifi by ESP8266 (esp12) AT cmds Driver impl.
 * 
 * 
 * Xtase - fgalliat @Dec2019
 * 
 * 
 * LOOK AT : https://forum.pjrc.com/threads/27850-A-Guide-To-Using-ESP8266-With-TEENSY-3
 * NO MORE USES : https://github.com/bportaluri/WiFiEsp
 * 
 * 
 * STILL TODO
 *  connectToAp(SSID, ?PSK?) -> PSK : from file / from interactive KB read
 *  listAPs()
 *  getSSID()
 *  isAtHome()
 *  getHomeServerName()
 *  wget(server, port, query)
 * 
 * Cf z:/wifi.psk
 *  storeAP(ssid, psk)
 *  getAPpsk(ssid)
 *  setHome(ssid, local_home, remote_home)
 *  isAtHome()
 * 
 * Cf textFile(s)
 *  write(char* content)
 *  read() => char* => split(content, '\n', x)
 *  appendLine(char*) => appends line+"\n"
 * 
 * Cf input
 *  _kbreadLine() => char* (w/o CR/LF)
 *  may be based on _kbhit() + _getch() / _getche()
 * 
 * Make an interactive system
 *  prompt SSID, PSK => add it
 *  prompt SSID, PSK => change it
 */

// forwards
/*extern*/ int _kbhit();
/*extern*/ uint8_t _getch();
/*extern*/ uint8_t _getche();

    #define WIFI_SERIAL Serial5
    #define WIFI_CMD_TIMEOUT 6000
    #define WIFI_SERIAL_BAUDS 115200 

    #define DBUG_WIFI 0

    // STA : wifi AP client
    const int WIFI_MODE_STA = 1;
    // AP : wifi soft AP server
    const int WIFI_MODE_AP = 2;

// forwards
bool wifi_connectToAP(char* ssid, char* psk=NULL);
char* wifi_wget(char* host, int port, char* query, char* headers=NULL);
bool wifi_setWifiMode(int mode);
int wifi_getWifiMode();

    bool wifi_setup() { 
        WIFI_SERIAL.begin(WIFI_SERIAL_BAUDS); 

        unsigned long t0 = millis();
        unsigned long tmo = 1500;

        while( !WIFI_SERIAL ) {
            delay(2); // TODO : timeout
            if ( millis() - t0 > tmo ) { break; }
        }

        while(WIFI_SERIAL.available() == 0) {
            delay(50);
            if ( millis() - t0 > tmo ) { break; }
        }

        while(WIFI_SERIAL.available() > 0) {
            int ch = WIFI_SERIAL.read();
            // Serial.write(ch);
        }
        Serial.println("module powered");

        return true; 
    }

    void _wifiSendCMD(const char* cmd) {
        // add CRLF
        if (DBUG_WIFI) { Serial.print("WIFI >");Serial.println(cmd); }
        int tlen = strlen( cmd ) + 2; // +2 cf CRLF
        int mlen = WIFI_SERIAL.availableForWrite();
        if ( mlen < tlen ) {
            Serial.print("NotEnoughtAvailableForWrite !!!! => div");
            Serial.println(mlen);
            int max = mlen;
            char sub[max+1];
            for(int i=0; i < tlen-2; i+= max ) {
                memset(sub, 0x00, max+1);
                memcpy( sub, &cmd[i], min( max, (tlen-2-i) ) );
                Serial.print(">"); Serial.println(sub);
                WIFI_SERIAL.print( sub );
            }
        } else {
            WIFI_SERIAL.print( cmd );
        }

        WIFI_SERIAL.print( "\r\n" );
        WIFI_SERIAL.flush();

        yield();
        // Serial.println("Sent packet");
    }

// const int remainingBufferLen = (64*2);
const int remainingBufferLen = (512+64);
char remainingBuffer[remainingBufferLen+1];
bool remainingBufferInited = false;

    // will have few pbms w/ bin contents
    // removes CRLF
    // assumes that _line is 512+1 bytes allocated 
    int _wifiReadline(char* _line, unsigned int timeout=WIFI_CMD_TIMEOUT) {
        memset(_line, 0x00, 512+1);

        if (!remainingBufferInited) {
            memset(remainingBuffer, 0x00, remainingBufferLen+1);
            remainingBufferInited = true;
        }

        int waitForBufferNext = 0;

        if ( strlen(remainingBuffer) > 0 ) {
            int idx = indexOf(remainingBuffer, '\n');
            int tlen = strlen(remainingBuffer);
            if ( idx == -1 ) {
                Serial.println("Oups did not find LF, have to look further");
                // memcpy(_line, remainingBuffer, tlen);
                // waitForBufferNext = tlen;
            } else {
                // memcpy(_line, remainingBuffer, idx+1);
                memcpy(_line, remainingBuffer, idx-1); // remove CRLF
                int slen = tlen - (idx+1);
                memmove(&remainingBuffer[0], &remainingBuffer[idx+1], slen);
                memset(&remainingBuffer[slen], 0x00, idx+1);
                Serial.print("So, buffered at least >");
                Serial.print(_line);
                Serial.println("<");
                return idx+1;
            }
        }


        // Serial.println("::_wifiReadline()");

        // Serial.print("WIFI READ >");Serial.println(timeout);
        yield();

        unsigned long t0=millis();
        bool timReached = false;
        while (WIFI_SERIAL.available() <= 0) {
            if ( millis() - t0 >= timeout ) { timReached = true; break; }
            delay(10);
        }
        yield();

        if ( timReached ) { return -1; }

        int cpt = 0;
        int ch;
        t0=millis();
        char seg[64+1]; int avi,tor;
        while ( (avi = WIFI_SERIAL.available()) > 0) {
            if ( millis() - t0 >= timeout ) { timReached = true; break; }

            tor = avi;
            if (avi > 64) {
                Serial.println("Oups, will overflow");
                tor = 64;
            } else {
                Serial.print("So, will read ");
                Serial.println(tor);
            }

            memset(seg, 0x00, 64+1);
            int rr = WIFI_SERIAL.readBytes( seg, tor );
            if ( rr < tor ) {
                Serial.print("Oups tor=");
                Serial.print(tor);
                Serial.print(" rr=");
                Serial.println(rr);
            } else {
                Serial.print("So, read :");
                Serial.println(seg);
            }

            int idx;
            if ( (idx = indexOf(seg, '\n')) == -1 ) {
                Serial.println("Oups, did not find LF in read()");
                strcat( remainingBuffer, seg ); // (!!) w/ bin content
                waitForBufferNext += rr;
                continue;
            } else {
                // memcpy(&_line[0], seg, idx+1);
                memcpy(&_line[waitForBufferNext], seg, idx-1); // remove CRLF
                strcat( remainingBuffer, &seg[idx+1] ); // (!!) w/ bin content
                cpt = idx+1;

                Serial.print("So, found at least >");
                Serial.print(_line);
                Serial.println("<");

                return cpt;
            }

            // sprintf(_line, "%s", seg);
            // cpt = rr;

            /*

            ch = WIFI_SERIAL.read();
            if ( ch == -1 ) { timReached = true; break; }
            if ( ch == '\r' ) { 
                if (WIFI_SERIAL.available() > 0) {
                    if ( WIFI_SERIAL.peek() == '\n' ) {
                        continue; 
                    }
                }
                break;
            }
            if ( ch == '\n' ) { break; }
            _line[ cpt++ ] = (char)ch;
            */
        }
        yield();

        if ( _line[0] == 0x00 && timReached ) {
            return -1;
        }
        yield();

        if ( _line[0] == 0x00 ) { return 0; }

        int t = strlen(_line);
        if ( t < 0 ) { _line[0] = 0x00; return -1; }

        return t;
    }

    #define _RET_TIMEOUT 0
    #define _RET_OK 1
    #define _RET_ERROR 2

    int _wifi_waitForOk(char* dest=NULL) {
        char resp[512+1];
        while (true) {
            int readed = _wifiReadline(resp);

            yield();

            if ( readed == -1 ) { Serial.println("TIMEOUT--"); return _RET_TIMEOUT; }
            if ( strlen( resp ) > 0 ) {
                if (DBUG_WIFI) { Serial.print("-->"); Serial.println(resp); }
                
                if ( equals(&resp[0], (char*)"OK") ) { 
                    // Serial.println("OK--"); 
                    return _RET_OK; 
                }
                if ( equals(&resp[0], (char*)"ERROR") ) { 
                    // Serial.println("ERROR--"); 
                    return _RET_ERROR; 
                }

                if ( dest != NULL ) {
                    // copy the last non-empty line
                    sprintf(dest, "%s", resp);
                }

            } else {
                // Serial.println("--:EMPTY");
            }
        }
        yield();
        return -1;
    }

    bool wifi_testModule();


    // TODO : call it
    bool wifi_init() {
        unsigned long t0 = millis();
        if (DBUG_WIFI) { Serial.println("Waiting for Serial2"); }
        while(WIFI_SERIAL.available() > 0) {
            WIFI_SERIAL.read();
        }
        if (DBUG_WIFI) { Serial.println("Found some garbage"); }


        bool ok = false;
        // Serial.println("Reset Module");
        // yat4l_wifi_resetModule(); 
        
        if (DBUG_WIFI) { Serial.println("Test for Module"); }
        ok = wifi_testModule();
        if (DBUG_WIFI) { Serial.print("Tested Module : "); 
        Serial.println(ok ? "OK" : "NOK"); }

        if (DBUG_WIFI) { Serial.println("set mode for Module"); }
        ok = wifi_setWifiMode( WIFI_MODE_STA );
        if (DBUG_WIFI) { Serial.print("Module mode set : "); 
        Serial.println(ok ? "OK" : "NOK"); }

        int mode = wifi_getWifiMode();
        if (DBUG_WIFI) { Serial.print("Module mode : "); 
        Serial.println(mode); }

        char* ssids = __WIFI_GET_KNWON_SSIDS();
        Serial.println("Configured APs ...");
        Serial.println(ssids);

        if ( ssids != NULL ) {
            Serial.println("Select your AP (1 to 9)");
            int ch = -1;
            while( _kbhit() <= 0 ) {
                delay(5);
            }
            ch = _getch(); // no echo

            while( _kbhit() > 0  ) {
                _getch(); // read trailling chars
            }

            if ( ch != -1 ) {
                ch = ch - '1';
                if ( ch >= 0 ) {
                    char ssid[32+1]; memset(ssid, 0x00, 32+1);
                    char* vol = str_split(ssids, '\n', ch);
                    sprintf(ssid, "%s", vol);
                    Serial.println("Connecting to AP ...");
                    Serial.println(ssid);
                    ok = wifi_connectToAP(ssid);
                    // if (DBUG_WIFI) 
                    { Serial.print("Connected to AP : ");
                    Serial.println(ok ? "OK" : "NOK"); }
                }
            }
        }

        // Serial.println("Try to GET / @Home Server...");
        // // char* ignored = wifi_wget((char*)"$home", 8090, "/");
        // char* ignored = wifi_wget((char*)"$home", 8666, "/");
        // Serial.println( ignored );

        delay(3000);

        if (DBUG_WIFI) { Serial.println("Have finished !!!"); }

        ok = true;
        return ok;
    }

    bool wifi_testModule() { 
        _wifiSendCMD("AT"); 
        return _wifi_waitForOk() == _RET_OK;
    }

    bool wifi_resetModule() { 
        _wifiSendCMD("AT+RST"); 

        delay(300);

        unsigned long t0 = millis();
        Serial.println("Waiting for Serial2");
        while( !WIFI_SERIAL ) {
            delay(10);
            if ( millis() - t0 >= 1500 ) { return false; }
        }

        Serial.println("Check for garbage");

        t0 = millis();

        unsigned long timOut = 3500;

        yield();

        while( true ) {
            if ( millis() - t0 > timOut ) {break;}

            while(WIFI_SERIAL.available() == 0) {
                yield();
                delay(50);
                if ( millis() - t0 > timOut ) {break;}
            }

            yield();

            while(WIFI_SERIAL.available() > 0) {
                int ch = WIFI_SERIAL.read();
                if ( millis() - t0 > timOut ) {break;}
            }

            yield();

        }
        yield();

        Serial.println("Found some garbage");

        return true;
    }

    bool wifi_setWifiMode(int mode) {
        char cmd[32];
        sprintf(cmd, "AT+CWMODE=%d", mode);
        _wifiSendCMD(cmd);
        return _wifi_waitForOk() == _RET_OK;
    }

    int wifi_getWifiMode() { 
        _wifiSendCMD("AT+CWMODE?");
        char resp[128];
        bool ok = _wifi_waitForOk( resp ) == _RET_OK;
        int mode = -1;
        if ( ok && startsWith(resp, (char*)"+CWMODE:") ) {
            // Serial.println("Found a mode :");
            // Serial.println(resp);
            mode = atoi( &resp[8] );
        }
        return mode; 
    }

    bool wifi_isStaMode() {
      // TODO : better cf can be BOTH
      int wmode = wifi_getWifiMode();
      return wmode == WIFI_MODE_STA ||
      false;
      // wmode == WIFI_MODE_STA + WIFI_MODE_STA;
    }

    char* NOIP = (char*)"x.x.x.x";
    char CURIP[16+1];

    char* XX_getIP(bool STA) {
      if ( STA ) { _wifiSendCMD("AT+CIPSTA?"); }
      else { _wifiSendCMD("AT+CIPAP?"); }

      memset(CURIP, 0x00, 16+1);

      // char resp[256+1]; memset(resp, 0x00, 256+1);
      //   _wifi_waitForOk( resp )    --->> 256 is enought
      char resp[512+1]; // _wifiReadline(resp); requires 512 bytes long

        bool found = false;
        while (!found) {
            int readed = _wifiReadline(resp);
            
            if (readed < 0) {
                break;
            }

            // when not connected seems to finish with "+" (no netmask)
            if ( equals( resp, (char*)"+" ) ) {
                // Serial.println( "EJECT II" );
                break;
            }

            if ( startsWith(resp, (char*)"+CIP") ) {
                // ip: / gateway: / netmask:
                if ( contains(resp, (char*)"ip:") ) {
                    // +CIPSTA:ip:"0.0.0.0"
                    char* subResult = str_split(resp, '"', 1);
                    if ( subResult == NULL ) {
                        sprintf(CURIP, "%s", NOIP);
                    } else {
                        sprintf(CURIP, "%s", subResult);
                        free(subResult);
                    }
                    found = true;
                    break;
                }
            }
        }

        // must not return an function-local pointer
      return CURIP;
    }


    // --------------------

    char* NOSSID = (char*)"NotConnected";
    char CURSSID[32+1];

    char* XX_getSSID(bool STA) {
      if ( STA ) { _wifiSendCMD("AT+CWJAP?"); }
      else { _wifiSendCMD("AT+CWSAP?"); }

      memset(CURSSID, 0x00, 32+1);

      char resp[512+1]; // _wifiReadline(resp); requires 512 bytes long

        bool found = false;
        while (!found) {
            int readed = _wifiReadline(resp);
            
            if (readed < 0) {
                break;
            }

            // when not connected seems to finish with "+" (no netmask)
            if ( equals( resp, (char*)"+" ) ) {
                // Serial.println( "EJECT II" );
                break;
            }

            if ( equals( resp, (char*)"OK" ) ) {
                break;
            }

            if ( equals( resp, (char*)"ERROR" ) ) {
                return NOSSID;
            }

            if ( startsWith(resp, (char*)"+CW") ) {
                    // +CWJAP:__?__"MySSID"
                    char* subResult = str_split(resp, '"', 1);
                    if ( subResult == NULL ) {
                        sprintf(CURSSID, "%s", NOIP);
                    } else {
                        sprintf(CURSSID, "%s", subResult);
                        free(subResult);
                    }
                    found = true;
                    break;
            }
        }

        // must not return an function-local pointer
      return CURSSID;
    }

    // --------------------

    char* wifi_getIP() { return XX_getIP( wifi_isStaMode() ); }
    char* wifi_getSSID() { return XX_getSSID( wifi_isStaMode() ); }

    // --------------------

    // bool yat4l_wifi_close() { return true; }
    // bool yat4l_wifi_beginAP() { return false; }
    // bool yat4l_wifi_startTelnetd() { return false; }

    // bool yat4l_wifi_loop() { return false; }

    // void yat4l_wifi_telnetd_broadcast(char ch)  { ; }
    // int  yat4l_wifi_telnetd_available()  { return 0; }
    // int  yat4l_wifi_telnetd_read() { return -1; }


    // Soft AP
    bool wifi_openAnAP(char* ssid, char* psk) { return false; }

    // STA (client of an AP)
    bool wifi_connectToAP(char* ssid, char* psk) { 
        if ( psk == NULL ) {
            psk = __WIFI_GET_PSK(ssid);
        }

        if ( psk == NULL ) {
            dbug("No PSK provided for that SSID");
            return false;
        }

        char cmd[96+1]; memset(cmd, 0x00, 96+1);
        sprintf(cmd, "AT+CWJAP=\"%s\",\"%s\"", ssid, psk);
        _wifiSendCMD(cmd);

        return _wifi_waitForOk() == _RET_OK;
    }

    bool wifi_disconnectFromAP() { return false; }

    // returns a 'ssid \n ssid \n ....'
    char* wifi_scanAPs() { return (char*)""; }


    void wifi_closeSocket() {
        _wifiSendCMD("AT+CIPCLOSE()");
        if ( ! _wifi_waitForOk() ) { return; }
    }

    // return type is not yet certified, may use a packetHandler ....
    // ex. yat4l_wifi_wget("www.google.com", 80, "/search?q=esp8266" 
    // ex. yat4l_wifi_wget("$home", 8089, "/login?username=toto&pass=titi" 
    char* wifi_wget(char* host, int port, char* query, char* headers) {

      char* usedHOST = host;

      if ( equals(host, (char*)"$home") ) {
        char* homeSrv = wifi_getHomeServer();
        if ( homeSrv != NULL ) {
            usedHOST = homeSrv;
        }          
      }

      char cmd[128];
      sprintf(cmd, "AT+CIPSTART=\"TCP\",\"%s\",%d", usedHOST, port);
      Serial.println(cmd);

      _wifiSendCMD(cmd);
      if (! _wifi_waitForOk() ) {
          wifi_closeSocket();
          return NULL;
      }
      Serial.println("OK");

      char resp[512+1]; // _wifiReadline(resp); requires 512 bytes long
      char fullQ[128+128+32];
      sprintf( fullQ, "GET %s HTTP/1.1\r\n", query );
      if ( headers != NULL ) {
        //   strcat(fullQ, headers);
          sprintf( fullQ, "%s%s\r\n", fullQ, headers );
      }
      sprintf( fullQ, "%s\r\n", fullQ );

      // TODO : add Authorization, Bearer .....

      sprintf(cmd, "AT+CIPSEND=%d", strlen( fullQ ));
      _wifiSendCMD( cmd );
      _wifiReadline(resp);
    //   Serial.println(cmd);

    //   Serial.println(fullQ);
      sprintf( fullQ, "%s+++", fullQ );
      _wifiSendCMD( fullQ );
    //   _wifiReadline(resp);
    //   Serial.println(resp);
      

    //   Serial.println("+++");
    //   _wifiSendCMD("+++"); // EOT
      _wifiReadline(resp);


        bool found = false;
        while (!found) {
            int readed = _wifiReadline(resp);
            
            if (readed < 0) {
                wifi_closeSocket();
                return NULL;
            }

            if ( equals( resp, (char*)"ERROR" ) ) {
                wifi_closeSocket();
                return NULL;
            } else if ( equals( resp, (char*)"SEND OK" ) ) {
                break;
            }

            Serial.print("send>");
            Serial.print(resp);
            Serial.println("<");
        }
        Serial.println("SEND OK");


        found = false;
        while (!found) {
            int readed = _wifiReadline(resp);
            
            if (readed < 0) {
                wifi_closeSocket();
                return NULL;
            }

            if ( equals( resp, (char*)"CLOSED" ) ) {
                wifi_closeSocket();
                break;
            } else if ( endsWith( resp, (char*)"CLOSED" ) ) {
                Serial.println( resp );
                break;
            }

            Serial.print( "rcv>" );
            Serial.print( resp );
            Serial.println( "<" );
        }
        Serial.println("READ OK");


        wifi_closeSocket();


        // must not return an function-local pointer
      return NULL;
    }


    // // return type is not yet certified, may use a packetHandler ....
    // // ex. yat4l_wifi_wget("www.google.com", 80, "/search?q=esp8266" 
    // // ex. yat4l_wifi_wget("$home", 8089, "/login?username=toto&pass=titi" 
    // char* yat4l_wifi_wget(char* host, int port, char* query) {
    //     return NULL;
    // }

    // bool yat4l_wifi_isAtHome() { return false; }
    // char* yat4l_wifi_getHomeServer() { return NULL; }


