#include "top.h"
/**
 * Joystick class impl.
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include <Arduino.h>

#include "xts_soft_console.h"
extern IOConsole console;

#include "xts_dev_joystick.h"

// ==== SerialIn JoystickKeys Support ====
uint8_t virtualX = 3;
uint8_t virtualY = 3;
bool virtualXactive = false;
bool virtualYactive = false;

const char sKEY_UP = (char)65;
const char sKEY_DOWN = (char)66;
const char sKEY_RIGHT = (char)67;
const char sKEY_LEFT = (char)68;

// BEWARE : it port is not Serial0
#define PORT Serial

bool serialPoll() {
    if ( !console.hasSerialInput() ) { return false; }
    if ( console.isSerialInputDummy() ) { return false; }

    int avail = PORT.available();

    // Arrow Key 27, 91, {65, 66, 67, 68}
    if ( avail >= 3 && PORT.peek() == 27 ) {
        char chs[3]; memset(chs, 0x00, 3);
        PORT.readBytes(chs, 3);
        // see if need to restore UART Buffer ....
        if ( chs[1] != (char)91 ) { return false; }
        if ( chs[2] < sKEY_UP || chs[2] > sKEY_LEFT ) { return false; }

        char dir = chs[2];
        if ( dir == sKEY_UP )    { virtualY = 0; virtualYactive = true; }
        else if ( dir == sKEY_DOWN )  { virtualY = 7; virtualYactive = true; }
        if ( dir == sKEY_RIGHT ) { virtualX = 7; virtualXactive = true; }
        else if ( dir == sKEY_LEFT )  { virtualX = 0; virtualXactive = true; }
        return true;
    }

    // handle BackSpace (BTN2) / Enter (BTN1) / ?? (MENU) 
    return false;
}

// =======================================

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
    }

    // Serial keys emulation
    if ( !changedState ) {
        changedState = serialPoll();
    }
}

uint8_t* Joystick::getState() { return states; }

bool Joystick::isDirLeft() {
    if ( virtualXactive && virtualX <= 1 ) { virtualXactive = false; return true; }
    return states[0] <= 1;
}

bool Joystick::isDirRight() {
    if ( virtualXactive && virtualX >= 6 ) { virtualXactive = false; return true; }
    return states[0] >= 6;
}

bool Joystick::isDirUp() {
    if ( virtualYactive && virtualY <= 1 ) { virtualYactive = false; return true; }
    return states[1] <= 1;
}

bool Joystick::isDirDown() {
    if ( virtualYactive && virtualY >= 6 ) { virtualYactive = false; return true; }
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


