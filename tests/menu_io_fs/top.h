#if defined(__MK66FX1M0__)
    #warning "It's a Teensy 3.6"
    #define BOARD_T36 1
#else
    #error "Not a Teensy 3.6"
#endif