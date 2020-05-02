#include "cpm_globals.h"

/**
 * TODO : 
 * cpm.h -> re-handle specific BdosCalls
 * abstraction_arduino.h -> refactor DRIVE_LED calls
 *                       -> refactor _kbhit, getch, getche
 * 
 * Xtase - fgalliat @ Mar2020
 * 
 * after 1st version of cleaned code :
 * Le croquis utilise 78064 octets (7%) de l'espace de stockage de programmes. Le maximum est de 1048576 octets.
 * Les variables globales utilisent 72484 octets (27%) de mémoire dynamique, ce qui laisse 189660 octets pour les variables locales. Le maximum est de 262144 octets.
 * 
 */

#if XTASE_YATDB_LAYOUT
  // ======== TEMP SD access resources =========
  #include <SPI.h>
  #include "SdFat.h"
  #include "sdios.h"

  #define FS_CLASS SdFat
  FS_CLASS SD;
  // ===========================================
  // RGBLed -> r
  #define LED 21
  #define LEDinv false
  bool Serial_useable = true;
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

void setup(void) {
  // ==== TEMP pin-init code ==== 
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
  Serial.begin(SERIALSPD);
  // ============================

  // ==== TEMP SD init code =====
  _puts("Initializing SD card.\r\n");
  #if XTASE_YATDB_LAYOUT
    bool sdOk = SD.begin(BUILTIN_SDCARD);
    if ( !sdOk ) {
      // _puts("Unable to initialize SD card [0x03].\r\nCPU halted.\r\n");
      Serial.println("Could not init the SD card, emulator will never run ...");
      Serial.println("Halting ...");
      while( true ) { delay(10000); }
    }
  #endif
  // ============================

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
    if (VersionCCP >= 0x10 || SD.exists(ccpFileName)) {
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
  #if YATL_PLATFORM
    yatl.blink(3);
    delay(500);
  #elif YAEL_PLATFORM
    yael_led(true);
    delay(DELAY);
    yael_led(false);
    delay(DELAY);
    yael_led(true);
    delay(DELAY);
    yael_led(false);
    delay(DELAY);
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
