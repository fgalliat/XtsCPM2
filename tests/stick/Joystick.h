/**
 * Joystick class
 * 
 * Xtase - fgalliat @Mar 2020
 */

#define JOY_X_AXIS 35
#define JOY_Y_AXIS 36
// Joystick integrated push button
#define JOY_BTN0  24
#define JOY_BTN1  25
#define JOY_BTN2  26


class Joystick {

    private:
      static const int nbBtns = 3;
      uint8_t btnPins[nbBtns] = { 
          JOY_BTN0, JOY_BTN1, JOY_BTN2
      };

      // value to adjust
      uint8_t debounceMax = 2; // for 100ms latency
      bool changedState = false;
      uint8_t lastX,lastY;

      uint8_t debounce[2 + nbBtns]; // [x][y][...] are not debounce values
      uint8_t states[2 + nbBtns];
      bool incDebounce(int idx) {
        if ( debounce[idx] >= debounceMax ) { return false; }
        debounce[idx]++;
        if ( debounce[idx] >= debounceMax ) { return true; }
        return false;
      }
      bool decDebounce(int idx) {
          if ( debounce[idx] <= 0x00 ) { return false; }
          debounce[idx]--;
          if ( debounce[idx] <= 0x00 ) { return true; }
          return false;
      }
      uint8_t readAxis(int pin) {
          int v = analogRead( pin );
          // v /= 128; // 0..1024 -> 0..8
          v = v >> 7; // div128 : 0..1024 -> 0..8
          return v;
      }

    public:
        Joystick() {
            memset( debounce, 0x00, 2 + nbBtns );
            memset( states, 0x00, 2 + nbBtns );
            lastX = 3;
            lastY = 3;
        }

        bool setup() {
            for(int i=0; i < nbBtns; i++) {
                pinMode( btnPins[i] , INPUT_PULLUP);
            }
            pinMode(JOY_X_AXIS, INPUT);
            pinMode(JOY_Y_AXIS, INPUT);
            return true;
        }

        // 0..7
        uint8_t readX() {
            return 7 - readAxis( JOY_X_AXIS );
        }

        uint8_t readY() {
            return readAxis( JOY_Y_AXIS );
        }

        void poll() {
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

        uint8_t* getState() {
            return states;
        }

        bool isDirLeft() {
            return states[0] <= 1;
        }

        bool isDirRight() {
            return states[0] >= 6;
        }

        bool isDirUp() {
            return states[1] <= 1;
        }

        bool isDirDown() {
            return states[1] >= 6;
        }

        bool isBtn0() {
            return states[2] >= 1;
        }

        bool isBtn1() {
            return states[3] >= 1;
        }

        bool isBtn3() {
            return states[4] >= 1;
        }

        bool hasChangedState() {
            return changedState;
        }

        char _str[64+1];
        char* toString() {
            memset(_str, 0x00, 64+1);
            sprintf(_str, "X= %d, Y= %d [%d][%d][%d]", states[0], states[1], states[2], states[3], states[4]);
            return _str;
        }

};