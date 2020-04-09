import java.io.File;

// emulation of Arduino SD driver lib
public class SD {

    static void mkdir(String path) {
        new File( new File("."), path ).mkdirs();
    }

}