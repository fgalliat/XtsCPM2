 - [x] find PuttyCOM VTEsc for CLS, LOCATE
 - [x] find PuttyCOM int scan keys for arrowKeys
 - [ ] WiFi
    - [ ] menu
       - [x] add loading / connecting indicators (@least w/ RGB led ?)
       - [x] add SSID choice
       - [ ] ? add SSID free value cap. ?
    - [x] access to HomeServer
       - [x] connect to AP
       - [x] isAtHome ?
       - [x] wget() w/ Bearer key
          - [x] Temp measure
          - [x] Rss titles
          - [ ] Arduinos tour
 - [ ] make VT100 based display when SD error (ascii art disk "picture"
   splash screen)
 - I/O modes
   - [x] Outputs
     - [x] Serial Stream
     - [x] Serial VT100
     - [x] Screen (tft) w/ VT-Ext Support
       - [x] look if any scrollTop method... => none still have to emulate ttyConsoleMEMSEG
       - [x] 2x 3200bytes for 80x40 chars
       - [x] text w/ background color
         - [x] void drawChar(uint16_t x, uint16_t y, char c, uint16_t color, uint16_t bg, uint8_t size);
         - [x] where color could be colors[ attr[ (y\*w)+x ] ] & bg = bg_colors[ attr[ (y*w)+x ] ]
         - [x] instead of using 'if'....
   - [ ] Inputs
     - [x] [.] Serial in -> still todo : arrow Keys => Joystick control
     - [x] Analog Stick + btns
     - [ ] Ms ChatPad via UART


 - w/ Joystick
    - [ ] adjust debounce
    - [x] add OutputConsole
      - [x] add TextBox
      - [x] make menu
      - [ ] add Serial ArrowKeys Support too
 - continue WifiFs w/ menu support
 - [x] OutputConsole.setMode(...)
 - [ ] DFPlayer

     - [x] solder
     - [x] wire
     - [x] lib w/ API
     - [ ] read JUKE.BAD again (see mem consumption ...)

         - [ ] update JUKE.BAD (81 songs)
         - [ ] Mp3.play() -> char* -> songname