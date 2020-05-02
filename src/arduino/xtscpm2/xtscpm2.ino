#include "cpm_globals.h"

/**
 * TODO : 
 * cpm.h -> re-handle specific BdosCalls
 * abstraction_arduino.h -> refactor DRIVE_LED calls (done)
 *                       -> refactor _kbhit, getch, getche (done)
 * call xts_handler !!! (done)
 * 
 * 
 * 
 * Xtase - fgalliat @ Mar2020
 * 
 * after 1st version of cleaned code :
 * Le croquis utilise 78064 octets (7%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 * Les variables globales utilisent 72484 octets (27%) de mémoire dynamique, ce qui laisse 189660 octets pour les variables locales. Le maximum est de 262144 octets.
 * 
 * after 2nd version w/ console calls
 * Le croquis utilise 112844 octets (10%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 * Les variables globales utilisent 80068 octets (30%) de mémoire dynamique, ce qui laisse 182076 octets pour les variables locales. Le maximum est de 262144 octets.
 * 
 * after adding xts_handler again
 * Le croquis utilise 132524 octets (12%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 * Les variables globales utilisent 128280 octets (48%) de mémoire dynamique, ce qui laisse 133864 octets pour les variables locales. Le maximum est de 262144 octets.
 * 
 * after adding BdosCalls back
 * Le croquis utilise 137284 octets (13%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 * Les variables globales utilisent 128372 octets (48%) de mémoire dynamique, ce qui laisse 133772 octets pour les variables locales. Le maximum est de 262144 octets.
 * 
 */

#if XTASE_YATDB_LAYOUT

#define USE_XTS_HDL 1
// forward symbols
void xts_handler();

// ========> to move away =======>
// Teensy 3.6 specific !!
#define RESTART_ADDR 0xE000ED0C
#define READ_RESTART() (*(volatile uint32_t *)RESTART_ADDR)
#define WRITE_RESTART(val) ((*(volatile uint32_t *)RESTART_ADDR) = (val))
void __softReset() {
    WRITE_RESTART(0x5FA0004);
}
// ========> to move away =======>

#include "xts_dev_joystick.h"
Joystick joystick;

#include "xts_dev_rgbled.h"
RGBLed led;

#include "xts_dev_gfx.h"
VideoCard screen;

#include "xts_soft_console.h"
IOConsole console( CONSOLE_MODE_SERIAL_VT100 | CONSOLE_MODE_TFT, CONSOLE_MODE_SERIAL_VT100 );

#include "xts_dev_fs.h"
Fs fileSystem;

#include "xts_dev_buzzer.h"
Buzzer buzzer;

#include "xts_dev_soundcard.h"
SoundCard snd;

#define WIFI_AT_START 1
#include "xts_dev_wifi.h"
WiFi wifi;

void _kill() {
  snd.stop();
  led.rgb(0,0,0);
}

void reboot() {
  _kill();
  __softReset();
}

void halt() {
  _kill();
  console.warn((char*)"Halting");
  while(true) {
    delay(10000);
  }
}

#include "xts_soft_menu.h"

#include "xts_dev_arduino_bdos.h"

  // ======== TEMP SD access resources =========
  // just for File class def (PUN, LST, abstraction_arduino.h)
  #include "SdFat.h"
  extern FS_CLASS SD;
  // ===========================================
  // // RGBLed -> r
  // #define LED 21
  // #define LEDinv false
  bool Serial_useable = true;

  const long maxTimePoll = 200L;
  const long maxTimeInput = 700L;
  long lastTimePoll = 0L;
  long lastTimeInput = 0L;
  void xts_handler() {
    long now = millis();
    if ( now - lastTimePoll <= maxTimePoll ) { 
        return;
    }
    lastTimePoll = millis();
    joystick.poll();

    if ( now - lastTimeInput <= maxTimeInput ) { 
        return;
    }
    lastTimeInput = millis();
    

    if ( joystick.hasChangedState() ) {
        if ( joystick.isBtnMenu() ) {
            menu();
        }
    }
}
#endif

// Serial port speed
#define SERIALSPD 115200

// Delays for LED blinking
#define sDELAY 50
#define DELAY 100

#include "cpm_abstraction_arduino.h"

// PUN: device configuration
#ifdef USE_PUN
  File pun_dev;
  int pun_open = FALSE;
#endif

// LST: device configuration
#ifdef USE_LST
  File lst_dev;
  int lst_open = FALSE;
#endif

#include "cpm_ram.h"
#include "cpm_console.h"
#include "cpm_cpu.h"
#include "cpm_disk.h"
#include "cpm_host.h"
#include "cpm_cpm.h"
#ifdef CCP_INTERNAL
#include "cpm_ccp.h"
#endif

#if XTASE_YATDB_LAYOUT
void once() {
  // console.println("OK, waiting for start...");
  // console.println("Pausing ...");
  // while(true) { delay(1000); }
  console.println("OK, start CPM emulation...");
}

void xts_setup() {
    joystick.setup();
    led.setup();
    buzzer.setup();

    screen.setup();

    console.setup();

    bool allOk = true;
    bool sdOk = fileSystem.setup();
    allOk &= sdOk;

    bool sndOk = snd.setup();
    allOk &= sndOk;

    bool wifOk = false;
    #if WIFI_AT_START
        wifOk = wifi.setup();
        // don't affect the allOk flag ...
    #endif

    console.cls();
    console.println("Joystick ... OK");
    console.println("Leds     ... OK");
    console.println("Console  ... OK");
    if ( sdOk ) {
        console.println("FileSyst ... OK");
    } else {
        console.println("FileSyst ... NOK");
    }
    if ( sndOk ) {
        console.println("SoundCard ... OK");
    } else {
        console.println("SoundCard ... NOK");
    }
    if ( wifOk ) {
        console.println("WiFi      ... OK");
    } else {
        console.println("WiFi      ... NOK");
    }
    console.println("");

    delay(300);

    if ( allOk ) {
        led.rgb( 209, 228, 194 ); // pastel green
        
        // remove Serial VTconsole out
        // Keep Serial Input @ Least
        console.setMode( CONSOLE_MODE_TFT, CONSOLE_MODE_SERIAL_VT100 );

        console.cls();
        console.splashScreen_SD();
        console.cursor(10, 40);
        console.attr_accent();
        console.println("System is OK !");
        console.attr_none();

        console.cursor(17, 1);

        once();
    } else {
        led.rgb( 187,  74, 230 ); // purple
        
        console.println("");
        console.println(" - System is not OK -");
        console.println(" Halting.");
        while( true ) { delay(1000); }
    }

}
#endif

void setup(void) {

  #if XTASE_YATDB_LAYOUT
    xts_setup();
  #else
    // ==== pin-init code ==== 
    pinMode(LED, OUTPUT);
    digitalWrite(LED, LOW);

    Serial.begin(SERIALSPD);
    // ========================
  #endif


  // // ==== TEMP SD init code =====
  // _puts("Initializing SD card.\r\n");
  // #if XTASE_YATDB_LAYOUT
  //   bool sdOk = SD.begin(BUILTIN_SDCARD);
  //   if ( !sdOk ) {
  //     // _puts("Unable to initialize SD card [0x03].\r\nCPU halted.\r\n");
  //     Serial.println("Could not init the SD card, emulator will never run ...");
  //     Serial.println("Halting ...");
  //     while( true ) { delay(10000); }
  //   }
  // #endif
  // // ============================

  // SerialPort is useable or not .....
  Serial_useable = !(!Serial);
  Serial_useable = true;
  
#ifdef DEBUGLOG
  _sys_deletefile((uint8 *)LogName);
#endif

  _clrscr();
  _puts("CP/M 2.2 Emulator v" VERSION " by Marcelo Dantas\r\n");
  _puts("Arduino read/write support by Krzysztof Klis\r\n");
  _puts("      Build " __DATE__ " - " __TIME__ "\r\n");
  _puts("--------------------------------------------\r\n");
  _puts("CCP: " CCPname "    CCP Address: 0x");
  _puthex16(CCPaddr);
  _puts("\r\nBOARD: ");
  _puts(BOARD);
  _puts("\r\n");

  char ccpFileName[13+1];
  memset( ccpFileName, 0x00, 13+1 );
  #if YAEL_PLATFORM
    // SD.h needs leading "/"
    sprintf(ccpFileName, "/%s", CCPname);
  #else
    sprintf(ccpFileName, "%s", CCPname);
  #endif

    // if (VersionCCP >= 0x10 || SD.exists(CCPname)) {
    if (VersionCCP >= 0x10 || fileSystem.exists(ccpFileName)) {
      while (true) {
        _puts(CCPHEAD);
        _PatchCPM();
	Status = 0;
#ifndef CCP_INTERNAL
        // if (!_RamLoad((char *)CCPname, CCPaddr)) {
        if (!_RamLoad((char *)ccpFileName, CCPaddr)) {
          _puts("Unable to load the CCP [0x01].\r\nCPU halted.\r\n");
          break;
        }
        Z80reset();
        SET_LOW_REGISTER(BC, _RamRead(0x0004));
        PC = CCPaddr;
        Z80run();
#else
        _ccp();
#endif
        if (Status == 1)
          break;
#ifdef USE_PUN
        if (pun_dev)
          _sys_fflush(pun_dev);
#endif
#ifdef USE_LST
        if (lst_dev)
          _sys_fflush(lst_dev);
#endif

      }
    } else {
      _puts("Unable to load CP/M CCP [0x02].\r\nCPU halted.\r\n");
    }
  
}

void loop(void) {
  #if XTASE_YATDB_LAYOUT
    led.clr_green();
    delay(DELAY);
    led.clr_blue();
    delay(DELAY);
    xts_handler(); // -> 
    led.clr_green();
    delay(DELAY);
    led.clr_blue();
    delay(DELAY);
    xts_handler(); // ->
  #else
    if ( LED > 0 ) {
      digitalWrite(LED, HIGH^LEDinv);
      delay(DELAY);
      digitalWrite(LED, LOW^LEDinv);
      delay(DELAY);
      digitalWrite(LED, HIGH^LEDinv);
      delay(DELAY);
      digitalWrite(LED, LOW^LEDinv);
    }
    delay(DELAY * 4);
    Serial.println("Halted");
  #endif
}
