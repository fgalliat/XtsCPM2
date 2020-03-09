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

Buzzer::Buzzer(){
}

bool Buzzer::setup() {
    pinMode( BUZZ_PIN, OUTPUT );
    _mute = false;
    return true;
}

void Buzzer::tone(int freq, long duration) {
    if ( !_mute ) {
        ::tone(BUZZ_PIN, freq, duration);
    }
    ::delay( (float)duration*0.98f );
}

void Buzzer::noTone() {
    ::noTone(BUZZ_PIN);
}

void Buzzer::mute() { _mute = true; }
void Buzzer::unmute() { _mute = false; }