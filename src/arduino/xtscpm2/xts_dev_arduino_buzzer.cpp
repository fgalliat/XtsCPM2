#include "top.h"

/**
 * Buzzer API impl.
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include <Arduino.h>


#include "xts_dev_buzzer.h"
#define BUZZ_PIN 2

#include "xts_soft_buzzer_notes.h"

Buzzer::Buzzer(){
}

bool Buzzer::setup() {
    pinMode( BUZZ_PIN, OUTPUT );
    digitalWrite( BUZZ_PIN, LOW );
    _mute = false;
    return true;
}

void Buzzer::tone(int freq, long duration) {
    if ( !_mute ) {
        ::tone(BUZZ_PIN, freq, duration);
    }
}

void Buzzer::beep(int freq, long duration) {
    tone(freq, duration);
    ::delay( (float)duration*0.98f );
    noTone();
}

void Buzzer::noTone() {
    ::noTone(BUZZ_PIN);
}

