/**
 * 
 * YATDB FileSystem API impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */
#include <Arduino.h>

#include "xts_string.h"

#include "xts_arduino_dev_fs.h"

// ===== Hardware section =====
#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

// SdFatSdio SD;
SdFat SD;
// ===== Hardware section =====

const int __tmpDiskFileNameLen = 4+8+1+3+1;
char __tmpDiskFileName[__tmpDiskFileNameLen]; 

Fs::Fs() {
}

bool Fs::setup() {
    return SD.begin(BUILTIN_SDCARD);
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


int Fs::readTextFile(char* fileName, char* dest, int maxLen) {
    if ( fileName == NULL || dest == NULL || maxLen < 0 ) {
        return -1;
    }

    memset(dest, 0x00, maxLen);

    if ( maxLen == 0 ) { return 0; }

    File f = SD.open( fileName, O_READ );
    if ( !f ) {
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

return cpt;
}

// stops on 0x00 (because of TEXTFile)
int Fs::writeTextFile(char* fileName, char* source, int maxLen) {
    if ( fileName == NULL || source == NULL || maxLen < 0 ) {
        return -1;
    }

    if ( maxLen == 0 ) { return 0; }

    File f = SD.open( fileName, O_CREAT | O_WRITE );
    if ( !f ) {
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

return i;
}


