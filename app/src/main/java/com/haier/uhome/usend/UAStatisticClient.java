package com.haier.uhome.usend;

import android.content.Context;

import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.utils.PreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: majunling
 * @Data: 2016/9/18
 * @Description: 统计发送条数
 */
public class UAStatisticClient {

    private static final String TAG = "UA-UAStatisticClient";
    private static UAStatisticClient sInstance;

    private UAStatisticClient() {

    }

    /**
     * 保存本次运行发送条数
     *
     * @param context
     * @param succCount
     * @param failCount
     */
    public static void saveCurrentSendCount(Context context, int succCount, int failCount) {

        Log.i(TAG, "saveCurrentSendCount getTodaySuccCount:" + getTodaySuccCount(context)
            + ", getTodayFailCount:" + getTodayFailCount(context) + ", succCount=" + succCount + ", failCount=" +
            failCount);

        if (succCount > 0) {
            int todaySuccCount = getTodaySuccCount(context) + succCount;
            PreferencesUtils.putInt(context, getTodaySuccSaveKey(), todaySuccCount);
        }
        if (failCount > 0) {
            int todayFailCount = getTodayFailCount(context) + failCount;
            PreferencesUtils.putInt(context, getTodayFailSaveKey(), todayFailCount);
        }
    }

    /**
     * 获取当天发送条数
     *
     * @return
     */
    public static int getTodaySuccCount(Context context) {
        Log.i(TAG, "getTodaySuccCount = " + PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0));
        return PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
    }

    public static int getTodayFailCount(Context context) {
        Log.i(TAG, "getTodayFailCount = " + PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0));
        return PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0);
    }

    private static String getTodaySuccSaveKey() {
        return getTodayDate() + "_succ";
    }

    private static String getTodayFailSaveKey() {
        return getTodayDate() + "_fail";
    }

    /**
     * 获取当天的日期
     *
     * @return
     */
    private static String getTodayDate() {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Log.i(TAG, "getTodayDate " + dateFormat.format(date).toString());
        return dateFormat.format(date).toString();
    }

    /**
     * 计算频率，返回单位秒
     * @param count 次数
     * @param second  时间，秒
     * @return
     */
    public static float calculateFrequency(int count, int second) throws Exception {
        if(second < 1 || count < 1){
            throw new Exception("次数count和时间second必须大于0");
        }
        return (float)second / (float) count;
    }
}
