package com.haier.uhome.usend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SendActivity extends Activity {

    private static final String TAG = "SendActivity";

    public static final String ACTION_CANCLE = "com.haier.usend.CANCLE_DONE";
    public static final String ACTION_DONE = "com.haier.usend.SEND_DONE";
    public static final String ACTION_PROGRESS = "com.haier.usend.SEND_PROGERESS";
    public static final String KEY_SUCCES_COUNT = "sucess_count";
    public static final String KEY_FIAL_COUNT = "fial_count";

    private TextView txtView;

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

        txtView = (TextView) findViewById(R.id.txt);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DONE);
        filter.addAction(ACTION_PROGRESS);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void startUaService() {

        txtView.setText("");

        Intent it = new Intent();
        it.setClass(this, UAService.class);
        startService(it);
        setSendButtnEnable(false);
    }

    private void setSendButtnEnable(boolean enable) {
        findViewById(R.id.btn).setEnabled(enable);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ACTION_DONE.equals(intent.getAction())) {
                    setSendButtnEnable(true);
                } else if (ACTION_PROGRESS.equals(intent.getAction())) {
                    int succCount = intent.getIntExtra(KEY_SUCCES_COUNT, 0);
                    int failCount = intent.getIntExtra(KEY_FIAL_COUNT, 0);

                    txtView.setText("成功条数：" + succCount + "；失败条数：" + failCount);
                } else if(ACTION_CANCLE.equals(intent.getAction())){
                    setSendButtnEnable(true);
                }
            }
        }
    };
}
