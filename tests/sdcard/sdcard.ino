#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

// SdFatSdio SD;
SdFat SD;

// SdFile file;

// Maximum line length plus space for zero byte.
const size_t LINE_DIM = 50;
char line[LINE_DIM];


void setup() {

    Serial.begin(115200);

    while( !Serial ) {
        delay(500);
    }

    Serial.println("setup SD");


    if ( ! SD.begin(BUILTIN_SDCARD) ) {
    // if ( ! SD.begin() ) {
        Serial.println("SD Error, Halting...");
        while(true) { delay(10000); }
    }

    Serial.println("setup done");

}

void loop() {

    Serial.println("Hello teensy");

    Serial.println("try opening");
/*
    if (!file.open("/foo.txt", O_READ)) {
        Serial.println(">> open failed");
        delay(2000);
        return;
    }
 
    int n;
    while ((n = file.fgets(line, sizeof(line))) > 0) {
        Serial.print(">> ");
        Serial.println(line);
    }
    file.close();
*/

File f;

// int e = SD.exists("C/0/CUBE.PAS");
int e = SD.exists("/FOO.TXT");
Serial.println( e );

	if (f = SD.open("foo.txt", O_READ)) {
	// if (f = SD.open("C/0/CUBE.PAS", O_READ)) {
		while (f.available())
			Serial.write( f.read() );
		f.close();
	} else {
        Serial.println(">> open failed");
        delay(2000);
        return;
    }
	

    Serial.println(F("Done"));


    delay(1500);

}

