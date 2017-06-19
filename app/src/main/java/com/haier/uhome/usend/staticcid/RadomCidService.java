package com.haier.uhome.usend.staticcid;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.haier.uhome.usend.DBHelper;
import com.haier.uhome.usend.UARequest;
import com.haier.uhome.usend.data.SendData;
import com.haier.uhome.usend.data.UserData;
import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.setting.ResultCallback;
import com.haier.uhome.usend.setting.SettingClient;
import com.haier.uhome.usend.setting.SettingInfo;
import com.haier.uhome.usend.staticfile.SendDataGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ${todo}(这里用一句话描述这个类的作用)
 *
 * @author majunling
 * @date 2017/6/9
 */

public class RadomCidService extends Service {
    private static final String TAG = "UA-RadomCidService";

    public static final String INTENT_CMD = "cmd";
    public static final int CMD_START = 0X0001;
    public static final int CMD_PAUSE = 0X0002;
    public static final int CMD_GO_ON = 0X0003;
    public static final int CMD_STOP = 0X0004;

    public enum Status {
        IDLE,
        RUNNING,
        PAUSE,
        STOP
    }

    private static Status status;
    private static List<Observer> observerList = new ArrayList<>();

    Handler handler = new Handler();
    private Timer timer;

    private int total;
    private int alreadySendCount;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        setStatus(Status.IDLE);
    }

    @Override
    public void onDestroy() {
        setStatus(Status.STOP);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int flag = intent.getIntExtra(INTENT_CMD, 0);

        if (flag == CMD_START) {
            start();
        } else if (flag == CMD_PAUSE) {
            pause();
        } else if (flag == CMD_GO_ON) {
            goOn();
        } else if (flag == CMD_STOP) {
            stop();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //获取开关上报开关
    private void loadSendSwitch() {
        //获取配置数据
        SettingClient.getInstance().getSettings(this, new ResultCallback<SettingInfo>() {
            @Override
            public void onSuccess(SettingInfo result) {
                if (!result.isUaSwitch() || !result.isOtherUaSwitch()) {
//                    notifySendError("开关：" + result.isUaSwitch() + ", 开关other:" + result.isOtherUaSwitch(), 0, 0);
                    stop();
                    return;
                }

                startSend();
            }

            @Override
            public void onFailure(SettingInfo result) {
//                notifySendError("获取开关失败", 0, 0);
                stop();
            }
        });
    }

    private void resetRequestData() {
        RequestClient.setAlreadySendCount(this, 0);
    }

    private TimerTask creatTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (status.equals(Status.RUNNING)) {
                    sendData();
                }
            }
        };
    }

    private void startSend() {
        total = RequestClient.getSendCount(this);
        alreadySendCount = RequestClient.getAlreadySendCount(this);

        timer = new Timer();
        timer.schedule(creatTimerTask(), 100, RequestClient.getSendInteval(this) * 1000);
    }

    private void start() {
        resetRequestData();
        setStatus(Status.RUNNING);
        loadSendSwitch();
    }

    private void pause() {
        if (timer != null) {
            timer.cancel();
        }
        setStatus(Status.PAUSE);
    }

    private void goOn() {
        setStatus(Status.RUNNING);
        startSend();
    }

    private void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        setStatus(Status.STOP);
        stopSelf();
    }

    private void sendData() {
        if (alreadySendCount >= total) {
            //TODO report
            stop();
            return;
        }

        UserData userDate = new UserData("", SendDataGenerator.generateRadomCid());
        final SendData sendData = SendDataGenerator.generate(userDate);

        handler.post(new Runnable() {
            @Override
            public void run() {
                UARequest.getInstance()
                        .sendAppStartUse(RadomCidService.this, sendData, new UARequest.RequestResult() {
                            @Override
                            public void onSuccess(int code, String response) {
                                alreadySendCount++;
                                Log.i(TAG, "sucess result >> " + getSendLog());
                                RequestClient.setAlreadySendCount(RadomCidService.this, alreadySendCount);
                                SendCidData data = new SendCidData();
                                data.setCid(sendData.getClientId());
                                SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String time = dataFormat.format(new Date(sendData.getAppStartEventTime()));
                                data.setTime(time);
                                DBHelper.getInstance().getDBSessioin(RadomCidService.this)
                                        .getSendCidDataDao().insert(data);
                                Log.i(TAG, "insert data = " + data);
                            }

                            @Override
                            public void onFailure(int code, String response) {

                            }
                        });
            }
        });
    }

    private String getSendLog() {
        return "总条数：" + total + ", 已发送：" + alreadySendCount;
    }

    public static Status getStatus() {
        return status;
    }

    private void setStatus(Status newStatus) {
        status = newStatus;
        for (Observer observer : observerList) {
            observer.onStatusChanged(newStatus);
        }
    }

    public static void addObserver(Observer observer) {
        observerList.add(observer);
    }

    public static void removeObserver(Observer observer) {
        observerList.remove(observer);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static abstract class Observer {
        public void onStatusChanged(Status status) {
        }
    }
}
