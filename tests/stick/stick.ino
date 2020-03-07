#include "Joystick.h"

Joystick joystick;

void setup() {
    joystick.setup();

    Serial.begin(115200);
}

void loop() {
    // int btn = digitalRead( SW_PIN ) == LOW ? 1 : 0;
    // int x = analogRead(X_AXIS);
    // int y = analogRead(Y_AXIS);
    // Serial.print( btn );
    // Serial.print( "  " );
    // Serial.print( x );
    // Serial.print( "/" );
    // Serial.print( y );
    // Serial.println( "" );

    joystick.poll();

    Serial.println( joystick.toString() );

    delay(100);
}