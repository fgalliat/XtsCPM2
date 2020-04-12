import java.io.File;
import java.util.ArrayList;
import java.util.List;

// emulation of Arduino SD driver lib
public class SD {

    public static final int O_APPEND = 1;
    public static final int O_WRITE = 2;
    public static final int O_CREAT = 4;
    public static final int O_READ = 8;
    public static final int O_RDWR = 32;
    public static final int O_RDONLY = 64;
    public static final int FILE_READ = O_READ; // not certified

    static List<SDFile> ls(String path) {
        File[] content = new File( new File("."), path ).listFiles();
        List<SDFile> files = new ArrayList<>();
        for(File entry : content) {
            if ( entry.isDirectory() && (entry.getName().equals(".") || entry.getName().equals("..") ) ) {
              continue;  
            }
            files.add( new SDFile(path, O_READ) ); 
        }
        return files;
    }

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
        if ( (flags & O_CREAT) == O_CREAT ) {
            // if a dirname ???
            // FIXME ....
            try {
                new File(filePath).createNewFile();
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if ( (flags & O_WRITE) == O_WRITE ) {
            if ( (flags & O_APPEND) == O_APPEND ) {
                // FIXME ....
                try {
                    new File(filePath).createNewFile();
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            // do not check if exists
        } else if ( (flags & O_RDONLY) == O_RDONLY ) {
            if ( !new File(filePath).exists() ) {
                return null;
            }
        }
        return new SDFile(filePath, flags);
    }

    static boolean truncate(SDFile f, int clustSize) {
        return f.truncate(clustSize);
    }

}