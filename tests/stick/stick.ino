#include "xts_dev_joystick.h"

Joystick joystick;

void setup() {
    joystick.setup();

    Serial.begin(115200);
}

void loop() {
    joystick.poll();

    if ( joystick.hasChangedState() ) {
        Serial.println( joystick.toString() );
    }

    delay(100);
}