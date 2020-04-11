import java.io.File;

// emulation of Arduino SD driver lib
public class SD {

    public static final int O_APPEND = 1;
    public static final int O_WRITE = 2;
    public static final int O_CREAT = 4;

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

    static boolean exists(String filePath) {
        File origFile = new File(filePath);
        if ( !origFile.exists() ) {
            return false;
        }
        return true;
    }


    static boolean remove(String filePath) {
        File origFile = new File(filePath);
        if ( !origFile.delete() ) {
            return false;
        }
        return true;
    }

    static SDFile open(String filePath, int flags) {
        if ( (flags & O_WRITE) == O_WRITE ) {
            // do not check if exists
        }
        return new SDFile(filePath, flags);
    }

    static boolean truncate(SDFile f, int clustSize) {
        return f.truncate(clustSize);
    }

}