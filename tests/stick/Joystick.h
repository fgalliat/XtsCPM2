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

      bool incDebounce(int idx);
      bool decDebounce(int idx);
      uint8_t readAxis(int pin);

    public:
        Joystick();

        bool setup();

        // 0..7
        uint8_t readX();
        uint8_t readY();

        void poll();

        uint8_t* getState();

        bool isDirLeft();
        bool isDirRight();
        bool isDirUp();
        bool isDirDown();
        bool isBtn0();
        bool isBtn1();
        bool isBtn3();
        bool hasChangedState();

        char* toString();
};