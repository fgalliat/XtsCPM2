 - [x] find PuttyCOM VTEsc for CLS, LOCATE
 - [x] find PuttyCOM int scan keys for arrowKeys

 - [ ] make VT100 based display when SD error (ascii art disk "picture"
   splash screen)

 - I/O modes
   - [ ] Outputs
     - [.] Serial Stream
     - [.] Serial VT100
     - [.] Screen (tft) w/ VT-Ext Support
       - look if any scrollTop method... => none still have to emulate ttyConsoleMEMSEG
       - 2x 3200bytes for 80x40 chars
       - text w/ background color
         - void drawChar(uint16_t x, uint16_t y, char c, uint16_t color, uint16_t bg, uint8_t size);
         - where color could be colors[ attr[ (y\*w)+x ] ] & bg = bg_colors[ attr[ (y*w)+x ] ]
         - instead of using 'if'....
   - [ ] Inputs
     - [.] Serial in
     - [ ] Ms ChatPad via UART


 - w/ Joystick
    - fix debounce
    - add OutputConsole
      - add TextBox
      - make menu
      - add Serial ArrowKeys Support too
 - continue WifiFs w/ menu support
 - OutputConsole.setMode(...)