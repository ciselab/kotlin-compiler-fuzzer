package org.fuzzer.utils;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtilities {

    public static long compareByByte(File f1, File f2) throws IOException {
        try (BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(f1));
             BufferedInputStream fis2 = new BufferedInputStream(new FileInputStream(f1))) {

            int ch = 0;
            long pos = 1;
            while ((ch = fis1.read()) != -1) {
                if (ch != fis2.read()) {
                    return pos;
                }
                pos++;
            }
            if (fis2.read() == -1) {
                return -1;
            }
            else {
                return pos;
            }
        }
    }
    public static String fileContentToString(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
