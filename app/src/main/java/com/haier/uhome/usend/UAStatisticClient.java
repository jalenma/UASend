package com.haier.uhome.usend;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.haier.uhome.usend.data.StatisticBean;
import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.utils.PreferencesConstants;
import com.haier.uhome.usend.utils.PreferencesUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: majunling
 * @Data: 2016/9/18
 * @Description: 统计发送条数
 */
public class UAStatisticClient {

    private static final String TAG = "UA-UAStatisticClient";
    private static UAStatisticClient sInstance;

    // 待发送总次数，user列表 - 已发送的数据
    private int sendSize;

    //请求已发送条数
    private int sendIndex;

    //发送成功总条数
    private int sendSuccCount;
    //发送失败总条数
    private int sendFailCount;

    //本次发送成功条数
    private int currentSuccCount;
    //本次发送失败条数
    private int currentFailCount;

    //今天发送成功条数
    private int todySuccCount;
    //今天发送失败条数
    private int todayFailCount;

    private Context context;

    private List<StatisticBean> statisticList;

    private String todayStr;

    private UAStatisticClient() {
    }

    public void init(Context context) {
        if (this.context != null) {
            throw new RuntimeException("already init");
        }
        this.context = context;
        statisticList = new ArrayList<>();
        sendSuccCount = PreferencesUtils.getInt(context, PreferencesConstants.SEND_SUCCESS_COUNT, 0);
        sendFailCount = PreferencesUtils.getInt(context, PreferencesConstants.SEND_FAIL_COUNT, 0);
        sendIndex = PreferencesUtils.getInt(context, PreferencesConstants.SEND_STATISTIC_INDEX, 0);

        todayStr = getTodayDate();
        todySuccCount = PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
        todayFailCount = PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0);
    }

    public static UAStatisticClient getInstance() {
        if (sInstance == null) {
            synchronized (UAStatisticClient.class) {
                if (sInstance == null) {
                    sInstance = new UAStatisticClient();
                }
            }
        }
        return sInstance;
    }

    public interface LoadDataCallback{
        void onDone();
    }

    public void loadStatisticData(final LoadDataCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String users = FileUtil.readFile(FileStorageConst.USER_FILE_PATH);
                if (!TextUtils.isEmpty(users)) {
                    String[] userArr = users.split("\r\n");
                    Log.i(TAG, "User list :" + users);
                    synchronized (statisticList){
                        statisticList.clear();
                        if (userArr != null && userArr.length > 0) {
                            for (int i = 0; i < userArr.length; i++) {
                                statisticList.add(new StatisticBean(userArr[i]));
                            }
                        }
                    }
                }
                callback.onDone();
            }
        }.start();
    }

    public int getSendSize() {
        return statisticList.size() - sendIndex;
    }

    public int getSendIndex() {
        return sendIndex;
    }

    public int getTotalStatisticBeanSize(){
        return statisticList.size();
    }

    public StatisticBean getNextStaticBean(){
        StatisticBean bean = null;
        if(sendIndex < statisticList.size()){
            bean = statisticList.get(sendIndex);
            sendIndex++;
        }
        return bean;
    }

    public boolean isLastData(){
        Log.i(TAG, "isLastData sendIndex=" + sendIndex + ", statisticList.size()="+statisticList.size());
        return sendIndex >= statisticList.size();
    }

    public int getSendSuccCount() {
        return sendSuccCount;
    }

    public int getSendFailCount() {
        return sendFailCount;
    }

    public int getCurrentSuccCount() {
        return currentSuccCount;
    }

    public int getCurrentFailCount() {
        return currentFailCount;
    }

    /**
     * 重置统计数据，包括历史数据，清零
     */
    public void resetStatisticData() {
        sendIndex = 0;
        sendSuccCount = 0;
        sendFailCount = 0;
        currentSuccCount = 0;
        currentFailCount = 0;

        PreferencesUtils.putInt(context, PreferencesConstants.SEND_SUCCESS_COUNT, 0);
        PreferencesUtils.putInt(context, PreferencesConstants.SEND_FAIL_COUNT, 0);
        PreferencesUtils.putInt(context, PreferencesConstants.SEND_STATISTIC_INDEX, 0);
        todayStr = getTodayDate();
        todySuccCount = PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
        todayFailCount = PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0);
    }

    /**
     * 重置本次统计数据
     */
    public void resetCurrentStatisticData() {
        currentSuccCount = 0;
        currentFailCount = 0;
        sendIndex = PreferencesUtils.getInt(context, PreferencesConstants.SEND_STATISTIC_INDEX, 0);
        todayStr = getTodayDate();
        todySuccCount = PreferencesUtils.getInt(context, getTodaySuccSaveKey(), 0);
        todayFailCount = PreferencesUtils.getInt(context, getTodayFailSaveKey(), 0);
        Log.i(TAG, "sendIndex = " + sendIndex);
    }

    public void addSuccessCount(int count) {
        if (count < 1) return;
        currentSuccCount += count;
        sendSuccCount += count;
        todySuccCount += count;
        handler.removeMessages(MSG_SAVE_SEND_DATA);
        handler.sendEmptyMessageDelayed(MSG_SAVE_SEND_DATA, 1000);
    }

    public void addFailCount(int count) {
        if (count < 1) return;
        currentFailCount += count;
        sendFailCount += count;
        todayFailCount += count;
        handler.removeMessages(MSG_SAVE_SEND_DATA);
        handler.sendEmptyMessageDelayed(MSG_SAVE_SEND_DATA, 2000);
    }


    private void saveSendData() {
        synchronized (this) {
            PreferencesUtils.putInt(context, PreferencesConstants.SEND_STATISTIC_INDEX, sendIndex);
            PreferencesUtils.putInt(context, PreferencesConstants.SEND_SUCCESS_COUNT, sendSuccCount);
            PreferencesUtils.putInt(context, PreferencesConstants.SEND_FAIL_COUNT, sendFailCount);

            saveTodaySendCount(context, todySuccCount, todayFailCount);
        }

        Log.i(TAG, "本文件以发送，成功："+sendSuccCount + ", 失败：" + sendFailCount
        + ", 今天已发送，成功：" + todySuccCount + ", 失败：" + todayFailCount);
    }

    private final int MSG_SAVE_SEND_DATA = 0X0001;
    HandlerThread thread = new HandlerThread("");
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAVE_SEND_DATA:
                    saveSendData();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 保存本次运行发送条数
     *
     * @param context
     * @param succCount
     * @param failCount
     */
    public void saveTodaySendCount(Context context, int succCount, int failCount) {
        PreferencesUtils.putInt(context, getTodaySuccSaveKey(), succCount);
        PreferencesUtils.putInt(context, getTodayFailSaveKey(), failCount);
    }

    /**
     * 获取当天发送条数
     *
     * @return
     */
    public int getTodaySuccCount(Context context) {
        return todySuccCount;
    }

    public int getTodayFailCount(Context context) {
        return todayFailCount;
    }

    private String getTodaySuccSaveKey() {
        return todayStr + "_succ";
    }

    private String getTodayFailSaveKey() {
        return todayStr + "_fail";
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
}
