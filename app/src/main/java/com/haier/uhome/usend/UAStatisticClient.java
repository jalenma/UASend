package com.haier.uhome.usend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.anye.greendao.gen.DaoMaster;
import com.anye.greendao.gen.DaoSession;
import com.haier.uhome.usend.data.SendData;
import com.haier.uhome.usend.data.UserData;
import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.utils.PreferencesConstants;
import com.haier.uhome.usend.utils.PreferencesUtils;

import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: majunling
 * @Data: 2016/9/18
 * @Description: 统计发送条数
 */
public class UAStatisticClient {

    private static final String TAG = "UA-UAStatisticClient";
    public static final String USER_FILE_PATH = "/users.txt";
    public static final String CID_FILE_PATH = "/cid.txt";

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

    private List<UserData> statisticUserList;

    private String todayStr;

    private static final String APP_ID = SendData.APP_ID;
    private static final int INSERT_SIZE = 1000;

    DaoSession mDaoSession;

    private UAStatisticClient() {
    }

    public void init(Context context) {
        if (this.context != null) {
            throw new RuntimeException("already init");
        }
        this.context = context;
        statisticUserList = new ArrayList<>();
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

    public interface LoadDataCallback {
        void onDone();
    }

    public void loadStatisticData(final LoadDataCallback callback) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String users = FileUtil.readFile(USER_FILE_PATH);
                if (!TextUtils.isEmpty(users)) {
                    String[] userArr = users.split("\r\n");
                    Log.i(TAG, "User list :" + users);
                    synchronized (statisticUserList) {
                        statisticUserList.clear();
                        if (userArr != null && userArr.length > 0) {
                            for (int i = 0; i < userArr.length; i++) {
                                statisticUserList.add(new UserData(userArr[i]));
                            }
                        }
                    }
                }
                callback.onDone();
            }
        }.start();
    }

    public void loadCidStatisticData(final LoadDataCallback callback) {
        setDatabase();
        new Thread() {
            @Override
            public void run() {
                super.run();

                long lastFileModifyTime = PreferencesUtils.getLong(context, PreferencesConstants.FILE_CID_MODIFY_TIME);

                File file = FileUtil.openFile(CID_FILE_PATH);
                final long modifyTime = file.lastModified();
                final List<UserData> dataList = new ArrayList<UserData>();

                //文件时间发送变化，重新读取文件。
                if (lastFileModifyTime != modifyTime) {
                    fileModifyMsg();

                    FileUtil.readLine(CID_FILE_PATH, new FileUtil.IReadLine() {
                        @Override
                        public void readLine(String line) {
                            Pattern pattern = Pattern.compile("\"([\\w-]+)\"\\s*\"([\\w-]+)\"\\s*\"([\\w-]+)\"\\s*\"" +
                                    "([\\w-]+)\"");
                            Matcher matcher = pattern.matcher(line);
                            //Log.i(TAG, "line =" + line);
                            if (matcher.find()) {
                                if (matcher.groupCount() < 4) {
                                    return;
                                }

                                String appId = matcher.group(1);
                                //非android appid，不处理
                                if (!TextUtils.equals(APP_ID, appId)) {
                                    return;
                                }
                                String cid = matcher.group(2);
                                String uid = matcher.group(3);
                                //String useTime = matcher.group(4);

                                //Log.i(TAG, "appId=" + appId + ", cid = " + cid
                                //        + ", uid = " + uid);
                                //dataList.add(new UserData(uid, cid));

                                dataList.add(new UserData("", cid));
                                if(dataList.size() >= INSERT_SIZE){
                                    Log.i(TAG, "read file size = " + dataList.size() );
                                    mDaoSession.getUserDataDao().insertOrReplaceInTx(dataList);
                                    dataList.clear();
                                }
                            }
                        }

                        @Override
                        public void done() {

                            if(dataList.size() > 0){
                                Log.i(TAG, "read file size = " + dataList.size() );
                                mDaoSession.getUserDataDao().insertOrReplaceInTx(dataList);
                                dataList.clear();
                            }

                            PreferencesUtils.putLong(context, PreferencesConstants.FILE_CID_MODIFY_TIME, modifyTime);
                            statisticUserList.clear();
                            statisticUserList = mDaoSession.getUserDataDao().loadAll();

                            Log.i(TAG, "done c size=" + statisticUserList.size());
                            callback.onDone();
                        }
                    });
                } else {
                    statisticUserList.clear();
                    statisticUserList = mDaoSession.getUserDataDao().loadAll();
                    Log.i(TAG, "done d size=" + statisticUserList.size());
                    callback.onDone();
                }

            }
        }.start();
    }


    private void fileModifyMsg(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                new Toast(context).makeText(context, "发现文件变动，重新计数", Toast.LENGTH_LONG).show();
            }
        });
        resetStatisticData();
        mDaoSession.getUserDataDao().deleteAll();
    }


    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.DevOpenHelper(context, "ua", null);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        DaoMaster mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public int getSendSize() {
        return statisticUserList.size() - sendIndex;
    }

    public int getSendIndex() {
        return sendIndex;
    }

    public int getTotalStatisticBeanSize() {
        return statisticUserList.size();
    }

    public UserData getNextStatisticUser() {
        UserData bean = null;
        if (sendIndex < statisticUserList.size()) {
            bean = statisticUserList.get(sendIndex);
            sendIndex++;
        }
        return bean;
    }

    public boolean isLastData() {
        Log.i(TAG, "isLastData sendIndex=" + sendIndex + ", statisticUserList.size()=" + statisticUserList.size());
        return sendIndex >= statisticUserList.size();
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

        Log.i(TAG, "本文件以发送，成功：" + sendSuccCount + ", 失败：" + sendFailCount
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
