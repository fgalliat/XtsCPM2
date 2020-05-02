package com.xtase.jni80;

import java.util.regex.Pattern;

/**
 * 
 * XtsYATxx specific Bdos Calls Handler
 * 
 * Xtase-fgalliat @Apr2020
 */

public class XtsJ80BdosHandler {

    protected XtsJ80System system;
    protected XtsJ80ImageDecoder imageDecoder = null;
    protected XtsJ80FileSystem fs;

    public XtsJ80BdosHandler(XtsJ80System system) {
        this.system = system;
        fs = system.getFs();
        if (system.getConsole() instanceof XtsJ80Video) {
            try {
                imageDecoder = new XtsJ80ImageDecoder((XtsJ80Video) system.getConsole(), fs, system.getLed());
            } catch (Exception ex) {
                System.out.println("(!!) Failed to instanciate ImageDecoder (" + ex.toString() + ")");
            }
        }
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

                    if (system.getConsole() instanceof XtsJ80Video) {
                        ((XtsJ80Video) system.getConsole()).drawRect(x, y, w, h, color);
                    } else {
                        System.out.println("drawRect(" + x + ", " + y + ", " + w + ", " + h + ", " + color + ")");
                    }

                } else if (fillType == 0x01) {
                    // fills the rect

                    if (system.getConsole() instanceof XtsJ80Video) {
                        ((XtsJ80Video) system.getConsole()).fillRect(x, y, w, h, color);
                    } else {
                        System.out.println("fillRect(" + x + ", " + y + ", " + w + ", " + h + ", " + color + ")");
                    }

                }
            } else if (shapeType == 0x02) {
                // Shape : circle
                int r = (test.charAt(10) << 8) + test.charAt(11);
                if (fillType == 0x00) {
                    // draw outlines

                    if (system.getConsole() instanceof XtsJ80Video) {
                        ((XtsJ80Video) system.getConsole()).drawCircle(x, y, r, color);
                    } else {
                        System.out.println("drawCircle(" + x + ", " + y + ", " + r + ", " + color + ")");
                    }

                } else {

                    if (system.getConsole() instanceof XtsJ80Video) {
                        ((XtsJ80Video) system.getConsole()).fillCircle(x, y, r, color);
                    } else {
                        System.out.println("fillCircle(" + x + ", " + y + ", " + r + ", " + color + ")");
                    }

                }
            } else if (shapeType == 0x03) {
                // Shape : line
                int x2 = (test.charAt(10) << 8) + test.charAt(11);
                int y2 = (test.charAt(12) << 8) + test.charAt(13);

                if (system.getConsole() instanceof XtsJ80Video) {
                    ((XtsJ80Video) system.getConsole()).drawLine(x, y, x2, y2, color);
                } else {
                    System.out.println("drawLine(" + x + ", " + y + ", " + x2 + ", " + y2 + ", " + color + ")");
                }

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

                // System.out.println("defineSprite(" + num + ", " + x + ", " + y + ", " + w +
                // ", " + h + ");");
                imageDecoder.defineSprite(num, x, y, w, h);
            } else if (shapeType == 0x02) {
                // draw sprite
                int num = test.charAt(10);

                // screen.drawSprite(num, x, y);
                // System.out.println("drawSprite(" + num + ", " + x + ", " + y + ");");
                imageDecoder.drawSprite(num, x, y);
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
            // System.out.println("Pascal String => [" + system.readRAM(value + 1) + "] '" +
            // ramString + "'");

            /*
             * if (ramString.startsWith("!")) {
             * System.out.println("Sprite loading => ...."); } else
             */ {
                // TODO better + no multiple instance of ImageDecoder nor Fs
                XtsJ80FileSystem fs = new XtsJ80FileSystem();

                if (ramString.toUpperCase().endsWith(".PAK")) {

                    String test = ramString;
                    int numImg = (int) test.charAt(0) - 1; // 1 based
                    int x = -1;
                    int y = -1; // centered

                    String filename = test.substring(1);
                    int tmp;
                    if ((tmp = test.indexOf(',', 1)) > -1) {
                        String[] tks = filename.split(Pattern.quote(","));

                        x = Integer.parseInt(tks[0]);
                        y = Integer.parseInt(tks[1]);
                        filename = tks[2];
                    }

                    // System.out.println("draw Pak : ("+filename+") @"+x+", "+y);

                    try {
                        imageDecoder.drawPakFile(filename, x, y, numImg);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (ramString.toUpperCase().endsWith(".BMP")) {
                    try {
                        if (ramString.startsWith("!")) {
                            imageDecoder.loadBMPSpriteBoard(ramString.substring(1));
                        } else {
                            imageDecoder.drawBitmapFile(ramString, 0, 0, true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("Unknwon image format (" + ramString + ")");
                }

            }

        }

        return 0;
    }

    public int mp3Bdos(int value) {
        int a0 = value / 256;
        int a1 = value % 256;

        if (system.getMusicPlayer() == null) {
            System.out.println("(!!) no music player on this system");
            return 0;
        }

        XtsJ80GenericOutputConsole console = system.getConsole();
        XtsJ80MP3Player snd = system.getMusicPlayer();

        try {

            if (a0 >= (1 << 6)) {
                // 11000000 -> 11 play+loop -> 64(10)
                // still 16000 addressable songs
                boolean loopMode = a0 >= (1 << 7);

                if (a0 >= 128) {
                    a0 -= 128;
                }

                a0 -= 64;
                int trkNum = (a0 << 8) + a1;

                if (loopMode)
                    console.warn("mp3 LOOP NYI");

                if (loopMode) {
                    ;
                } else {
                    snd.playTrack(trkNum);
                }
            } else if (a0 == 0x00) {
                snd.stop();
            } else if (a0 == 0x01) {
                snd.pause();
            } else if (a0 == 0x02) {
                snd.next();
            } else if (a0 == 0x03) {
                snd.prev();
            } else if (a0 == 0x04) {
                // snd.volume( a1 ); // FIXME
            } else if (a0 == 0x05) {
                // for now : just for demo
                snd.playTrack(65);
            } else if (a0 == 0x06) {
                return snd.isPlaying() ? 1 : 0;
            }

        } catch (Exception ex) {
            console.warn("Error in MP3 control");
            System.out.println("(!!) Error in MP3 control");
        }

        return 0;
    }

    protected final int MEMXCHANGE_NOTINIT = 0;
    protected int memXchangeAddr = MEMXCHANGE_NOTINIT;

    protected int setSystemExchangeAddr(int addr) {
        memXchangeAddr = addr;
        return 1;
    }

    protected int subSystemBdosCall(int value) {
        int hiB = (value / 256);

        if (hiB == 0) {
            int volts = (int) 0xFF; // 5v
            return volts;
        } else if (hiB == 1) {
            system.reboot();
            return 0;
        } else if (hiB == 2) {
            system.halt();
            return 0;
        } else if (hiB == 3) {
            int LO = value % 256;
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
            return 0;
        } else if (hiB == 4) {
            System.out.println("(!!) DownloadFromSerial NYI");
        } else if (hiB == 5) {
            System.out.println("(!!) DownloadFromWiFi NYI");
        } else if (hiB == 6) {
            int LO = value % 256;
            int timeToSleep = LO * 10;
            system.delay( timeToSleep );
        } else if (hiB == 64) {
            System.out.println("(!!) TelnetServer NYI");
            return 0;
        } else if (hiB == 65) {
            System.out.println("(!!) Get/IP -MEMXCHANGE- NYI");
            return 0;
        } else if (hiB == 66) {
            System.out.println("(!!) Get/SSID -MEMXCHANGE- NYI");
            return 0;
        } else if (hiB == 67) {
            System.out.println("(!!) Connect/SSID NYI");
            return 0;
        } else if (hiB == 68) {
            System.out.println("(!!) GetKnown/SSID NYI");
            return 0;
        } else if (hiB == 69) {
            System.out.println("(!!) GetAvailable/SSID NYI");
            return 0;
        } else if (hiB == 70) {
            System.out.println("(!!) Open SoftAP NYI");
            return 0;
        } else if (hiB == 71) {
            System.out.println("(!!) isAtHome NYI");
            return 1;
        } else if (hiB == 72) {
            System.out.println("(!!) wget NYI");
            return 0;
        }

        else if ( hiB == 80 ) {
            // Joypad reading
            int jPos = 0;
            /*
              8|1|2
              7|0|3
              6|5|4
            */
    
            XtsJ80Joypad pad = system.getJoypad();
            if ( pad.isDirUp() && pad.isDirLeft() ) { jPos = 8; }
            else if ( pad.isDirUp() && pad.isDirRight() ) { jPos = 2; }
            else if ( pad.isDirUp() ) { jPos = 1; }

            else if ( pad.isDirDown() && pad.isDirLeft() ) { jPos = 6; }
            else if ( pad.isDirDown() && pad.isDirRight() ) { jPos = 4; }
            else if ( pad.isDirDown() ) { jPos = 5; }

            else if ( pad.isDirLeft() ) { jPos = 7; }
            else if ( pad.isDirRight() ) { jPos = 3; }
    
            int jBtn = 0;
            if ( pad.isBtnB1() ) { jBtn += 1; }
            if ( pad.isBtnB2() ) { jBtn += 2; }
            if ( pad.isBtnMenu() ) { jBtn += 4; }

            int result = (jPos * 16) + jBtn;

            return result;
          }

        return 0;
    }



    public int XtsBdosCall(int reg, int value) {

        if (reg == 225) {
            return drawingBdos(value);
        }

        if (reg == 227) {
            return mp3Bdos(value);
        }

        if (reg == 228) {
            return subSystemBdosCall(value);
        }

        if (reg == 229) {
            return setSystemExchangeAddr(value);
        }

        return 0;
    }

}