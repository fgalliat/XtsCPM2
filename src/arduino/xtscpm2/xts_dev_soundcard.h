/**
 * Xtase - fgalliat Sept2019
 * 
 * DFPlayer - lite lib
 */

#ifndef __SOUNDCARD_H_
#define __SOUNDCARD_H_ 1

  class SoundCard {
      private:
        Stream* serial = NULL;
        
      public:
        /** if non-null serial -> have to call ...begin(9600) */
        SoundCard(Stream* serial = NULL);
        ~SoundCard();
        
        bool setup();
        bool init();
        void close();
        
        void play(int track);
        void volume(int vol);
        
        int getTrackNum();
        char* getTrackName();
        
        int getTrackNb();
        char** getTrackNames();
        
        void next();
        void prev();
        
        bool isPlaying();
        int  getVolume();
        
        void pause();
        void stop();

        void mute();
        void unmute();
        bool isMute();
  };

#endif