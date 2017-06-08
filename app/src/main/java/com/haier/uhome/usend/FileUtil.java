package com.haier.uhome.usend;

import android.os.Environment;

import com.haier.uhome.usend.log.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private static final String TAG = "UA-FileUtil";

    public static File getPreferFileBaseDir() {
        return Environment.getExternalStorageDirectory();
    }

    public static File openFile(String path) {
        return new File(getPreferFileBaseDir(), path);
    }

    public static String readFile(String path) {
        File file = openFile(path);
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

    public static byte[] readFile(File file) throws IOException {
        if (null == file || !file.exists()) {
            return null;
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] out = readInputStream(fileInputStream);
        fileInputStream.close();
        return out;
    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        if (null == inputStream) {
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

    public interface IReadLine {
        void readLine(String line);

        void done();
    }

    public static void readLine(String path, IReadLine handle) {

        File file = openFile(path);

        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(file);
            br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                handle.readLine(line);
            }
            Log.i(TAG, "read file done " + path);
            handle.done();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "error", e);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "error", e);
        } finally {
            try {
                if(br != null){
                    br.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
