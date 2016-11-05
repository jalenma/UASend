package com.haier.uhome.usend;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.haier.uhome.usend.events.EventSendStatus;
import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.setting.ResultCallback;
import com.haier.uhome.usend.setting.SettingClient;
import com.haier.uhome.usend.setting.SettingInfo;
import com.haier.uhome.usend.utils.PreferencesConstants;
import com.haier.uhome.usend.utils.PreferencesUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author: majunling
 * @Data: 2016/6/13
 * @Description:
 */
public class UAService extends Service {
    private static final String TAG = "UA-UAService";

    private static boolean isRunning = false;

    //最大等待120分钟
    private int maxWaitTime = 2 * 60 * 60;

    private int waitTime = 0;

    private Random random = new Random(System.currentTimeMillis());

    //进入发送日志循环
    private static final int MSG_SEND_LOOP = 0X0001;
    //获取控制开关结束
    private static final int MSG_FETCH_SWITCH_DONE = 0X0002;
    //获取user列表结束
    private static final int MSG_LOAD_USER_DONE = 0X0003;


    //默认请求时间间隔
    private static final float DEFAULT_TIME_INTEVAL = 2f;
    //请求时间间隔
    private float sendTimeInterval = DEFAULT_TIME_INTEVAL;
    //请求次数
    private int sendCount = 0;

    private Timer timer;

    //启动成功次数
    private int successAppStartCount = 0;
    //启动失败次数
    private int failAppStartCount = 0;
    //上次上报启动成功次数
    private int lastSuccessAppStartCount = 0;
    //上次上报启动失败次数
    private int lastFailAppStartCount = 0;

    private int successUserStartCount = 0;
    private int failUserStartCount = 0;

    private int alreadySendCount = 0;

    private List<String> userIdList = new ArrayList<String>();
    //user列表的index
    int processId = 0;

    private TimerTask sendTimerTask = new TimerTask() {
        @Override
        public void run() {

            if (successAppStartCount + failAppStartCount >= sendCount || alreadySendCount >= sendCount
                || processId >= userIdList.size()) {
                timer.cancel();
                return;
            }
            alreadySendCount++;
            //final String userId = String.valueOf(Math.abs(random.nextLong()));
            final String userId = userIdList.get(processId);
            processId++;
            PreferencesUtils.putInt(UAService.this, PreferencesConstants.SEND_USER_INDEX, processId);
            Message msg = handler.obtainMessage(MSG_SEND_LOOP);
            //msg.arg1 = delay;
            msg.obj = userId;
            handler.sendMessage(msg);
        }
    };

    private UARequest.RequestResult resultCallback = new UARequest.RequestResult() {
        @Override
        public void onSuccess(int code, String response) {
            successAppStartCount++;
            Log.i(TAG, "result >> " + getSendResult());
        }

        @Override
        public void onFailure(int code, String response) {
            failAppStartCount++;
            Log.i(TAG, "result >> " + getSendResult());
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SEND_LOOP:
                    String userId = (String) msg.obj;
                    UARequest.getInstance().sendAppAndUserStartBatch(UAService.this, userId, resultCallback);
                    break;
                case MSG_LOAD_USER_DONE:
                    sendRequestTask();
                    break;
                case MSG_FETCH_SWITCH_DONE:
                    loadUserId();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        timer = new Timer();
        loadSendParam();
        loadSendSwitch();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        UARequest.getInstance().cancelRequst(this);
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        if(timer != null){
            timer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void reset() {
        successAppStartCount = 0;
        failAppStartCount = 0;
        successUserStartCount = 0;
        failUserStartCount = 0;
        lastSuccessAppStartCount = 0;
        lastFailAppStartCount = 0;
        alreadySendCount = 0;
        processId = 0;
    }

    /**
     * 异常情况处理
     * @param errorMsg
     * @param successCount
     * @param failCount
     */
    private void notifySendError(String errorMsg, int successCount, int failCount) {
        EventBus.getDefault().post(new EventSendStatus(EventSendStatus.SendStatus.SEND_CANCLE, successCount,
            failCount));
        showToast(errorMsg);
        stopSelf();
    }

    //获取开关上报开关
    private void loadSendSwitch(){
        //获取配置数据
        SettingClient.getInstance().getSettings(this, new ResultCallback<SettingInfo>() {
            @Override
            public void onSuccess(SettingInfo result) {
                if (!result.isUaSwitch() || !result.isOtherUaSwitch()) {
                    notifySendError("开关：" + result.isUaSwitch() + ", 开关other:" + result.isOtherUaSwitch(), 0, 0);
                    return;
                }
                handler.sendEmptyMessage(MSG_FETCH_SWITCH_DONE);
            }

            @Override
            public void onFailure(SettingInfo result) {
                notifySendError("获取开关失败", 0, 0);
            }
        });
    }

    private void loadSendParam() {
//        sendCount = PreferencesUtils.getInt(this, PreferencesConstants.RUN_COUNT, 0);
//        int sendTime = PreferencesUtils.getInt(this, PreferencesConstants.RUN_TIME, 0) * 60;
//        try {
//            sendTimeInterval = UAStatisticClient.calculateFrequency(sendCount, sendTime);
//        } catch (Exception e) {
//            sendTimeInterval = DEFAULT_TIME_INTEVAL;
//        }

        sendTimeInterval = PreferencesUtils.getInt(this, PreferencesConstants.SEND_INTEVAL, 0);

        Log.i(TAG, "loadSendParam frequence=" + sendTimeInterval);

        //避免太频繁
        if (sendTimeInterval < 0.1f) {
            sendTimeInterval = 0.1f;
        }
    }

    private void loadUserId() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String users = FileUtil.readFile(FileStorageConst.USER_FILE_PATH);
                if (!TextUtils.isEmpty(users)) {
                    String[] userArr = users.split("\r\n");
                    Log.i(TAG,"User list :" + users);
                    userIdList = Arrays.asList(userArr);
                }

                handler.sendEmptyMessage(MSG_LOAD_USER_DONE);
            }
        }.start();
    }

    private void sendRequestTask() {

        if (userIdList == null || userIdList.isEmpty()) {
            notifySendError("获取userid错误", 0, 0);
            return;
        }
        reset();

        processId = PreferencesUtils.getInt(UAService.this, PreferencesConstants.SEND_USER_INDEX, processId);

        Log.i(TAG, "上次发送到第" + processId + "条");
        sendCount = userIdList.size() - processId;


        if (sendCount < 1) {
            notifySendError("发送条数小于1，条数：" + sendCount, 0, 0);
            return;
        }

        isRunning = true;

        Log.i(TAG, "UA-start");

        //执行次数
        final int len = sendCount;

        timer.schedule(sendTimerTask, (long) (sendTimeInterval * 1000), (long) (sendTimeInterval * 1000));

        waitTime = 0;
        new Thread("checkfinish") {
            @Override
            public void run() {
                super.run();
                try {
                    while (true && isRunning) {
                        int step = 5;
                        waitTime += step;
                        sleep(step * 1000);

                        if (successAppStartCount + failAppStartCount >= len || waitTime > maxWaitTime) {
                            timer.cancel();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reportFinished(successAppStartCount, failAppStartCount);
                                }
                            });
                            break;
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reportProgress(successAppStartCount, failAppStartCount);
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    private String getSendResult() {
        return "successUserStartCount = " + successUserStartCount + ", failUserStartCount=" + failUserStartCount
            + ", successAppStartCount=" + successAppStartCount + ", failAppStartCount=" + failAppStartCount;
    }

    private void reportFinished(int succCount, int failCount) {

        int deltaSucc = succCount - lastSuccessAppStartCount;
        int deltaFail = failCount - lastFailAppStartCount;
        lastSuccessAppStartCount = succCount;
        lastFailAppStartCount = failCount;
        UAStatisticClient.saveCurrentSendCount(this, deltaSucc, deltaFail);

        Log.i(TAG, "UA-end, 成功条数：" + succCount + ", failCount=" + failCount);

        EventBus.getDefault().post(new EventSendStatus(EventSendStatus.SendStatus.SEND_DONE, succCount, failCount));

        AlertDialog.Builder builder = new AlertDialog.Builder(UAService.this);
        builder.setTitle("完成");
        builder.setMessage("成功条数：" + succCount + "\n失败条数：" + failCount);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        dialog.show();

        isRunning = false;
        stopSelf();
    }

    private void reportProgress(int succCount, int failCount) {
        int deltaSucc = succCount - lastSuccessAppStartCount;
        int deltaFail = failCount - lastFailAppStartCount;
        lastSuccessAppStartCount = succCount;
        lastFailAppStartCount = failCount;
        UAStatisticClient.saveCurrentSendCount(this, deltaSucc, deltaFail);

        EventBus.getDefault().post(new EventSendStatus(EventSendStatus.SendStatus.SEND_PROGRESS, succCount, failCount));
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
