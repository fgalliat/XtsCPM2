#define SW_PIN 24
#define X_AXIS 35
#define Y_AXIS 36

void setup() {
    pinMode(SW_PIN, INPUT_PULLUP);
    pinMode(X_AXIS, INPUT);
    pinMode(Y_AXIS, INPUT);

    Serial.begin(115200);
}

void loop() {
    int btn = digitalRead( SW_PIN ) == LOW ? 1 : 0;
    int x = analogRead(X_AXIS);
    int y = analogRead(Y_AXIS);
    Serial.print( btn );
    Serial.print( "  " );
    Serial.print( x );
    Serial.print( "/" );
    Serial.print( y );
    Serial.println( "" );
}