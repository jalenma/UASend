package com.haier.uhome.usend.log;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;


import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static CrashHandler             crashHandler;
    private Application application;
    private UncaughtExceptionHandler defaultExceptionHandler;

    private CrashHandler() {

    }

    public synchronized static CrashHandler getInstance() {
        if (null == crashHandler) {
            crashHandler = new CrashHandler();
        }
        return crashHandler;
    }

    public void init(Application app) {
        application = app;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 销毁所有activity
        if (Log.SWITCH_LOG) {
            handleException(thread, ex);
        }

        if (null != defaultExceptionHandler) {
            defaultExceptionHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 处理异常
     * 
     * @param thread
     * @param ex
     * @return true if handled, false otherwise
     */
    private boolean handleException(Thread thread, Throwable ex) {
        if (null == ex) {
            return false;
        }
        try {
            collectDeviceInfo();
            Log.w(TAG, "uncaughtException, THREAD: " + thread + " Name: " + thread.getName()
                            + " ID: " + thread.getId());
            StackTraceElement[] steArray = ex.getStackTrace();
            Log.w(TAG, "STACKTRACE Begin");
            Log.w(TAG, "exception :" + ex);
            for (StackTraceElement ste : steArray) {
                Log.e(TAG,
                                "at " + ste.getClassName() + "." + ste.getMethodName() + "("
                                                + ste.getFileName() + ":" + ste.getLineNumber()
                                                + ")");
            }
            Log.w(TAG, "STACKTRACE End");
            if (ex.getCause() != null) {
                Log.w(TAG, "CAUSE BY Begin : ex.getCause() = " + ex.getCause());
                StackTraceElement[] causeByArray = ex.getCause().getStackTrace();
                if (causeByArray != null) {
                    for (StackTraceElement causeby : causeByArray) {
                        Log.e(TAG, "at " + causeby);
                    }
                }
                Log.w(TAG, "CAUSE BY End");
            }
        } catch (Exception e) {
            Log.e(TAG, "While processing crash, it cause a new exception");
        }
        return false;
    }

    /**
     * 为便于差错，此处在Log中添加相应的程序版本号等内容
     */
    private void collectDeviceInfo() {
        try {
            PackageManager pm = application.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(),
                            PackageManager.GET_ACTIVITIES);
            if (null != pi) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                Log.w(TAG, "versionName: " + versionName);
                Log.w(TAG, "versionCode: " + versionCode);
            }
            /*ApplicationInfo ai = pm.getApplicationInfo(application.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                String appVersion = ai.metaData.getString(HTConstants.KEY_APP_VERSION);
                Log.w(TAG, "appVersion: " + appVersion);
            }*/
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        // 返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                // setAccessible(boolean flag)
                // 将此对象的 accessible 标志设置为指示的布尔值。
                // 通过设置Accessible属性为true,才能对私有变量进行访问，不然会得到一个IllegalAccessException的异常
                field.setAccessible(true);
                Log.w(TAG, "DeviceInfo[" + field.getName() + "] : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash Device info", e);
            }
        }

    }
}
