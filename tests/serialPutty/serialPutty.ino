/**
 * Putty VT-xx escapes tests 
 */

#include "SerialConsole.h"

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

            con_ser_cls();

            Serial.write("coucou");

            // row, col 1-Based
            con_ser_cursor(3, 10);
            

            con_ser_attr_accent();
            Serial.print("TOTO");
            con_ser_attr_none();

            Serial.println("");


            con_ser_cursor(1, 1);
            for(int x=0; x < con_ser_width(); x++) {
                Serial.print( (x%10) );
            }
            for(int y=0; y < con_ser_height(); y++) {
                con_ser_cursor(y+1, 1);
                Serial.print( y );
            }
        }

    }


}