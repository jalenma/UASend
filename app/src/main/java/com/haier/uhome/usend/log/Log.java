package com.haier.uhome.usend.log;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log {

    /** log打印开关 */
    public static final boolean SWITCH_LOG = true;

    private static boolean isConfigured = false;

    public static void d(String tag, String message) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.debug(message);
        }
    }

    public static void d(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.debug(message, exception);
        }
    }

    public static void i(String tag, String message) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.info(message);
        }
    }

    public static void i(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.info(message, exception);
        }
    }

    public static void w(String tag, String message) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.warn(message);
        }
    }

    public static void w(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.warn(message, exception);
        }
    }

    public static void e(String tag, String message) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.error(message);
        }
    }

    public static void e(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger logger = getLogger(tag);
            logger.error(message, exception);
        }
    }

    private static Logger getLogger(String tag) {
        if (!isConfigured) {
            ConfigureLog4j.configure();
            isConfigured = true;
        }
        Logger logger;
        if (null == tag || null != tag && tag.isEmpty()) {
            logger = Logger.getRootLogger();
        } else {
            logger = Logger.getLogger(tag);
        }
        return logger;
    }
}
