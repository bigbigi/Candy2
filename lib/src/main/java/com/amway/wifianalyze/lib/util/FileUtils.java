package com.amway.wifianalyze.lib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by big on 2018/11/14.
 */

public class FileUtils {
    public static void clearDir(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        clearDir(f.getAbsolutePath());
                    } else {
                        f.delete();
                    }
                }
            }
        }
    }

    public static void mkdirs(String dir) {
        File file = new File(dir);
        // 如果目录不中存在，创建这个目录
        if (!file.exists())
            file.mkdirs();
    }

    public static void closeIO(Closeable ioStream) {
        if (ioStream != null) {
            try {
                ioStream.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = fis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(fis);
            closeIO(bos);
        }
        return buffer;
    }

}
