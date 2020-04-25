package com.xtase.jni80;

/**
 * 
 * XtsYATxx specific Bdos Calls Handler
 * 
 * Xtase-fgalliat @Apr2020
 */

public class XtsJ80BdosHandler {

    protected XtsJ80System system;

    public XtsJ80BdosHandler(XtsJ80System system) {
        this.system = system;
    }

    protected String readPascalStringFromRam(int addr) {
        int str1stByte = system.readRAM(addr); // in TP3 : 1st byte (str[0]) is the str.length();
        String result = "";
        for (int i = 0; i < str1stByte; i++) {
            result += (char) system.readRAM(addr + 1 + i);
        }
        return result;
    }

    public int drawingShapesBdos(String test) {
        int OpType = test.charAt(1);
        int shapeType = test.charAt(2);
        int fillType = test.charAt(3); // 0 draw / 1 fill

        // BEWARE : color must be mapped
        int color = (test.charAt(4) << 8) + test.charAt(5);

        if (OpType == 0x7F) {
            // drawShapes
            int x = (test.charAt(6) << 8) + test.charAt(7);
            int y = (test.charAt(8) << 8) + test.charAt(9);
            if (shapeType == 0x01) {
                // Shape : rectangle
                int w = (test.charAt(10) << 8) + test.charAt(11);
                int h = (test.charAt(12) << 8) + test.charAt(13);
                if (fillType == 0x00) {
                    // draw outlines
                    // screen.drawRect( x, y, w, h, color );
                    System.out.println("drawRect(" + x + ", " + y + ", " + w + ", " + h + ", " + color + ")");
                } else if (fillType == 0x01) {
                    // fills the rect
                    // screen.fillRect( x, y, w, h, color );
                    System.out.println("fillRect(" + x + ", " + y + ", " + w + ", " + h + ", " + color + ")");
                }
            } else if (shapeType == 0x02) {
                // Shape : circle
                int r = (test.charAt(10) << 8) + test.charAt(11);
                if (fillType == 0x00) {
                    // draw outlines
                    // screen.drawCircle( x,y,r, color );
                    System.out.println("drawCircle(" + x + ", " + y + ", " + r + ", " + color + ")");
                } else {
                    // screen.fillCircle( x,y,r, color );
                    System.out.println("fillCircle(" + x + ", " + y + ", " + r + ", " + color + ")");
                }
            } else if (shapeType == 0x03) {
                // Shape : line
                int x2 = (test.charAt(10) << 8) + test.charAt(11);
                int y2 = (test.charAt(12) << 8) + test.charAt(13);
                // screen.drawLine( x, y, x2, y2, color );
                System.out.println("drawLine(" + x + ", " + y + ", " + x2 + ", " + y2 + ", " + color + ")");
            }
        } else if (OpType == 0x80) {
            // manage Sprite
            int x = (test.charAt(6) << 8) + test.charAt(7);
            int y = (test.charAt(8) << 8) + test.charAt(9);

            if (shapeType == 0x01) {
                // define sprite
                int w = (test.charAt(10) << 8) + test.charAt(11);
                int h = (test.charAt(12) << 8) + test.charAt(13);

                int num = test.charAt(14);

                // screen.defineSprite(num, x, y, w, h);
                System.out.println("defineSprite(" + num + ", " + x + ", " + y + ", " + w + ", " + h + ");");
            } else if (shapeType == 0x02) {
                // draw sprite
                int num = test.charAt(10);

                // screen.drawSprite(num, x, y);
                System.out.println("drawSprite(" + num + ", " + x + ", " + y + ");");
            }

        }

        return 0;
    }

    public int drawingBdos(int value) {
        String ramString = readPascalStringFromRam(value);
        // BEWARE w/ drawing spe chars (may have UTF-conv. ???)

        if (ramString.charAt(1) >= 127) { // [0][1][2] => real Op descriptor
            // System.out.println("drawShapes from Bdos");
            return drawingShapesBdos(ramString);
        } else {
            System.out.println("Pascal String => [" + system.readRAM(value + 1) + "] '" + ramString + "'");
        }

        return 0;
    }

    public int XtsBdosCall(int reg, int value) {

        if (reg == 225) {
            return drawingBdos(value);
        }

        if (reg == 228) {
            int HI = value / 256;
            int LO = value % 256;

            if (HI == 3) {
                int r = 0, g = 0, b = 0;
                if ((LO & 1) == 1) {
                    r = 0xFF;
                }
                if ((LO & 2) == 2) {
                    g = 0xFF;
                }
                if ((LO & 4) == 4) {
                    b = 0xFF;
                }

                system.getLed().rgb(r, g, b);
            }
        }

        return 0;
    }

}