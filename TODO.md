 - [x] find PuttyCOM VTEsc for CLS, LOCATE
 - [x] find PuttyCOM int scan keys for arrowKeys

 - [ ] make VT100 based display when SD error (ascii art disk "picture"
   splash screen)

 - I/O modes
   - [ ] Outputs
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
     - [.] Serial in
     - [ ] Ms ChatPad via UART


 - w/ Joystick
    - [ ] adjust debounce
    - [x] add OutputConsole
      - [x] add TextBox
      - [x] make menu
      - add Serial ArrowKeys Support too
 - continue WifiFs w/ menu support
 - [x] OutputConsole.setMode(...)
 - [ ] DFPlayer

     - [x] solder
     - [x] wire
     - [x] lib w/ API
     - [ ] read JUKE.BAD again

         - [ ] update JUKE.BAD (81 songs)
         - [ ] Mp3.play() -> char* -> songname