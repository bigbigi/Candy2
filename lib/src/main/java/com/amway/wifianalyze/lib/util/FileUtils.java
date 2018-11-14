package com.amway.wifianalyze.lib.util;

import java.io.File;

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

}
