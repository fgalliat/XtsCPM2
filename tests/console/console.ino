/**
 * Putty VT-xx escapes tests 
 */

#include "OutputConsole.h"

#include "GenericConsole.h"
GenericConsole console(CONSOLE_MODE_SERIAL_VT100 | CONSOLE_MODE_TFT );


void setup() {
    // con_ser_init();
    console.init();
}


void loop() {
    if ( Serial.available() > 0 ) {
        int ch = Serial.read();
        console.print( "ch : " );
        console.print( ch );
        console.print( " " );
        console.println( (ch == 27 ? '^' : (char)ch) );


        if ( ch == 9 || ch == ' ' ) {
            // Tab
            //http://manpagesfr.free.fr/man/man4/console_codes.4.html
            // http://www.termsys.demon.co.uk/vtansi.htm

            console.cls();

            console.cursor(1, 1);
            console.print( splash_screen );
            console.cursor(10, 40);
            console.attr_accent();
            console.print("Missing BOOT-DISK /!\\");
            console.attr_none();
            console.println();


            console.print("coucou");

            // row, col 1-Based
            console.cursor(3, 10);
            

            console.attr_accent();
            console.print("TOTO");
            console.attr_none();

            console.println("");


            console.cursor(1, 1);
            for(int x=0; x < con_ser_width(); x++) {
                console.print( (x%10) );
            }
            for(int y=0; y < con_ser_height(); y++) {
                console.cursor(y+1, 1);
                console.print( y );
            }

            console.cursor(15, 45);
            console.println( 12.125 );

            console.cursor(17, 45);
            console.println("Write from ganaric class");

        }

    }


}