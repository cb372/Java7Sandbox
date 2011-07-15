package java7;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Created by IntelliJ IDEA.
 * User: chris
 * Date: 11/07/15
 */
public class FileIO {

    public static void main(String[] args) throws IOException {
        File file1 = new File(System.getProperty("java.io.tmpdir"), "file1.fileio");
        File file2 = new File(System.getProperty("java.io.tmpdir"), "file2.fileio");
        Path path1 = file1.toPath();
        Path path2 = file1.toPath();
        byte[] bytes = "Hello world".getBytes();
        try {
            Files.write(path1, bytes);
            Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING);
            byte[] copiedBytes = Files.readAllBytes(path2);
            System.out.println("Wrote this to a file, copied the file and read it back in 3 lines :)   " + new String(copiedBytes));
        } finally {
            try {
                Files.deleteIfExists(path1);
            } finally {
                Files.deleteIfExists(path2);
            }
        }
    }
}
