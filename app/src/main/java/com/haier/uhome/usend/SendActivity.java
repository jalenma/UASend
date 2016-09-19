package com.haier.uhome.usend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SendActivity extends Activity {

    private static final String TAG = "SendActivity";

    public static final String ACTION_CANCLE = "com.haier.usend.CANCLE_DONE";
    public static final String ACTION_DONE = "com.haier.usend.SEND_DONE";
    public static final String ACTION_PROGRESS = "com.haier.usend.SEND_PROGERESS";
    public static final String KEY_SUCCES_COUNT = "sucess_count";
    public static final String KEY_FIAL_COUNT = "fial_count";

    private TextView txtResult;
    private TextView txtSendTotal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUaService();
            }
        });

        txtResult = (TextView) findViewById(R.id.txt_result);
        txtSendTotal = (TextView) findViewById(R.id.txt_send_total);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DONE);
        filter.addAction(ACTION_PROGRESS);
        registerReceiver(receiver, filter);

        if(UAService.isRunning()){
            setSendButtnEnable(false);
        }
        refreshTotalSendText();
        refreshCurrentSendCount(0, 0);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void startUaService() {
        Intent it = new Intent();
        it.setClass(this, UAService.class);
        startService(it);
        setSendButtnEnable(false);
    }

    private void setSendButtnEnable(boolean enable) {
        findViewById(R.id.btn).setEnabled(enable);
    }

    private void refreshTotalSendText(){
        String total = String.format("今天已经发送成功：%d; 失败：%d",
            UAStatisticClient.getTodaySuccCount(this),
            UAStatisticClient.getTodayFailCount(this));
        txtSendTotal.setText(total);
    }

    private void refreshCurrentSendCount(int succCount, int failCount){
        txtResult.setText("本次发送成功条数：" + succCount + "; 失败条数：" + failCount);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ACTION_DONE.equals(intent.getAction())) {
                    setSendButtnEnable(true);
                    int succCount = intent.getIntExtra(KEY_SUCCES_COUNT, 0);
                    int failCount = intent.getIntExtra(KEY_FIAL_COUNT, 0);
                    refreshCurrentSendCount(succCount, failCount);
                    refreshTotalSendText();
                } else if (ACTION_PROGRESS.equals(intent.getAction())) {
                    int succCount = intent.getIntExtra(KEY_SUCCES_COUNT, 0);
                    int failCount = intent.getIntExtra(KEY_FIAL_COUNT, 0);
                    refreshCurrentSendCount(succCount, failCount);
                    refreshTotalSendText();
                } else if(ACTION_CANCLE.equals(intent.getAction())){
                    setSendButtnEnable(true);
                }
            }
        }
    };
}
