package com.haier.uhome.usend;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Author: majunling
 * @Data: 2016/6/13
 * @Description:
 */
public class FileUtil {

    public static final String FILE_PATH = "/haier/users.txt";

    private static File getUserFile(String path) {
        return new File(Environment.getExternalStorageDirectory(), path);
    }

    public static String getAllUserId() {
        File file = getUserFile(FILE_PATH);
        if (!file.exists()) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String strLine = null;
            while ((strLine = reader.readLine()) != null) {
                buffer.append(strLine).append(",");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
