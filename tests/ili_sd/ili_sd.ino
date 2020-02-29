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

	Serial.println("***** ILI9486 graphic Test *****");

	SPI.begin();
	tft.begin();

if ( ! SD.begin(BUILTIN_SDCARD) ) {
    // if ( ! SD.begin() ) {
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

	// if (f = SD.open("C/O/CUBE3D.PAS", O_READ)) {
	if (f = SD.open("foo.txt", O_READ)) {
		while (f.available()) {
            int ch = f.read();
			Serial.write( ch );
            tft.write(ch);
        }
		f.close();
	} else {
        Serial.println(">> open failed");
        tft.println(">> open failed");
        delay(2000);
        return;
    }
	

    delay(2000);

}