#include "top.h"
/**
 * Joystick class impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include <Arduino.h>


#include "xts_dev_joystick.h"

// private symbols
bool Joystick::incDebounce(int idx) {
    if ( debounce[idx] >= debounceMax ) { return false; }
    debounce[idx]++;
    if ( debounce[idx] >= debounceMax ) { return true; }
    return false;
}

bool Joystick::decDebounce(int idx) {
    if ( debounce[idx] <= 0x00 ) { return false; }
    debounce[idx]--;
    if ( debounce[idx] <= 0x00 ) { return true; }
    return false;
}
uint8_t Joystick::readAxis(int pin) {
    int v = analogRead( pin );
    // v /= 128; // 0..1024 -> 0..8
    v = v >> 7; // div128 : 0..1024 -> 0..8
    return v;
}

// public symbols
Joystick::Joystick() {
    memset( debounce, 0x00, 2 + nbBtns );
    memset( states, 0x00, 2 + nbBtns );
    lastX = 3;
    lastY = 3;
}

bool Joystick::setup() {
    for(int i=0; i < nbBtns; i++) {
        pinMode( btnPins[i] , INPUT_PULLUP);
    }
    pinMode(JOY_X_AXIS, INPUT);
    pinMode(JOY_Y_AXIS, INPUT);
    return true;
}

// 0..7 - can be used w/o polling ....
uint8_t Joystick::readX() {
    return 7 - readAxis( JOY_X_AXIS );
}
uint8_t Joystick::readY() {
    return readAxis( JOY_Y_AXIS );
}

void Joystick::poll() {
    changedState = false;
    for(int i=0; i < nbBtns; i++) {
        if ( digitalRead( btnPins[i] ) == LOW ) {
            changedState |= incDebounce(2+i);
        } else {
            changedState |= decDebounce(2+i);
        }
    }
    uint8_t vx = readX();
    uint8_t vy = readY();
    debounce[0] = vx;
    debounce[1] = vy;

    if ( lastX != vx || lastY != vy ) {
        changedState = true;
        lastX = vx;
        lastY = vy;
    }

    states[0] = debounce[0];
    states[1] = debounce[1];
    for(int i=0; i < nbBtns; i++) {
        states[2+i] = debounce[2+i] >= debounceMax ? 1 : 0;
        // states[2+i] = debounce[2+i];
    }
}

uint8_t* Joystick::getState() { return states; }

bool Joystick::isDirLeft() {
    return states[0] <= 1;
}

bool Joystick::isDirRight() {
    return states[0] >= 6;
}

bool Joystick::isDirUp() {
    return states[1] <= 1;
}

bool Joystick::isDirDown() {
    return states[1] >= 6;
}

bool Joystick::isBtn0() {
    return states[2] >= 1;
}

bool Joystick::isBtnMenu() {
    return states[3] >= 1;
}

bool Joystick::isBtn1() {
    return states[4] >= 1;
}

bool Joystick::isBtn2() {
    return states[5] >= 1;
}

bool Joystick::hasChangedState() {
    return changedState;
}


char _str[64+1];
char* Joystick::toString() {
    memset(_str, 0x00, 64+1);
    sprintf(_str, "X= %d, Y= %d [%d][%d][%d][%d]", states[0], states[1], states[2], states[3], states[4], states[5]);
    return _str;
}


