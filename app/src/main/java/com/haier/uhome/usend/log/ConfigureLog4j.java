package com.haier.uhome.usend.log;

import android.os.Environment;

import org.apache.log4j.Level;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4j {
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 5;
    private static final String DEFAULT_LOG_FILE_NAME = "uasend.dat";

    private static final String TAG = "Uplus.ConfigureLog4j";

    public static void configure(String fileName) {
        final LogConfigurator logConfigurator = new LogConfigurator();
        try {
            if (isSdcardMounted()) {
                logConfigurator.setFileName(Environment.getExternalStorageDirectory()
                                + File.separator + "ua_jalen" + File.separator + "send"
                                + File.separator + fileName);
            } else {
                logConfigurator.setFileName("/data/data/com.haier.uhome.usend/files"
                                + File.separator + fileName);
            }
            logConfigurator.setMaxBackupSize(3);
            logConfigurator.setRootLevel(Level.DEBUG);
            logConfigurator.setFilePattern("%d\t%p/%c:\t%m%n");
            logConfigurator.setMaxFileSize(MAX_FILE_SIZE);
            logConfigurator.setImmediateFlush(true);
            logConfigurator.configure();
            android.util.Log.e(TAG, "Log4j config finish");
        } catch (Throwable throwable) {
            logConfigurator.setResetConfiguration(true);
            android.util.Log.e(TAG, "Log4j config error, use default config. Error:" + throwable);
        }
    }

    public static void configure() {
        configure(DEFAULT_LOG_FILE_NAME);
    }

    private static boolean isSdcardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}