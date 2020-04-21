import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * class that allows to load a SystemLibrary even if packaged in a jar
 */

 public class XtsJ80InJarSysLibLoader {

    // requires that libcpm80.so is copied in same dir than .class
    public static boolean loadNativeLib(String libName) throws UnsatisfiedLinkError {
        // very dirty test
        boolean windowsMode = new File("c:").exists();

        // The name of the file in resources/ dir
        String libFile = "lib" + libName + ".so";
        if (windowsMode) {
            libFile = "" + libName + ".dll";
        }

        // URL url = locator.getResource("/" + libFile);
        File nativeLibTmpFile = null;

        try {
            File tmpDir = Files.createTempDirectory("my-native-lib").toFile();
            tmpDir.deleteOnExit();
            nativeLibTmpFile = new File(tmpDir, libFile);
            nativeLibTmpFile.deleteOnExit();

            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String resourceName = libFile;
            InputStream resourceStream = loader.getResourceAsStream(resourceName);
            Files.copy(resourceStream, nativeLibTmpFile.toPath());

            // try (InputStream in = url.openStream()) {
            // try (InputStream in =  JavaRunCPM.class.getResourceAsStream( "/"+libFile )) {
            //     Files.copy(in, nativeLibTmpFile.toPath());
            // }
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new UnsatisfiedLinkError(ex.toString());
        }

        System.load(nativeLibTmpFile.getAbsolutePath());

        return true;
    }

}