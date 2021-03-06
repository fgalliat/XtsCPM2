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
 * known issue : wget() w/ more than 1 +IPD packet ....
 */

// forwards external
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

    // forwards internal
    bool wifi_connectToAP(char* ssid, char* psk=NULL);
    int wifi_wget(char* host, int port, char* query, char* dest, int maxLength, char* headers=NULL);
    bool wifi_setWifiMode(int mode);
    int wifi_getWifiMode();
    bool wifi_testModule();

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
            #if DBUG_WIFI
            Serial.print("NotEnoughtAvailableForWrite !!!! => div");
            Serial.println(mlen);
            #endif
            int max = mlen;
            char sub[max+1];
            for(int i=0; i < tlen-2; i+= max ) {
                memset(sub, 0x00, max+1);
                memcpy( sub, &cmd[i], min( max, (tlen-2-i) ) );
#if DBUG_WIFI
                Serial.print(">"); Serial.println(sub);
#endif
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
    int _wifiReadline(char* _line, unsigned int timeout=WIFI_CMD_TIMEOUT, const char* HALT_ON=NULL) {
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
                #if DBUG_WIFI
                Serial.println("Oups did not find LF, have to look further");
                #endif
            } else {
                // memcpy(_line, remainingBuffer, idx+1);
                memcpy(_line, remainingBuffer, idx-1); // remove CRLF
                int slen = tlen - (idx+1);
                memmove(&remainingBuffer[0], &remainingBuffer[idx+1], slen);
                memset(&remainingBuffer[slen], 0x00, idx+1);
                #if DBUG_WIFI
                Serial.print("So, buffered at least >");
                Serial.print(_line);
                Serial.println("<");
                #endif
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

        bool foundSomeBytesToRead = false;

        while( true ) {

            bool foundCRLF = false;
            bool foundHALTON = false;
            bool overflowed = false;

            if ( millis() - t0 >= timeout ) { timReached = true; break; }

            while ( (avi = WIFI_SERIAL.available()) > 0) {

                foundSomeBytesToRead = true;

                tor = avi;
                if (avi > 64) {
                    Serial.println("Oups, will overflow");
                    tor = 64;
                } else {
                    #if DBUG_WIFI
                    Serial.print("So, will read ");
                    Serial.println(tor);
                    #endif
                }

                memset(seg, 0x00, 64+1);
    #if 0
                int rr = WIFI_SERIAL.readBytes( seg, tor );
    #else

                int haltOnCpt=0;
                for(int i=0; i < tor; i++) {
                    while( WIFI_SERIAL.available() <= 0 ) {}
                    seg[i] = WIFI_SERIAL.read();
                    if ( HALT_ON != NULL ) {
                        if ( seg[i] == HALT_ON[haltOnCpt++] ) {
                            if ( haltOnCpt >= strlen(HALT_ON) ) {
                                foundHALTON = true;
                                break;
                            }
                        } else {
                            haltOnCpt = 0;
                        }
                    }
                }
                int rr = tor;
                if ( foundHALTON ) { 
                    strcat( remainingBuffer, seg );
                    break; 
                }

    #endif
                if ( rr < tor ) {
                    Serial.print("Oups tor=");
                    Serial.print(tor);
                    Serial.print(" rr=");
                    Serial.println(rr);
                } else {
                    #if DBUG_WIFI
                    Serial.print("So, read :");
                    Serial.println(seg);
                    #endif
                }

                if ( strlen(remainingBuffer) + strlen(seg) > remainingBufferLen ) {
                    Serial.println("wget() Overflowed !! [1]");
                    // TODO copy @least whats possible
                    overflowed = true;
                    break;
                }

                strcat( remainingBuffer, seg );

                if ( indexOf(seg, '\n') > -1 ) {
                    foundCRLF = true;
                }

            } // eof available loop
            yield();

            if ( foundHALTON ) {
                #if DBUG_WIFI
                Serial.println("Found HALTON");
                #endif
                break;
            }

            if ( overflowed || strlen(remainingBuffer) > remainingBufferLen ) {
                Serial.println("wget() Overflowed !! [2]");
                break;
            }

            if ( avi <= 0 && foundCRLF ) {
                #if DBUG_WIFI
                Serial.println( "found a line & no more to read" );
                #endif
                break;
            }

            if ( avi <= 0 && timReached ) {
                return -1;
            }

            if ( timReached ) {
                Serial.println( "timReached" );
                break;
            }

        } // end of while true
        #if DBUG_WIFI
        Serial.println( "end of loop" );
        #endif
        if ( strlen(remainingBuffer) > 0 ) {
            int idx = indexOf(remainingBuffer, '\n');
            int tlen = strlen(remainingBuffer);
            int idx2;
            if ( idx == -1 ) {
                #if DBUG_WIFI
                Serial.println("Oups did not find LF, have to come back later");
                #endif
                idx = min(512, strlen(remainingBuffer));
                idx2 = idx;
            } else {
                idx--; // remove CRLF
                idx2 = idx+2;
            } 
            
            memcpy(_line, remainingBuffer, idx);
            int slen = tlen - (idx2);
            memmove(&remainingBuffer[0], &remainingBuffer[idx2], slen);
            memset(&remainingBuffer[slen], 0x00, idx2);
            #if DBUG_WIFI
            Serial.print("So, buffered at least >");
            Serial.print(_line);
            Serial.println("<");
            #endif
            return strlen(_line);
            
        }

       return -1;
    }

    #define _RET_TIMEOUT 0
    #define _RET_OK 1
    #define _RET_ERROR 2

    int _wifi_waitForOk(char* dest=NULL, int timeout=WIFI_CMD_TIMEOUT) {
        char resp[512+1];
        while (true) {
            int readed = _wifiReadline(resp, timeout);

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

        // int mode = wifi_getWifiMode();
        // if (DBUG_WIFI) { Serial.print("Module mode : "); 
        // Serial.println(mode); }

        char* ssids = __WIFI_GET_KNWON_SSIDS();
        Serial.println("Configured APs ...");
        Serial.println(ssids);

        if ( ssids != NULL ) {
            Serial.println("Select your AP (1 to 9)");
            int ch = -1;
            // while( _kbhit() <= 0 ) {
            //     delay(5);
            // }
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
                    Serial.print("Connecting to AP : ");
                    Serial.println(ssid);
                    ok = wifi_connectToAP(ssid);
                    // if (DBUG_WIFI) 
                    { Serial.print("Connected to AP : ");
                    Serial.println(ok ? "OK" : "NOK"); }
                }
            }
        }

        // delay(3000);

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

        return _wifi_waitForOk(NULL, 12000) == _RET_OK;
    }

    bool wifi_disconnectFromAP() { return false; }

    // returns a 'ssid \n ssid \n ....'
    char* wifi_scanAPs() { return (char*)""; }


    void wifi_closeSocket() {
        // _wifiSendCMD("AT+CIPCLOSE()"); // was a bug
        _wifiSendCMD("AT+CIPCLOSE");
        if ( ! _wifi_waitForOk() ) { return; }
    }

    const int MAX_IPD_BLOC_LEN = 2048;

    // return type is not yet certified, may use a packetHandler ....
    // ex. wifi_wget("www.google.com", 80, "/search?q=esp8266" 
    // ex. wifi_wget("$home", 8089, "/login?username=toto&pass=titi" 
    // TODO : return HTTP reposnse code
    int wifi_wget(char* host, int port, char* query, char* dest, int maxLength, char* headers) {
      int httpResponseCode = -1;

      char* usedHOST = host;

      if ( equals(host, (char*)"$home") ) {
        char* homeSrv = wifi_getHomeServer();
        if ( homeSrv != NULL ) {
            usedHOST = homeSrv;
        }          
      }

      char cmd[128];
      sprintf(cmd, "AT+CIPSTART=\"TCP\",\"%s\",%d", usedHOST, port);
      #if DBUG_WIFI
      Serial.println(cmd);
      #endif

      _wifiSendCMD(cmd);
      if (! _wifi_waitForOk() ) {
          wifi_closeSocket();
          return -1;
      }
      #if DBUG_WIFI
      Serial.println("OK");
      #endif

      char resp[512+1]; // _wifiReadline(resp); requires 512 bytes long
      char fullQ[512+32];
      sprintf( fullQ, "GET %s HTTP/1.1\r\n", query );

      sprintf( fullQ, "%sHost: %s\r\n", fullQ, usedHOST );

      if ( headers != NULL ) {
        //   strcat(fullQ, headers);
          sprintf( fullQ, "%s%s\r\n", fullQ, headers );
      }
      sprintf( fullQ, "%s\r\n", fullQ );

      sprintf(cmd, "AT+CIPSEND=%d", strlen( fullQ ));
      _wifiSendCMD( cmd );
      _wifiReadline(resp);
      sprintf( fullQ, "%s+++", fullQ );
      _wifiSendCMD( fullQ );
      _wifiReadline(resp);


        bool found = false;
        while (!found) {
            int readed = _wifiReadline(resp, WIFI_CMD_TIMEOUT/3, "\r\nSEND OK\r\n");
            
            if (readed < 0) {
                wifi_closeSocket();
                return -1;
            }

            if ( equals( resp, (char*)"ERROR" ) ) {
                wifi_closeSocket();
                return -1;
            } else if ( equals( resp, (char*)"SEND OK" ) ) {
                break;
            }

#if DBUG_WIFI
            Serial.print("send>");
            Serial.print(resp);
            Serial.println("<");
#endif
        }
#if DBUG_WIFI
        Serial.println("SEND OK");
#endif
        char traillingCRLF[2+1]; memset(traillingCRLF, 0x00, 2+1);
        int readed = WIFI_SERIAL.readBytes(traillingCRLF, 2);

        found = false; // never set to true ...
        long rt0 = millis();
        bool alreadyFoundAnIpdPacket = false;
        while (!found) {

            // +IPD,xxx:<...>
            char ipd[4+1]; memset(ipd, 0x00, 4+1);
            int readed = WIFI_SERIAL.readBytes(ipd, 4);
            if ( readed <= 0 ) {
                if ( alreadyFoundAnIpdPacket && millis() - rt0 > 500 ) {
                    // Serial.println("HTTP-RESP -eof-");
                    break;
                }
                if ( millis() - rt0 > 6000 ) {
                    Serial.println("HTTP-RESP timeout");
                    break;
                }
            } else {
                #if DBUG_WIFI
                Serial.print("found ");
                Serial.print( readed );
                Serial.println(" bytes to read.");
                Serial.println(ipd);
                #endif

                if ( readed == 4 ) {

                    if ( indexOf(ipd, '+') > 0 ) {
                        // found a + but garbage before...
                        int idxP = indexOf(ipd, '+');
                        memmove(&ipd[0], &ipd[idxP], 4-idxP);
                        int cptP = 4-idxP;
                        for(int i=idxP; i < 4; i++) {
                            while( WIFI_SERIAL.available() <= 0 ) {;}
                            ipd[cptP++] =  WIFI_SERIAL.read();
                        }
                        #if DBUG_WIFI
                        Serial.println("TRIED to restore a bloc");
                        Serial.println(ipd);
                        #endif
                    }

                    if ( equals( ipd, "+IPD" ) ) {
                        #if DBUG_WIFI
                        Serial.println("Start reading +IPD bloc ");
                        #endif
                        while( WIFI_SERIAL.available() <= 0 ) {;}
                        char ch;
                        ch = WIFI_SERIAL.read(); // ','
                        // Serial.print("##"); Serial.write(ch);
                        char ipdLen[8+1]; memset(ipdLen, 0x00, 8+1);
                        int ipdLenCpt = 0;
                        while( WIFI_SERIAL.available() <= 0 ) {;}
                        while( (ch = WIFI_SERIAL.read()) != ':' ) {
                            // Serial.print("#)"); Serial.write(ch);
                            ipdLen[ipdLenCpt++] = ch;
                            if ( ipdLenCpt >= 8 ) { break; }
                            while( WIFI_SERIAL.available() <= 0 ) {;}
                        }
                        int ipdLenI = atoi(ipdLen);
                        #if DBUG_WIFI
                        Serial.print( "found a bloc of " );
                        Serial.print( ipdLenI );
                        Serial.println( " bytes" );
                        #endif

                        if ( ipdLenI > MAX_IPD_BLOC_LEN ) {
                            Serial.println("(!!) THE BLOC IS TOO BIG ");
                            // TODO : store what i can
                            // read remaining to flush input
                        } else {
                            char buff[ipdLenI+1];
                            memset(buff, 0x00, ipdLenI+1);

                            while( WIFI_SERIAL.available() <= 0 ) {;}
                            int readed = WIFI_SERIAL.readBytes( buff, ipdLenI );

                            if ( !alreadyFoundAnIpdPacket ) {
                                // try to read HTTP REPONSE CODE
                                if ( startsWith(buff, (char*)"HTTP") ) {
                                    int spIdx = indexOf(buff, ' ');
                                    int crIdx = indexOf(buff, '\r');
                                    if ( crIdx > -1 ) {
                                        spIdx = crIdx - (3+1); // How... dirty 
                                        char responseCodeStr[3+1];
                                        int cpt=0;
                                        memset(responseCodeStr, 0x00, 3+1);
                                        for(int i=spIdx; i < crIdx; i++) {
                                            responseCodeStr[cpt++] = buff[i];
                                            if ( cpt >= 3 ) { break; }
                                        }
                                        httpResponseCode = atoi(responseCodeStr);
                                        #if DBUG_WIFI
                                        Serial.print("HTTP RESP CODE : ");
                                        Serial.println(responseCodeStr);
                                        Serial.println(httpResponseCode);
                                        #endif
                                    }
                                } // startsWith HTTP/1.1 200

                                // try to read HTTP REPONSE W/O Headers
                                const char* headerEndSeq = "\r\n\r\n";
                                int headerEndSeqCpt = 0;
                                int endOfHeaderPos = 0;
                                for( int i = 0; i < ipdLenI; i++ ) {
                                    if ( buff[i] == headerEndSeq[headerEndSeqCpt] ) {
                                        headerEndSeqCpt++;
                                        if ( headerEndSeqCpt >= 4 ) {
                                            endOfHeaderPos = i+1;
                                            break;
                                        }
                                    } else {
                                        headerEndSeqCpt=0;
                                    }
                                }

                                if (endOfHeaderPos < ipdLenI) {
                                    int maxLen = min(maxLength, ipdLenI);
                                    memcpy( dest, &buff[endOfHeaderPos], maxLen );
                                }
                            } // eoif alreadyIPD

                            #if DBUG_WIFI
                            Serial.println("========================");
                            Serial.println( buff );
                            Serial.println("========================");
                            Serial.print( readed );
                            Serial.print( " on " );
                            Serial.println( ipdLenI );
                            Serial.println("========================");
                            #endif

                            if( WIFI_SERIAL.available() == 2 ) {
                                // CR LF
                                WIFI_SERIAL.read();
                                WIFI_SERIAL.read();
                            }
                        }
                        alreadyFoundAnIpdPacket = true;
                        rt0 = millis();
                    } else {
                        Serial.println("Not a +IPD bloc ");

                        // just for now ...
                        while( WIFI_SERIAL.available() > 0 ) {
                            WIFI_SERIAL.read();
                        }
                    }
                }

            }

        }
        #if DBUG_WIFI
        Serial.println("READ OK");
        #endif

        char closingSeq[512+1];
        readed = _wifiReadline(closingSeq, 500);

        if ( !endsWith(closingSeq, (char*)"AT+CIPCLOSE") ) {
            #if DBUG_WIFI
            Serial.println("Some bytes remainging...");
            Serial.println(closingSeq);
            #endif
        }
        // don't mind about error ...
        wifi_closeSocket();

      return httpResponseCode;
    }



