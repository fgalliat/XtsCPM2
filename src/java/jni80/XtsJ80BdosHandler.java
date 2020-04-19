public class XtsJ80BdosHandler {

    protected XtsJ80System system;

    public XtsJ80BdosHandler(XtsJ80System system) {
        this.system = system;
    }


    protected String readPascalStringFromRam(int addr) {
        int str1stByte = system.readRAM(addr); // in TP3 : 1st byte (str[0]) is the str.length();
        String result = "";
        for(int i=0; i < str1stByte; i++) {
            result += (char)system.readRAM(addr+1+i);
        }
        return result;
    }


    public int drawingBdos(int value) {
        String ramString = readPascalStringFromRam(value);
        // BEWARE w/ drawing spe chars (may have UTF-conv. ???)

        if ( ramString.charAt(1) >= 127 ) { // [0][1][2] => real Op descriptor
            // System.out.println("drawShapes from Bdos");
        } else {
            System.out.println( "Pascal String => ["+ system.readRAM(value+1) +"] '"+ramString+"'" );
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