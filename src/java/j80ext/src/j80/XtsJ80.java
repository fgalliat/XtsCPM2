package j80;

import j80.vdu.XtsGraphicsCRT;

public class XtsJ80 extends J80 {

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
            System.out.println(" Bdos subSystem [" + HI + ", " + LO + "]");
            return 0;
        } else if (reg == 229) {
            System.out.println(" Bdos setSystemExchangeAddr(" + value + ")");
            return 1;
        }
        return 0;
    }

    public int inb(int port, int hi) {

        // Xts
        int reg = C; // LOW_REG(BC)
        // int value = (D*256)+E; // HIGH REG is D / LOW REG is E

        if (reg >= 220 && reg < 225) {
            System.out.println("Bdos Arduino(" + reg + ")");
            return 0;
        } else if (reg >= 225 && reg <= 229) {
            // System.out.println("Bdos("+reg+", "+value+" ["+D+","+E+"] )");
            // display = true;

            // HL = 0x0000; // HL is reset by the BDOS
            // SET_LOW_REGISTER(BC, LOW_REGISTER(DE)); // C ends up equal to E
            C = E;

            int DE = (D * 256) + E;
            int HL;

            HL = XtsBdosCall(reg, DE);

            H = HL / 256;
            L = HL % 256;

            // // CP/M BDOS does this before returning
            // SET_HIGH_REGISTER(BC, HIGH_REGISTER(HL));
            // SET_HIGH_REGISTER(AF, LOW_REGISTER(HL));
            B = H;
            A = L;

            return A;
        }

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