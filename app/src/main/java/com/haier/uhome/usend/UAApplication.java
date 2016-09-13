package com.haier.uhome.usend;

import android.app.Application;

import com.haier.uhome.usend.log.CrashHandler;

/**
 * @Author: majunling
 * @Data: 2016/6/23
 * @Description:
 */
public class UAApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
