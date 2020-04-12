
public class Main {

    static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch(Exception ex) {}
    }

    protected Console console;
    protected CPM cpm;
    protected CPU cpu;

    public static void main(String[] args) {
        Main main = new Main();

        main.console = new Console();
        main.cpm = new CPM(main.console);
        main.cpu = main.cpm.cpu;


        main.setup();
        while(true) {
            main.loop();
            delay(100);
        }    
    }


    void setup() {
        console._clrscr();
        console._puts("CP/M 2.2 Emulator v"+ CPM.VERSION +" by Xtase (based on Marcelo Dantas)\r\n");
        console._puts("Arduino read/write support by Krzysztof Klis\r\n");
        console._puts("Java read/write support by Xtase - fgalliat\r\n");
        // console._puts("      Build " __DATE__ " - " __TIME__ "\r\n");
        console._puts("--------------------------------------------\r\n");
        console._puts("CCP: "+ cpm.CCPname +"    CCP Address: 0x");
        console._puthex16(cpm.CCPaddr);
        console._puts("\r\nBOARD: ");

        String BOARD = "Teensy 3.6";

        console._puts(BOARD);
        console._puts("\r\n");

        String ccpFileName = cpm.CCPname;

        if (cpm.VersionCCP >= 0x10 || SD.exists(ccpFileName)) {
            while (true) {
                // _puts(cpm.CCPHEAD);
                cpm._PatchCPM();
            cpu.setStatus(0);
        // #ifndef CCP_INTERNAL
                // if (!_RamLoad((char *)CCPname, CCPaddr)) {
                if (!cpm.disk._RamLoad(ccpFileName, cpm.CCPaddr)) {
                  console._puts("Unable to load the CCP [0x01].\r\nCPU halted.\r\n");
                  break;
                }
                cpu.Z80reset();

                DataUtils.SET_LOW_REGISTER(cpu.BC, cpu.mem._RamRead(0x0004));
System.out.println("RAM(4): "+ (int)cpu.mem._RamRead(0x0004)); 


System.out.println("CCPaddr: "+cpm.CCPaddr); 

                cpu.PC.set( cpm.CCPaddr );

// System.exit(0);

                cpu.Z80run();
                System.out.println( "CPU status : "+ cpu.getStatus() );
        // #else
        //         _ccp();
        // #endif
                if (cpu.getStatus() == 1)
                  break;
        // #ifdef USE_PUN
        //         if (pun_dev)
        //           _sys_fflush(pun_dev);
        // #endif
        // #ifdef USE_LST
        //         if (lst_dev)
        //           _sys_fflush(lst_dev);
        // #endif
        
              }
            } else {
              console._puts("Unable to load CP/M CCP [0x02].\r\nCPU halted.\r\n");
            }
        
    }

    void loop() {
        System.out.println("HALTED");
    }

}