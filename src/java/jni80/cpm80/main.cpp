#include "cpm_globals.h"

#include "xts_string.h"

// =============================================================
#include "cpm_abstraction_posix.h"

// AUX: device configuration
#ifdef USE_PUN
FILE *pun_dev;
int pun_open = FALSE;
#endif

// PRT: device configuration
#ifdef USE_LST
FILE *lst_dev;
int lst_open = FALSE;
#endif

inline bool fileSystem_exists (const char* name) {
    if (FILE *file = fopen(name, "r")) {
        fclose(file);
        return true;
    } else {
        return false;
    }   
}
// =============================================================

#include "cpm_ram.h"
#include "cpm_console.h"
#include "cpm_cpu.h"
#include "cpm_disk.h"
#include "cpm_host.h"
#include "cpm_cpm.h"
#ifdef CCP_INTERNAL
#include "cpm_ccp.h"
#endif

void setup() {

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

    // if (VersionCCP >= 0x10 || fileSystem.exists(ccpFileName)) {
    if (VersionCCP >= 0x10 || fileSystem_exists(ccpFileName)) {
      while (true) {
        _puts(CCPHEAD);
        _PatchCPM();
	Status = 0;
#ifndef CCP_INTERNAL
        // if (!_RamLoad((char *)CCPname, CCPaddr)) {
        if (!_RamLoad((uint8 *)ccpFileName, CCPaddr)) {
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

int main(int argc, char *argv[]) {
    _console_init();

    setup();

    _console_reset();
	_puts("\r\n");
	return(0);
}