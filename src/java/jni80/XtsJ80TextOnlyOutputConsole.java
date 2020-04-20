public class XtsJ80TextOnlyOutputConsole implements XtsJ80GenericOutputConsole {

    protected XtsJ80System system;
    protected XtsJ80VTExtHandler consoleEmulator;

    public XtsJ80TextOnlyOutputConsole(XtsJ80System system) {
        this.system = system;
        this.consoleEmulator = new XtsJ80VTExtHandler(this);
    }

    @Override
    public void reset() {
    }

    @Override
    public void setup() {
    }

    @Override
    public void cls() {
    }

    @Override
    public void write(char ch) {
        System.out.print(ch);
    }

    @Override
    public void br() {
        System.out.print('\n');
    }

    @Override
    public void backspace() {
        // TODO ?
    }

    @Override
    public void bell() {
    }

    @Override
    public void cursor(int col, int row) {
    }

    @Override
    public void charAttr(int attrValue) {
    }

    @Override
    public void eraseUntilEOL() {
    }

    @Override
    public XtsJ80VTExtHandler getVtExtHandler() {
        return consoleEmulator;
    }

}