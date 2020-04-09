import java.io.File;

// emulation of Arduino SD driver lib
public class SD {

    static boolean mkdir(String path) {
        return new File( new File("."), path ).mkdirs();
    }

    static boolean renamePathToName(String filePath, String newName) {
        File origFile = new File(filePath);
        if ( !origFile.exists() ) {
            return false;
        }
        File origPath = origFile.getParentFile();

        return origFile.renameTo( new File( origPath, newName ) );
    }

}