#include "top.h"
/**
 * 
 * YATDB FileSystem API impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */
#include <Arduino.h>


#include "xts_string.h"

#include "xts_dev_fs.h"

#include "xts_dev_rgbled.h"
extern RGBLed led;

#include "xts_soft_console.h"
extern IOConsole console;

// ===== Hardware section =====
#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

FS_CLASS SD;
// ===== Hardware section =====

const int __tmpDiskFileNameLen = 4+8+1+3+1;
char __tmpDiskFileName[__tmpDiskFileNameLen]; 

Fs::Fs() {
}

bool Fs::setup() {
    // return SD.begin(BUILTIN_SDCARD);
    return SD.begin( SdioConfig(FIFO_SDIO) );
    // return SD.begin();
}

char* Fs::getDiskFileName(char* diskFileName) {
    if ( indexOf(diskFileName, ':') == -1 ) {
        // ex. foo.txt
        return diskFileName;
    }
    memset(__tmpDiskFileName, 0x00, __tmpDiskFileNameLen);
    sprintf( __tmpDiskFileName, "%c/0/%s", diskFileName[0], &diskFileName[2] );
    return __tmpDiskFileName;
}

char* Fs::getAssetsFileEntry(char* assetName) {
    if ( indexOf(assetName, ':') > -1 ) {
        // ex. y:pack1.pak
        return this->getDiskFileName(assetName);
    }
    const int asDNlen = 1+1+8+1+3+1;
    char assetDiskName[asDNlen];
    memset(assetDiskName, 0x00, asDNlen);
    sprintf( assetDiskName, "Z:%s", assetName );
    return this->getDiskFileName( assetDiskName );
}

bool Fs::eraseFile(char* fileName) {
    if ( fileName == NULL ) {
        return false;
    }
    SD.remove( fileName );
    return true;
}

bool Fs::exists(char* fileName) {
    if ( fileName == NULL ) {
        return false;
    }
    return SD.exists( fileName );
}

int Fs::readTextFile(char* fileName, char* dest, int maxLen) {
    if ( fileName == NULL || dest == NULL || maxLen < 0 ) {
        return -1;
    }

    memset(dest, 0x00, maxLen);

    if ( maxLen == 0 ) { return 0; }

    led.drive_led();
    File f = SD.open( fileName, O_READ );
    if ( !f ) {
        led.drive_led(false);
        return -1;
    }

    int cpt=0, ch;
    while(true) {
        ch = f.read();
        if ( ch == -1 ) { break; }
        dest[cpt] = ch;
        cpt++;
        if ( cpt >= maxLen ) { break; }
    }

    f.close();
    led.drive_led(false);

return cpt;
}

// stops on 0x00 (because of TEXTFile)
int Fs::writeTextFile(char* fileName, char* source, int maxLen) {
    if ( fileName == NULL || source == NULL || maxLen < 0 ) {
        return -1;
    }

    if ( maxLen == 0 ) { return 0; }

    led.drive_led();
    File f = SD.open( fileName, O_CREAT | O_WRITE );
    if ( !f ) {
        led.drive_led(false);
        return -1;
    }
    // erase file content
    f.truncate(0);

    int ch,i;
    for(i=0; i < maxLen; i++) {
        ch = source[i];
        if ( ch <= 0 ) { break; }
        f.write(ch);
        if ( i % 32 == 0 ) { f.flush(); }
    }
    f.flush();

    f.close();
    led.drive_led(false);

return i;
}

// (!!) doesn't eraese dest buffer
int Fs::readBinFile(char* fileName, uint8_t* dest, int maxLen) {
    if ( fileName == NULL || dest == NULL || maxLen < 0 ) {
        return -1;
    }
    
    led.drive_led();
    File f = SD.open( fileName, O_READ );
    if ( !f ) {
        led.drive_led(false);
        return -1;
    }

    int cpt=0, ch;
    while(true) {
        ch = f.read();
        if ( ch == -1 ) { break; }
        dest[cpt] = (uint8_t)ch;
        cpt++;
        if ( cpt >= maxLen ) { break; }
    }

    f.close();
    led.drive_led(false);
    return cpt;
}

bool Fs::downloadFromSerial() {
    led.clr_blue();
    while( Serial.available() ) { Serial.read(); delay(2); }
    console.warn((char*)"Download in progress");
    Serial.println("+OK");
    while( !Serial.available() ) { delay(2); }
    // for now : file has to be like "/C/0/XTSDEMO.PAS"
    int tlen = 0;
    char txt[128+1]; 
    char name[64+1]; memset(name, 0x00, 64); tlen = Serial.readBytesUntil(0x0A, name, 64);
    if ( tlen <= 0 ) {
        sprintf(txt, "Downloading %s (error)", name);
        console.error(txt);
        led.clr_red();
        Serial.println("Download not ready");
        Serial.println(name);
        Serial.println("-OK");
        return false;
    }

    // Cf CPM may padd the original file
    File f = SD.open(name, O_CREAT | O_WRITE);
    if ( !f ) {
        Serial.println("-OK");
        led.clr_red();
        return false;    
    }
    f.remove();
    f.close();
    // Cf CPM may padd the original file

    f = SD.open(name, O_CREAT | O_WRITE);
    if ( !f ) {
        Serial.println("-OK");
        led.clr_red();
        return false;    
    }

    Serial.println("+OK");
    while( !Serial.available() ) { delay(2); }
    char sizeStr[12+1]; memset(sizeStr, 0x00, 12); tlen = Serial.readBytesUntil(0x0A, sizeStr, 12);
    long size = atol(sizeStr);
    sprintf(txt, "Downloading %s (%ld bytes)", name, size);
    console.warn(txt);
    char packet[128+1];
    Serial.println("+OK");
    for(int readed=0; readed < size;) {
        while( !Serial.available() ) { delay(2); }
        int packetLen = Serial.readBytes( packet, 128 );
        f.write(packet, packetLen);
        f.flush();
        readed += packetLen;
    }
    f.close();
    console.warn("-EOF-");
    //this->yatl->beep();
    led.clr_green();
    return true;
}

