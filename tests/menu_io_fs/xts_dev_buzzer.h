
/**
 * Buzzer API 
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 */

class Buzzer {
    private:
        bool _mute = false;
    public:
        Buzzer();
        bool setup();
        
        void tone(int freq, long duration);
        void noTone();

        void mute();
        void unmute();
};