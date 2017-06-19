package com.haier.uhome.usend;

import android.app.Application;

import com.haier.uhome.usend.data.Channels;
import com.haier.uhome.usend.staticfile.UAStatisticClient;
import com.haier.uhome.usend.log.CrashHandler;
import com.haier.uhome.usend.data.PhoneModel;

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
        PhoneModel.init(this);
        Channels.loadChannels(this);
        UAStatisticClient.getInstance().init(this);
    }
}
