/**
 * RGB led driver API
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 * 
 */

class RGBLed {

    private:
        uint8_t red;
        uint8_t green;
        uint8_t blue;

        void save(uint8_t r, uint8_t g, uint8_t b);
        void restore();
        void setState(uint8_t r, uint8_t g, uint8_t b);
    public:
        RGBLed();
        bool setup();
        void rgb(uint8_t r, uint8_t g, uint8_t b);
        void off();
        void drive_led(bool state = true);
        void clr_red();
        void clr_green();
        void clr_blue();
};