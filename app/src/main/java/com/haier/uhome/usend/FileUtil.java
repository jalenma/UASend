package com.haier.uhome.usend;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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

        try {
            return new String(readFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readFile (File file) throws IOException{
        if(null == file || !file.exists()){
            return null;
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        return readInputStream(fileInputStream);
    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException{
        if(null == inputStream){
            return null;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copy(inputStream, byteArrayOutputStream);
        byte[] out = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return out;
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        int count = 0;
        int length;
        byte[] buffer = new byte[4 * 1024];
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
            count += length;
        }
        output.flush();
        return count;
    }
}
