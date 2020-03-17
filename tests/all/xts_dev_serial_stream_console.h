/**
 * Based on Arduino Serial Monitor behavior
 * raw stream console
 * 
 * Xtase - fgalliat @Feb2020
 */
#define DUM_CAP_WIDTH 80
#define DUM_CAP_HEIGHT 40

#define DUM_DESC Serial

int con_dum_width() {
    return DUM_CAP_WIDTH;
}

int con_dum_height() {
    return DUM_CAP_HEIGHT;
}

void con_dum_init() {
  DUM_DESC.begin(115200);
}

bool con_dum_ready() {
  if ( DUM_DESC ) { return true; }
  return false;
}

Stream* con_dum() {
  return &DUM_DESC;
}


void con_dum_cls() {
  for(int y=0; y < DUM_CAP_HEIGHT; y++)
  DUM_DESC.write("\r\n");
}

// 1-based
void con_dum_cursor(int row, int col) {
}

void con_dum_attr_accent() {
}

void con_dum_attr_none() {
}