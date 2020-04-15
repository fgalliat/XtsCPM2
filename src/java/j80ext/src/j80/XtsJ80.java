package j80;

import j80.vdu.XtsGraphicsCRT;

public class XtsJ80 extends J80 {


    int subSystemBdosCall(int value) {
        int HI = value / 256;
        int LO = value % 256;

        System.out.println(" Bdos subSystem [" + HI + ", " + LO + "]");

        if ( HI == 3 ) {
            int r=0;
            int g=0;
            int b=0;

            if ( (LO & 4) == 4 ) { r = 0xFF; }
            if ( (LO & 2) == 2 ) { g = 0xFF; }
            if ( (LO & 1) == 1 ) { b = 0xFF; }

            if ( this.getCRT() instanceof XtsGraphicsCRT ) {
                // System.out.println("Yeah, I've got a LED !");
                ((XtsGraphicsCRT)this.getCRT()).getLed().rgb(r, g, b);
                System.out.println("RGBLed( "+r+", "+g+", "+b+")");
            } else {
                System.out.println("RGBLed( "+r+", "+g+", "+b+")");
            }
        }

        return 0;
    }

    // Xts
    int XtsBdosCall(int reg, int value) {
        int HI = value / 256;
        int LO = value % 256;

        if (reg == 225) {
            System.out.println(" Bdos draw");
            return 0;
        } else if (reg == 226) {
            System.out.println(" Bdos console [" + HI + ", " + LO + "]");
            return 0;
        } else if (reg == 227) {
            System.out.println(" Bdos mp3 [" + HI + ", " + LO + "]");
            if (HI == 6)
                return 1;
            return 0;
        } else if (reg == 228) {
            return subSystemBdosCall(value);
            // System.out.println(" Bdos subSystem [" + HI + ", " + LO + "]");
            // return 0;
        } else if (reg == 229) {
            System.out.println(" Bdos setSystemExchangeAddr(" + value + ")");
            return 1;
        }
        return 0;
    }

String lastStr = null;

    public int inb(int port, int hi) {

        // Xts
        int reg = C; // LOW_REG(BC)
        // int value = (D*256)+E; // HIGH REG is D / LOW REG is E

// if (reg > 130) {
//     System.out.println(reg);
// }

String str = "B:"+B+" C:"+C+" D:"+D+" E:"+E+" H:"+H+" L:"+L+" / "+port+" | "+hi;

if ( !str.equals(lastStr) ) {
    if (str.contains("225")) System.out.println( str );
    lastStr = str;
}

//         /*if (reg >= 220 && reg < 225) {
//             System.out.println("Bdos Arduino(" + reg + ")");
//             return 0;
//         } else*/ if (reg >= 225 && reg <= 229) {
//             System.out.println("Bdos("+reg+", ["+D+","+E+"] ) => "+B+"->("+H+","+L+") "+port+">"+hi);
//             // HL = 0x0000; // HL is reset by the BDOS
//             // SET_LOW_REGISTER(BC, LOW_REGISTER(DE)); // C ends up equal to E

// if (false) {
//             C = E;
//             HL(0x0000);
// }
//             int DE = DE();
//             int _HL = XtsBdosCall(reg, DE); 
// if (false) {
//             HL( _HL );

//             // // CP/M BDOS does this before returning
//             // SET_HIGH_REGISTER(BC, HIGH_REGISTER(HL));
//             // SET_HIGH_REGISTER(AF, LOW_REGISTER(HL));

//             B = H;
//             A = L;
// }
//             // return A;
//             return 0x00;
//         }

        return super.inb(port, hi);
    }

    public static void main(String argv[]) {
        System.out.println("XtsJ80 starts");
        XtsJ80 cpu = null;
        try {
            System.out.println(version);
            cpu = new XtsJ80();
            if (argv.length > 0) {
                for (int i = 0; i < argv.length; i++)
                    cpu.config(argv[i]);
            } else
                cpu.config("j80.conf");

            if ( cpu.getCRT() instanceof XtsGraphicsCRT ) {
                System.out.println("Yeah, I've got a LED !");
            }

            cpu.start();

        } catch (Exception ex) {
            ex.printStackTrace();

            cpu.Error(ex);
            System.out.println(ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

}