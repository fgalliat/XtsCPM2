/**
 * Putty VT-xx escapes tests 
 */

void setup() {
    Serial.begin(115200);
}


void loop() {
    if ( Serial.available() > 0 ) {
        int ch = Serial.read();
        Serial.print( "ch : " );
        Serial.print( ch );
        Serial.print( " " );
        Serial.println( (ch == 27 ? '^' : (char)ch) );


        if ( ch == 9 ) {
            // Tab
            //http://manpagesfr.free.fr/man/man4/console_codes.4.html

            // http://www.termsys.demon.co.uk/vtansi.htm

            // cls whole screen seq
            Serial.write(27);
            Serial.write("[2J");
            // but doesn't reset cursor

            // set cursor to origin 1,1
            Serial.write(27);
            Serial.write("[H"); // row,col ?


            Serial.write("coucou");

            // row 3, col 10 (1-based)
            Serial.write(27);
            Serial.write("[3;10f");

            Serial.write(27);
            Serial.write("[7m"); // reverse video

            Serial.write("TOTO");

            Serial.write(27);
            Serial.write("[0m"); // normal video (all attrs disabled)
        }

    }


}