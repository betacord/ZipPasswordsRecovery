package sample;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by t-krz on 10.02.2017.
 */
public class PasswordHelper {

    public static boolean checkPasswordZIP(String path, String password, String tmpDir) {

        try {
            ZipFile zipFile = new ZipFile(path);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(Paths.get("").toAbsolutePath().toString() + "/" + tmpDir);
            return true;
        } catch (ZipException e) {
            return false;
        } finally {
            if (new File(Paths.get("").toAbsolutePath().toString() + "/" + tmpDir).exists()) {
                DirectoryHelper.deleteDirectory(new File(Paths.get("").toAbsolutePath().toString() + "/" + tmpDir));
            }
        }
    }
}
