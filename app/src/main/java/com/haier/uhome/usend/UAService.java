package com.haier.uhome.usend;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.setting.ResultCallback;
import com.haier.uhome.usend.setting.SettingClient;
import com.haier.uhome.usend.setting.SettingInfo;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: majunling
 * @Data: 2016/6/13
 * @Description:
 */
public class UAService extends Service {
    private static final String TAG = "UA-UAService";

    //最大等待60分钟
    private int maxWaitTime = 60 * 60;

    private int waitTime = 0;

    private Random random = new Random(System.currentTimeMillis());

    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);

    private static final int MSG_SEND = 0X0001;

    //请求时间间隔
    private static final float TIME_SEP = 1.5f;

    private Timer timer;


    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            final int succCount = UARequest.getInstance().getSuccessAppStartCount();
            final int failCount = UARequest.getInstance().getFailAppStartCount();
            int sendCount = SettingClient.getInstance().getSettingInfo().getSendCount();

            if (succCount + failCount >= sendCount) {
                timer.cancel();
                return;
            }
            final String userId = String.valueOf(Math.abs(random.nextLong()));
            Message msg = handler.obtainMessage(MSG_SEND);
            //msg.arg1 = delay;
            msg.obj = userId;
            handler.sendMessage(msg);
            //UARequest.getInstance().sendRequset(UAService.this, userId);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SEND:
                    String userId = (String) msg.obj;
                    UARequest.getInstance().sendRequset(UAService.this, userId);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new Timer();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //获取配置数据
        SettingClient.getInstance().getSettings(this, new ResultCallback<SettingInfo>() {
            @Override
            public void onSuccess(SettingInfo result) {
                //sendRequest();
                sendRequestTask();
            }

            @Override
            public void onFailure(SettingInfo result) {
                showToast("获取开关失败");
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendRequestTask(){
        boolean uaSwitch = SettingClient.getInstance().getSettingInfo().isUaSwitch();
        boolean otherUaSwitch = SettingClient.getInstance().getSettingInfo().isOtherUaSwitch();
        int sendCount = SettingClient.getInstance().getSettingInfo().getSendCount();

        if (!uaSwitch || !otherUaSwitch || sendCount < 1) {
            showToast("开关：" + uaSwitch + ", 条数：" + sendCount
                + ", 开关other:" + otherUaSwitch);
            return;
        }

        Log.i(TAG, "UA-start");

        //执行次数
        final int len = sendCount;
        //线程等待时间，弹出执行结果
        //maxWaitTime = 2 * 60;

        UARequest.getInstance().reset();
        timer.schedule(timerTask, (long) (TIME_SEP * 1000) , (long) (TIME_SEP * 1000));

        waitTime = 0;
        new Thread("checkfinish") {
            @Override
            public void run() {
                super.run();
                try {
                    while (true) {
                        int step = 5;
                        waitTime += step;
                        sleep(step * 1000);
                        final int succCount = UARequest.getInstance().getSuccessAppStartCount();
                        final int failCount = UARequest.getInstance().getFailAppStartCount();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendProgress(succCount, failCount);
                            }
                        });

                        if (succCount + failCount >= len || waitTime > maxWaitTime) {
                            timer.cancel();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    sendFinished(succCount, failCount);
                                }
                            });
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }

    private void sendRequest() {

        boolean uaSwitch = SettingClient.getInstance().getSettingInfo().isUaSwitch();
        boolean otherUaSwitch = SettingClient.getInstance().getSettingInfo().isOtherUaSwitch();
        int sendCount = SettingClient.getInstance().getSettingInfo().getSendCount();

        if (!uaSwitch || !otherUaSwitch || sendCount < 1) {
            showToast("开关：" + uaSwitch + ", 条数：" + sendCount
                + ", 开关other:" + otherUaSwitch);
            return;
        }

        Log.i(TAG, "UA-start");

//        String users = FileUtil.getAllUserId();
//
//        String[] userArr = users.split(",");
//
//        int len = userArr.length;

        //执行次数
        final int len = sendCount;
        //线程等待时间，弹出执行结果
        maxWaitTime = 2 * 60;

        UARequest.getInstance().reset();
        for (int i = 0; i < len; i++) {
            final String userId = String.valueOf(Math.abs(random.nextLong()));// userArr[i];
//            fixedThreadPool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    UARequest.getInstance().sendRequset(UAService.this, userId);
//                }
//            });

            int delay = i / 100;
            delay = Math.min(10, delay);
            Message msg = handler.obtainMessage(MSG_SEND);
            //msg.arg1 = delay;
            msg.obj = userId;

            handler.sendMessageDelayed(msg, delay * 1000);
            Log.i(TAG, "delay = " + delay);
            //UARequest.getInstance().sendRequset(UAService.this, userId);
        }

        waitTime = 0;
        new Thread("checkfinish") {
            @Override
            public void run() {
                super.run();
                try {
                    while (true) {
                        int step = 1;
                        waitTime += step;
                        sleep(step * 1000);
                        final int succCount = UARequest.getInstance().getSuccessAppStartCount();
                        final int failCount = UARequest.getInstance().getFailAppStartCount();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sendProgress(succCount, failCount);
                            }
                        });

                        if (succCount + failCount >= len || waitTime > maxWaitTime) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    sendFinished(succCount, failCount);
                                }
                            });
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
//        fixedThreadPool.execute(new Thread("checkfinish") {
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    while (true) {
//                        int step = 1;
//                        waitTime += step;
//                        sleep(step * 1000);
//                        final int succCount = UARequest.getInstance().getSuccessAppStartCount();
//                        final int failCount = UARequest.getInstance().getFailAppStartCount();
//
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                sendProgress(succCount, failCount);
//                            }
//                        });
//
//                        if (succCount + failCount >= len || waitTime > maxWaitTime) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    sendFinished(succCount, failCount);
//                                }
//                            });
//                            break;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });

//        new Thread("xx") {
//            @Override
//            public void run() {
//                super.run();
//                UARequest.getInstance().sendRequset(UAService.this, "263550");
//
//                String result = UARequest.getInstance().getSendResult();
//                Log.i(TAG, "send result=" + result);
//            }
//        }.start();
    }

    private void sendFinished(int succCount, int failCount) {

        Log.i(TAG, "UA-end, 成功条数：" + succCount + ", failCount=" + failCount);

        Intent intent = new Intent(SendActivity.ACTION_DONE);
        sendBroadcast(intent);

        AlertDialog.Builder builder = new AlertDialog.Builder(UAService.this);
        builder.setTitle("完成");
        builder.setMessage("成功条数：" + succCount + "\n失败条数：" + failCount);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        dialog.show();
    }

    private void sendProgress(int succesCount, int failCount) {
        Intent intent = new Intent(SendActivity.ACTION_PROGRESS);
        intent.putExtra(SendActivity.KEY_SUCCES_COUNT, succesCount);
        intent.putExtra(SendActivity.KEY_FIAL_COUNT, failCount);
        sendBroadcast(intent);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
