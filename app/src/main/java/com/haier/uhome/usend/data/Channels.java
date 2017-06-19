package com.haier.uhome.usend.data;

import android.content.Context;
import android.text.TextUtils;

import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.utils.PreferencesUtils;

import java.util.Random;

/**
 * 渠道
 *
 * @author majunling
 * @date 2017/6/17
 */
public class Channels {
    private static final String KEY_CHANNELS = "key_channels";
    private static final String DEFAULT_CHANNELS = "yunying,baidu,91,anzhuo,360,yingyongbao,xiaomi,pp";
    private static final String SEP = ",";

    private static String[] channelArr;

    private Channels() {

    }

    public static void loadChannels(Context context) {
        String saveChannels = PreferencesUtils.getString(context, KEY_CHANNELS);
        if (TextUtils.isEmpty(saveChannels)) {
            saveChannels = DEFAULT_CHANNELS;
        }
        channelArr = saveChannels.split(SEP);
    }

    public static void saveChannels(Context context, String[] inChannelArr) {

        if (inChannelArr == null || inChannelArr.length < 1) {
            return;
        }

        StringBuffer retBuf = new StringBuffer(inChannelArr[0]);
        for (int i = 1; i < inChannelArr.length; i++) {
            retBuf.append(SEP).append(inChannelArr[i]);
        }
        PreferencesUtils.putString(context, KEY_CHANNELS, retBuf.toString());
        channelArr = inChannelArr;
    }

    public static String getChannelsString(Context context) {
        String saveChannels = PreferencesUtils.getString(context, KEY_CHANNELS);
        if (TextUtils.isEmpty(saveChannels)) {
            saveChannels = DEFAULT_CHANNELS;
        }
        return saveChannels;
    }

    public static String getRadomChannel() {
        Random random = new Random();

        if (channelArr.length == 1) {
            return channelArr[0];
        }
        int index = random.nextInt(channelArr.length);
        Log.i("jalen", channelArr[index]);
        return channelArr[index];
    }
}
