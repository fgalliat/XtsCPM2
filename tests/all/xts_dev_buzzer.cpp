#include "top.h"

/**
 * Buzzer API common impl.
 * 
 * 
 * Xtase - fgalliat @Mar 2020
 */

#include <Arduino.h>

  // #include "xts_string.h"
  extern char charUpCase(char ch);

#include "xts_dev_buzzer.h"

#include "xts_dev_rgbled.h"
extern RGBLed led;

// for tune files
#include "xts_dev_fs.h"
extern Fs fileSystem;

#include "xts_soft_console.h"
extern IOConsole console;


#include "xts_dev_joystick.h"
extern Joystick joystick;

#include "xts_soft_buzzer_notes.h"


void Buzzer::mute() { _mute = true; }
void Buzzer::unmute() { _mute = false; }
bool Buzzer::isMute() { return _mute; }

/**
 * plays a note or pseudo freq.
 * duration is a 1/50th
 */
void Buzzer::playNote(int noteOrFreq, int duration) {
    if (this->_mute) { return; } 

    if ( noteOrFreq >= 1 && noteOrFreq <= 48 ) {
        // 0..48 octave2 to 5
        noteOrFreq = notes[ noteOrFreq-1 ];
    } else if ( noteOrFreq >= 49 && noteOrFreq <= 4096 ) {
        // 49..4096 -> 19200/note in Hz
        noteOrFreq *= 20;
    } else {
        noteOrFreq = 0;
    }

    this->noTone();
    this->tone( noteOrFreq, duration*50 );
    ::delay(duration*50);
    this->noTone(); // MANDATORY for ESP32
}

// to move away
// ex. "AC#B"
void Buzzer::playTuneString(char* tuneString) {
    if (this->_mute) { return; } 
    this->noTone();

    int defDuration = 200;
    int slen = strlen( tuneString );

    for (int i=0; i < slen; i++) {
        char ch = tuneString[i];
        ch = charUpCase(ch);
        bool sharp = false;
        if ( i+1 < slen && tuneString[i+1] == '#' ) { 
            sharp = true; 
            i++; 
        }  

        int pitch = 0;
        switch (ch) {
            case 'C' :
                if ( sharp ) { pitch = notes[ NOTE_CS4 ]; }
                else pitch = notes[ NOTE_C4 ];
                break;
            case 'D' :
                if ( sharp ) { pitch = notes[ NOTE_DS4 ]; }
                else pitch = notes[ NOTE_D4 ];
                break;
            case 'E' :
                pitch = notes[ NOTE_E4 ];
                break;
            case 'F' :
                if ( sharp ) { pitch = notes[ NOTE_FS4 ]; }
                else pitch = notes[ NOTE_F4 ];
                break;
            case 'G' :
                if ( sharp ) { pitch = notes[ NOTE_GS4 ]; }
                else pitch = notes[ NOTE_G4 ];
                break;
            case 'A' :
                if ( sharp ) { pitch = notes[ NOTE_AS4 ]; }
                else pitch = notes[ NOTE_A4 ];
                break;
            case 'B' :
                pitch = notes[ NOTE_B4 ];
                break;
        }

        this->tone(pitch, defDuration);
        ::delay(defDuration);
    }
    this->noTone(); // MANDATORY for ESP32
}

// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
bool checkbreak() { return false; }

// (!!) beware joystick.poll() may slow down playback
bool anyBtn() { joystick.poll(); if (joystick.isBtn1()) { return true; } else { return false; } }

// T5K Format
bool __playTune(Buzzer* _this, unsigned char* tuneStream, bool btnStop);
// T53
bool __playTuneT53(Buzzer* _this, unsigned char* tuneStream, bool btnStop);

// 5KB audio buffer
#define AUDIO_BUFF_SIZE (5 * 1024)
uint8_t audiobuff[AUDIO_BUFF_SIZE];
void cleanAudioBuff() { memset(audiobuff, 0x00, AUDIO_BUFF_SIZE); }

void led3(bool state) { if (state) { led.clr_blue(); } }
void led2(bool state) { if (state) { led.clr_green(); } }
void led1(bool state) { if (state) { led.clr_red(); } }

typedef struct Note {
    unsigned char note;
    unsigned short duration;
} Note;

    
    // ex. "MONKEY.T5K"
    bool _playTuneFile(Buzzer* _this, char* tuneStreamName) { 
        if (_this->isMute()) { return true; } 
        _this->noTone();

        cleanAudioBuff();
        int tlen = strlen(tuneStreamName);
        if ( tlen < 1 ) {
            char msg[64+1];
            sprintf(msg, "BUZ NO File provided");
            console.warn(msg);
            return false;
        }

        char* ftuneStreamName = fileSystem.getAssetsFileEntry( tuneStreamName );
        int format = 0;
        bool btnStop = true;

        char lastCh = tuneStreamName[ tlen-1 ];
        lastCh = charUpCase(lastCh);
        if ( (( lastCh == 'K' || lastCh == '3') && (tlen >= 2 && tuneStreamName[ tlen-2 ] == '5') ) ) {
            if ( lastCh == 'K' ) {
                format = AUDIO_FORMAT_T5K;
            } else {
                format = AUDIO_FORMAT_T53;
            }
        }

        /*static*/ unsigned char preBuff[2];
        memset(preBuff, 0x00, 2);
        int n = fileSystem.readBinFile(ftuneStreamName, preBuff, 2);
        if ( n <= 0 ) {
            char msg[64+1];
            sprintf(msg, "BUZ File not ready %s", ftuneStreamName);
            console.warn(msg);
            return false;
        }
        int nbNotes = (preBuff[0]<<8)|preBuff[1];

        int fileLen = (nbNotes*sizeof(Note))+2+16+2;
        if ( format == AUDIO_FORMAT_T53 ) {
            fileLen = (nbNotes*(3+3+3))+2+16+2;
        }
        n = fileSystem.readBinFile(ftuneStreamName, audiobuff, fileLen);

        bool ok = false;
        if ( format == AUDIO_FORMAT_T5K ) {
            ok = __playTune(_this,  &audiobuff[0], btnStop );  
        } else {
            ok = __playTuneT53(_this,  &audiobuff[0], btnStop );  
        }
        _this->noTone();
        return ok;
    }

    // where tuneStream is the audio buffer content
    // T5K audio format
    bool __playTune(Buzzer* _this, unsigned char* tuneStream, bool btnStop) {
        _this->noTone();
        /*static*/ short nbNotes = (*tuneStream++ << 8) | (*tuneStream++);
        /*static*/ char songname[16+1];
        memset(songname, 0x00, 16+1); // BUGFIX -> YES
        for(int i=0; i < 16; i++) {
            songname[i] = *tuneStream++;
        }

        char msg[64+1];
        sprintf(msg, "-= Playing : %s =-", songname );
        console.println( msg );

        short tempoPercent = (*tuneStream++ << 8) | (*tuneStream++);
        float tempo = (float)tempoPercent / 100.0;
        // cf a bit too slow (Cf decoding)
        tempo *= 0.97;
        for (int thisNote = 0; thisNote < nbNotes; thisNote++) {
            int note = *tuneStream++;
            short duration = (*tuneStream++ << 8) | (*tuneStream++);
            // note 0 -> silence
            if ( note > 0 ) {
                _this->tone(notes[ note-1 ], duration);
                led.off();
                led2( note > 30 );
                led3( note > 36 );
            }
            // to distinguish the notes, set a minimum time between them.
            // the note's duration + 10% seems to work well => 1.10:
            int pauseBetweenNotes = duration * tempo;
            delay(pauseBetweenNotes);
            // stop the tone playing:
            _this->noTone();

            if (btnStop && ( anyBtn() || checkbreak() ) ) {
                return true;
            }
        } // end of note loop

        led2(false);
        led3(false);
        led.off();
        return true;
    } // end of play T5K function

    // T53 Format
    // where tuneStream is the audio buffer content
    bool __playTuneT53(Buzzer* _this, unsigned char* tuneStream, bool btnStop = false) {
        _this->noTone();
    
        short nbNotes = (*tuneStream++ << 8) | (*tuneStream++);
        char songname[16];
        memset(songname, 0x00, 16); // BUGFIX -> YES
        for(int i=0; i < 16; i++) {
            songname[i] = *tuneStream++;
        }
        short tempoPercent = (*tuneStream++ << 8) | (*tuneStream++);

        if ( true ) {
            char msg[64+1];
            sprintf(msg, "-= Playing : %s =-", songname );
            console.println( msg );
        }
    
        float tempo = (float)tempoPercent / 100.0;
        // cf a bit too slow (Cf decoding)
        tempo *= 0.97;

        short note, duration, wait;
        for (int thisNote = 0; thisNote < nbNotes; thisNote++) {
            note = (*tuneStream++ << 8) | (*tuneStream++);
            duration = (*tuneStream++ << 8) | (*tuneStream++);
            wait = (*tuneStream++ << 8) | (*tuneStream++);
            
            // note 0 -> silence
            if ( note > 0 ) {
                _this->tone(note, duration);
            }

            delay(wait*tempo);
            // stop the tone playing:
            _this->noTone();

            if (btnStop && ( anyBtn() || checkbreak() ) ) {
                return true;
            }
        }
        return true;
    } // end of play T53 function



// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

// to move away
void Buzzer::playTuneFile(char* filename, bool btnBreaks) {
    // btnBreaks is ignored for now ...
    _playTuneFile( this, filename);
}

