#include "xts_dev_rgbled.h"

RGBLed led;

// 209, 228, 194 -> vert pastel
// 182, 237, 141 -> bleu pastel


void setup() {
    led.setup();
}

void loop() {
    led.rgb( 209, 228, 194 );
    delay(1000);
    led.rgb( 182, 237, 141 );
    delay(1000);
    led.rgb( 187,  74, 230 ); // purple
    delay(1000);

    led.drive_led(true);
    delay(100);
    led.drive_led(false);
    delay(100);
    led.drive_led(true);
    delay(100);
    led.drive_led(false);
    delay(1000);

}