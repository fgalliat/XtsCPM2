#include <SPI.h>
#include "SdFat.h"
#include "sdios.h"

// SdFatSdio SD;
SdFat SD;

#include "Adafruit_ILI9486_Teensy.h"

 // for pin definitions, please refer to the header file
Adafruit_ILI9486_Teensy tft;

void setup()
{
	Serial.begin(115200);
	// while ( !Serial );

	Serial.println("ILI9486 + SDCard");

	SPI.begin();
	tft.begin();
    tft.setRotation(1);

    if ( ! SD.begin(BUILTIN_SDCARD) ) {
        Serial.println("SD Error, Halting...");
        while(true) { delay(10000); }
    }

}

void loop() {
   tft.fillScreen(BLACK);
   tft.setCursor(0, 0);
   tft.setTextColor(WHITE);  
   tft.setTextSize(1);
   tft.println("Hello World!");
   tft.println("");

    File f;

	if (f = SD.open("C/0/CUBE3D.PAS", O_READ)) {
	// if (f = SD.open("foo.txt", O_READ)) {
        int cpt = 0;
		while (f.available()) {
            int ch = f.read();
			Serial.write( ch );
            tft.write(ch);
            if ( cpt++ >= 255 ) { break; }
        }
		f.close();
	} else {
        Serial.println(">> open failed");
        tft.println(">> open failed");
        delay(2000);
        return;
    }
	
    // 6x8 -> 80x40

    tft.setCursor(0, 0);
    for(int x=0; x < 80; x++) {
        tft.print( (x%10) );
    }
    for(int y=0; y < 40; y++) {
        tft.setCursor(0, y*8);
        tft.println( y );
    }




    delay(10000);

}