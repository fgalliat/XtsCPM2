package com.xtase.jni80;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JNI 80 - CPM Emulator ~headless(inMem) frontend <br/>
 * by Xtase - fgalliat @Apr2020 <br/>
 * cpp code from Xtase-fgalliat (XtsCPM/XtsCPM2) (based on MockbaTheBorg
 * RunCPM)<br/>
 * <br/>
 * Used as a TurboPascal3 compiler (inMem / not on disk) backend<br/>
 * <br/>
 * <br/>
 */

public class JavaRunCPM_inMEM extends JavaRunCPM implements XtsJ80System {

    // ========= Devices =============
    protected XtsJ80GenericOutputConsole console;
    protected XtsJ80Keyb keyb;
    protected XtsJ80RgbLed led;

    protected XtsJ80BdosHandler bdosHdl;
    // ===============================

    protected XtsJ80FileSystem fs = new XtsJ80FileSystem();
    protected XtsJ80MP3Player musicPlayer = null;

    @Override
    public XtsJ80FileSystem getFs() {
        return fs;
    }

    @Override
    public XtsJ80MP3Player getMusicPlayer() {
        return musicPlayer;
    }

    // ============================================

    protected boolean firstKeybRequest = true;

    protected int _ext_kbhit() {
        int res = keyb.available();
        return res;
    }

    protected List<String> cmdBuffer = new ArrayList<String>();

    void ISR_AnotherKeyReq() {

        // System.out.println("===)"+ curLine +"(<<<["+ inCompiller +"]-["+
        // atCompileTime +"]");

        if (inCompiller) {

            if (!compileStarted) {
                if (curLine.startsWith(">")) {
                    cmdBuffer.add("c"); // compile (default in Mem)

                    // the Program to compile (C:BMP.PAS -> BMP.PAS)
                    String prgmFile = fileToCompile.substring(2);

                    // cmdBuffer.add("BMP.PAS" + XtsJ80Keyb.EOL);
                    cmdBuffer.add(prgmFile + XtsJ80Keyb.EOL);

                    atCompileTime = true;
                    compileStarted = true;
                }
            } else

            if (compileStarted) {
                if (atCompileTime) {

                    // System.out.println("---)"+ curLine +"(<<<");

                    if (curLine.contains("<ESC>")) {
                        // Error case
                        cmdBuffer.add("" + ((char) 27));

                        cmdBuffer.add("" + ((char) 11)); // Ctrl + 'k'

                        cmdBuffer.add("" + ('d')); // to Exit from Editor

                        atCompileTime = false;

                        cmdBuffer.add("" + ('q')); // to Exit from TP3
                        inCompiller = false;

                        compilerErrorFound = true;

                        cmdBuffer.add("a:EXIT" + XtsJ80Keyb.EOL);
                    } else if (curLine.startsWith(">")) {
                        // Success case
                        atCompileTime = false;
                        cmdBuffer.add("q");// to Exit from TP3
                        inCompiller = false;
                        cmdBuffer.add("a:EXIT" + XtsJ80Keyb.EOL);
                    }
                }
            }
        }

        if (cmdBuffer.isEmpty()) {
            return;
        }

        String toInject = cmdBuffer.get(0);
        // System.out.println("[["+ toInject +"]]");
        keyb.injectString(toInject);
        cmdBuffer.remove(0);

        keyBuffCounter++;
    }

    // ====================================
    boolean inCompiller = false;
    boolean atCompileTime = false;
    boolean compileStarted = false;
    boolean compilerErrorFound = false;
    String compilerErrorMsg = "No Error";
    int keyBuffCounter = 0;
    // ====================================

    void ISR_gotLine(String prevLine) {

        // System.out.println("$$ "+ prevLine +" $$");

        if (!inCompiller) {
            if (prevLine.contains("TURBO Pascal system")) {
                // if (prevLine.startsWith(">")) {
                // System.out.println("$$ " + "ENTERED IN TP3" + " $$");
                inCompiller = true;
                atCompileTime = false;
                compileStarted = false;
                // return;
            }
        }

        // BEWARE w/ these flags
        if (compilerErrorFound) {
            if (prevLine.contains("Error")) {
                // System.out.println("##)");
                // System.out.println("##) --- Compiler Output --");
                // // System.out.println("##) "+prevLine );
                // // System.out.println("##)");
                // System.out.println("##)");
                compilerErrorMsg = "(EE) " + prevLine.substring(0, prevLine.indexOf("Press ")) + "\n";

                compileStarted = false;
                // int lineMarker = prevLine.indexOf("Line");
                // compilerErrorMsg += prevLine.substring( lineMarker,
                // prevLine.indexOf("Insert", lineMarker) );

                int lineMarker = prevLine.indexOf("Line");
                String fileError = prevLine.substring(lineMarker - 18, lineMarker - 1).trim();
                if (fileError.contains(":")) {
                    fileError = fileError.substring(fileError.indexOf(":") - 1); // looks for "C:filename"
                }

                lineMarker = prevLine.indexOf("Col");
                String colError = prevLine.substring(lineMarker, prevLine.indexOf("Insert", lineMarker));

                lineMarker = prevLine.indexOf("Indent");
                lineMarker = prevLine.indexOf("+", lineMarker);
                compilerErrorMsg += "(EE) " + "Line "
                        + prevLine.substring(lineMarker, prevLine.indexOf(" ", lineMarker));
                compilerErrorMsg += " " + colError + " (in " + fileError + ")";

            }
        }

    }

    // blocking char read
    protected char _ext_getch() {
        if (firstKeybRequest) {
            ISR_1stKeyReq();

            inCompiller = false;

            // goes on same drive that source code is
            // cmdBuffer.add("c:" + XtsJ80Keyb.EOL);
            cmdBuffer.add(fileToCompile.charAt(0) +":"+ XtsJ80Keyb.EOL);

            cmdBuffer.add("b:turbo" + XtsJ80Keyb.EOL);
            cmdBuffer.add("y");

            if ( !compileInMemOnly ) {
                // [O]ption / .[C]om compile / [Q]uit options
                cmdBuffer.add("ocq");
            }

            firstKeybRequest = false;
        }

        if (keyb.available() == 0) {
            ISR_AnotherKeyReq();
        }

        while (keyb.available() <= 0) {
            Zzz(5);
        }
        char res = keyb.read();
        return res;
    }

    String lastLine = "";
    String curLine = "";

    static final char EOL = '\r';
    static final char silentEOL = '\n';

    boolean silentOutput = false;
    boolean compileInMemOnly = true;

    protected void _ext_putch(char ch) {
        if (ch == silentEOL) {
        } else if (ch == EOL) {
            lastLine = "" + curLine;
            curLine = "";
            ISR_gotLine(lastLine);
        } else {
            curLine += ch;
        }
        if (!silentOutput) {
            console.getVtExtHandler().put_ch(ch);
        }
    }

    protected void _ext_coninit() {
        // System.out.println("J> Init the console.\n");
    }

    protected void _ext_conrelease() {
        // System.out.println("J> Release the console.\n");
    }

    protected void _ext_clrscr() {
        console.cls();
    }

    // ============================================

    public void halt(boolean kill) {
        if (!kill) {
            // FIXME : nice C++ code shutdown !
            return;
        }
        System.exit(0);
    }

    public void halt() {
        halt(false);
    }

    public void reboot() {
        System.out.println("-REBOOT- NYI !!");
    }

    public void delay(long millis) {
        Zzz(millis);
    }

    public void bell() {
        System.out.println("!! BELL !!");
    }

    // .....

    protected void ISR_1stKeyReq() {
        // System.out.println("Got my 1st Keyb REQUEST !!!!");

        String autorun = AUTORUN();
        if (autorun != null) {
            keyb.injectString(autorun);
        }
    }

    // the autorun sequence
    protected String AUTORUN() {
        // return "DIR"+XtsJ80Keyb.EOL;
        return null;
    }

    @Override
    public int XtsBdosCall(int reg, int value) {
        return bdosHdl.XtsBdosCall(reg, value);
    }

    // ============================================

    @Override
    public XtsJ80Keyb getKeyb() {
        return keyb;
    }

    @Override
    public XtsJ80GenericOutputConsole getConsole() {
        return console;
    }

    @Override
    public XtsJ80RgbLed getLed() {
        return led;
    }

    // ============================================

    public static void Zzz(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
        }
    }

    protected String fileToCompile = null;

    public JavaRunCPM_inMEM(String fileToCompile) {
        boolean fsValid = true;

        this.fileToCompile = fileToCompile;

        if (!fileToCompile.toUpperCase().endsWith(".PAS")) {
            System.out.println("(EE) Wrong file to compile (" + fileToCompile + ") should be .PAS");
            fsValid = false;
        }

        if (!fs.existsCPMPath(fileToCompile)) {
            System.out.println("(EE) Missing file to compile (" + fileToCompile + ")");
            fsValid = false;
        }

        // FIXME : do better
        if (!new File("./CCP-DR.60K").exists()) {
            System.out.println("(EE) Missing CCP (CCP-DR.60K)");
            fsValid = false;
        }

        if (!fs.existsCPMPath("a:exit.com")) {
            System.out.println("(EE) Missing A: System disk");
            fsValid = false;
        }

        if (!fs.existsCPMPath("b:turbo.com")) {
            System.out.println("(EE) Missing B: TP3 disk");
            fsValid = false;
        }

        // upper ensures that the path is CPM valid
        String drive = ("" + fileToCompile.charAt(0)).toUpperCase();

        if (!fs.existsCPMPath(drive + ":" + "TURBO.MSG")) {
            System.out.println("(EE) Missing TP3 .MSG file (" + drive + ":" + "TURBO.MSG" + ")");
            fsValid = false;
        }

        if (!fsValid) {
            throw new IllegalArgumentException("Some System files are missing");
        }

        // == init devices ==
        console = new XtsJ80TextOnlyOutputConsole(this);
        keyb = new XtsJ80Keyb(this);

        led = new XtsJ80RgbLed(this);
        led.setup();

        bdosHdl = new XtsJ80BdosHandler(this);
        // ==================
    }

    public static boolean compilePascalPrg(String cpmPasFile, boolean inMemOnly) throws IOException {
        if (!libraryLoaded) {
            throw new IOException("Could not load jni80 system lib.");
        }

        String fileToCompile = cpmPasFile;

        JavaRunCPM emul = new JavaRunCPM_inMEM(fileToCompile);

        ((JavaRunCPM_inMEM) emul).compileInMemOnly = inMemOnly;
        ((JavaRunCPM_inMEM) emul).silentOutput = true;

        emul.startCPM();

        // todo : detect reboot code

        if (((JavaRunCPM_inMEM) emul).compilerErrorFound) {
            System.out.println("(EE) Compilation Failed");
            System.out.println(((JavaRunCPM_inMEM) emul).compilerErrorMsg);
            throw new IOException(((JavaRunCPM_inMEM) emul).compilerErrorMsg);
        } else {
            System.out.println("(ii) Compilation Succeeded");
        }

        emul.halt();
        return true;
    }

    public static void main(String[] args) throws Exception {
        DBUG("Starting inMem version");
        String fileToCompile = null;
        if (args.length < 1) {
            fileToCompile = "C:BMP.PAS";
        } else {
            fileToCompile = args[0];
        }

        JavaRunCPM_inMEM.compilePascalPrg(fileToCompile, true);
    }

}