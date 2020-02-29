/**
 * Based on putty behavior
 * 
 * 
 * Xtase - fgalliat @Feb2020
 */
#define SER_CAP_WIDTH 80
#define SER_CAP_HEIGHT 25

#define SER_DESC Serial

int con_ser_width() {
    return SER_CAP_WIDTH;
}

int con_ser_height() {
    return SER_CAP_HEIGHT;
}

void con_ser_cls() {
  // do cls
  SER_DESC.write(27);
  SER_DESC.write("[2J");

  // set cursor Home
  SER_DESC.write(27);
  SER_DESC.write("[H");
}

// 1-based
void con_ser_cursor(int row, int col) {
  // force cursor position
  SER_DESC.write(27);
  SER_DESC.write('[');
  SER_DESC.print( (row) );
  SER_DESC.write(';');
  SER_DESC.print( (col) );
  SER_DESC.write('f');
}

void con_ser_attr_accent() {
    SER_DESC.write(27);
    // SER_DESC.write("[7m"); // reverse video
    SER_DESC.write("[32m");
    // [42m -> make it green (as ACCENT color on Yatl)
}

void con_ser_attr_none() {
    SER_DESC.write(27);
    SER_DESC.write("[0m");
}