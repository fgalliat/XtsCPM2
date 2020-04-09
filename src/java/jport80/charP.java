// char*, uint8*
public class charP {
    char[] ptr = null;
    int ptrA = 0; // cursor 

    public charP(int len) {
        ptr = new char[len];
    }

    public charP(char[] content) {
        ptr = content;
    }

    public charP(String content) {
        ptr = content.toCharArray();
    }

    charP reset() { ptrA = 0; return this; }

    char get() { return ptr[ ptrA ]; }
    char get(int addr) { return ptr[ addr ]; }
    void set(char x) { ptr[ ptrA ] = x; }
    void set(int addr, char x) { ptr[ addr ] = x; }
    char inc(int i) { ptrA+=i; return get(); }
    char dec(int i) { ptrA-=i; return get(); }
    char inc() { return inc(1); }
    char dec() { return dec(1); }

    public String toString() {
        return new String( ptr );
    }
}