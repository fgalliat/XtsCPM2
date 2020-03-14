
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
        void beep(int freq, long duration);

        /**
         * plays a note or pseudo freq.
         * duration is a 1/50th
         */
        void playNote(int noteOrFreq, int duration);

        // ex. "AC#B"
        void playTuneString(char* tuneString);
        void playTuneFile(char* filename, bool btnBreaks = true);

        void mute();
        void unmute();
        bool isMute();
};