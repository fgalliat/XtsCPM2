
public class Main {

    static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch(Exception ex) {}
    }

    protected Console console;
    protected CPM cpm;

    public static void main(String[] args) {
        Main main = new Main();

        main.console = new Console();
        main.cpm = new CPM(main.console);


        main.setup();
        while(true) {
            main.loop();
            delay(100);
        }    
    }


    void setup() {
        console._clrscr();
        console._puts("CP/M 2.2 Emulator v"+ CPM.VERSION +" by Marcelo Dantas\r\n");
        console._puts("Arduino read/write support by Krzysztof Klis\r\n");
        console._puts("      Build " __DATE__ " - " __TIME__ "\r\n");
        console._puts("--------------------------------------------\r\n");
        console._puts("CCP: " CCPname "    CCP Address: 0x");
        console._puthex16(CCPaddr);
        console._puts("\r\nBOARD: ");
        console._puts(BOARD);
        console._puts("\r\n");
    }

    void loop() {
        System.out.println("HALTED");
    }

}