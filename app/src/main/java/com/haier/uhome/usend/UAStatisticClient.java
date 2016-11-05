package com.haier.uhome.usend;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

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

    //请求总次数
    private int sendSize;

    //请求已发送条数
    private int sendIndex;

    //发送成功总条数
    private int todaySuccCount;
    //发送失败总条数
    private int todayFailCount;

    //本次发送成功条数
    private int currentSuccCount;
    //本次发送失败条数
    private int currentFailCount;

    private Context context;

    private UAStatisticClient(Context context) {
        this.context = context;

        todaySuccCount = PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
        todayFailCount = PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
    }

    public void init(Context context){
        sInstance = new UAStatisticClient(context);
    }

    public static UAStatisticClient getInstance() {
        return sInstance;
    }

    public void reset(){
        sendIndex = 0;
        todaySuccCount = 0;
        todayFailCount = 0;
        currentSuccCount = 0;
        currentFailCount = 0;
    }

    public void resetCurrentSendData() {
        currentSuccCount = 0;
        currentFailCount = 0;
    }

    public void addSuccess(int count) {
        currentSuccCount += count;
        saveTodaySuccess(count);
    }

    public void addFialCount(int count){
        currentFailCount += count;
        saveTodayFail(count);
    }

    private void saveTodaySuccess(int count){
        if(count < 1){
            return;
        }
        synchronized(this){
            if (count > 0) {
                int total = getTodaySuccCount(context) + count;
                PreferencesUtils.putInt(context, getTodaySuccSaveKey(), total);
            }
        }
    }

    private void saveTodayFail(int count){
        if(count < 1){
            return;
        }
        synchronized(this){
            if (count > 0) {
                int total = getTodayFailCount(context) + count;
                PreferencesUtils.putInt(context, getTodayFailSaveKey(), total);
            }
        }
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
        //Log.i(TAG, "getTodaySuccCount = " + PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0));
        return PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
    }

    public static int getTodayFailCount(Context context) {
        //Log.i(TAG, "getTodayFailCount = " + PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0));
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

        //Log.i(TAG, "getTodayDate " + dateFormat.format(date).toString());
        return dateFormat.format(date).toString();
    }

    /**
     * 计算频率，返回单位秒
     *
     * @param count  次数
     * @param second 时间，秒
     * @return
     */
    public static float calculateFrequency(int count, int second) throws Exception {
        if (second < 1 || count < 1) {
            throw new Exception("次数count和时间second必须大于0");
        }
        return (float) second / (float) count;
    }

    HandlerThread thread = new HandlerThread("");
    Handler handler = new Handler(thread.getLooper());
}
