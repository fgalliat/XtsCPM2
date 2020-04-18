public interface XtsJ80System {

    XtsJ80Video getVideo();

    XtsJ80Keyb getKeyb();

    void halt();
    void reboot();

    void bell();

    void delay(long millis);

}