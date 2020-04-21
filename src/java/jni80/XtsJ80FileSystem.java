import java.io.File;

/**
 * 
 * Generic Output Console interface
 * 
 * Xtase-fgalliat @Apr2020
 */

public class XtsJ80FileSystem {

    // invariable for now cf Cpp code
    protected File fsRoot = new File("./");

    public XtsJ80FileSystem() {
    }

    /** 'c:BmP.pas' -> './C/0/BMP.PAS' */
    public File resolveCPMPath(String cpmPath) throws IllegalArgumentException {
        cpmPath = cpmPath.toUpperCase();
        if (cpmPath.charAt(1) != ':') {
            throw new IllegalArgumentException("Path is invalid (?:????????.???)");
        }

        char drive = cpmPath.charAt(0);
        if (drive < 'A' || drive > 'Z') {
            throw new IllegalArgumentException("Drive is invalid ([A:-Z:])");
        }

        if (cpmPath.length() > 1 + 1 + 8 + 1 + 3) {
            throw new IllegalArgumentException("fileName is invalid (????????.???)");
        }

        int userSelect = 0;
        String filename = cpmPath.substring(2);

        return new File(fsRoot, drive + "/" + userSelect + "/" + filename);
    }

    public boolean existsCPMPath(String cpmPath) throws IllegalArgumentException {
        return resolveCPMPath(cpmPath).exists();
    }

}