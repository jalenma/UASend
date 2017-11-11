package com.haier.uhome.usend.data;

import android.content.Context;

import com.haier.uhome.usend.utils.PreferencesUtils;

/**
 * @author majunling
 * @date 2017/7/19
 */

public class AppInfo {
    private static final String APP_VER = "3.1.1-2017052502";
    private static final String KEY_APP_VER = "app_vertion";

    private Context context;

    private static AppInfo sInstance;

    private void AppInfo() {

    }

    public static AppInfo getInstance() {
        if (sInstance == null) {
            synchronized (AppInfo.class) {
                if (sInstance == null) {
                    sInstance = new AppInfo();
                }
            }
        }

        return sInstance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public String getAppVer() {
        return PreferencesUtils.getString(context, KEY_APP_VER, APP_VER);
    }

    public void setAppVer(String appVer) {
        PreferencesUtils.putString(context, KEY_APP_VER, appVer);
    }
}
