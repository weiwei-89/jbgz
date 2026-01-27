package cn.tj.food.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class FileWriter {
    public static void write(byte[] data, String path, String fileName) throws Exception {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(
                    new FileOutputStream(String.format("%s%s%s", path, File.separator, fileName))
            );
            out.write(data);
            out.flush();
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }
}