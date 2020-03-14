#define LED 13

void setup() {

    pinMode(LED, OUTPUT);
    digitalWrite(LED, LOW);

    Serial.begin(115200);
    

}

void loop() {

    digitalWrite(LED, HIGH);
    Serial.println("Hello teensy");
    delay(300);
    digitalWrite(LED, LOW);

    delay(1500);

}