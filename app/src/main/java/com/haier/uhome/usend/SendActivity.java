package com.haier.uhome.usend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.haier.uhome.usend.utils.PreferencesConstants;
import com.haier.uhome.usend.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendActivity extends Activity {

    private static final String TAG = "SendActivity";

    public static final String ACTION_CANCLE = "com.haier.usend.CANCLE_DONE";
    public static final String ACTION_DONE = "com.haier.usend.SEND_DONE";
    public static final String ACTION_PROGRESS = "com.haier.usend.SEND_PROGERESS";
    public static final String EXTRA_SUCCES_COUNT = "sucess_count";
    public static final String EXTRA_FIAL_COUNT = "fial_count";

    @BindView(R.id.tv_current_result)
    TextView tvResult;
    @BindView(R.id.tv_send_total)
    TextView tvSendTotal;
    @BindView(R.id.et_run_count)
    EditText etRunCount;
    @BindView(R.id.et_run_time)
    EditText etRunTime;
    @BindView(R.id.btn_send)
    Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DONE);
        filter.addAction(ACTION_PROGRESS);
        registerReceiver(receiver, filter);

        if(UAService.isRunning()){
            setSendButtnEnable(false);
        }
        refreshTotalSendText();
        refreshCurrentSendCount(0, 0);

        etRunCount.setText(String.valueOf(PreferencesUtils.getInt(this, PreferencesConstants.RUN_COUNT, 0)));
        etRunTime.setText(String.valueOf(PreferencesUtils.getInt(this, PreferencesConstants.RUN_TIME, 0)));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @OnClick(R.id.btn_send)
    void startSend(){
        String runTime = etRunTime.getText().toString().trim();
        if(!TextUtils.isEmpty(runTime)){
            PreferencesUtils.putInt(this, PreferencesConstants.RUN_TIME, Integer.valueOf(runTime));
        }
        String runCount = etRunCount.getText().toString().trim();
        if(!TextUtils.isEmpty(runCount)){
            PreferencesUtils.putInt(this, PreferencesConstants.RUN_COUNT, Integer.valueOf(runCount));
        }

        startUaService();
    }

    private void startUaService() {
        showConfirmDialog();
//        Intent it = new Intent();
//        it.setClass(this, UAService.class);
//        startService(it);
//        setSendButtnEnable(false);
    }

    private void setSendButtnEnable(boolean enable) {
        btnSend.setEnabled(enable);
    }

    private void refreshTotalSendText(){
        String total = String.format("今天已经发送成功：%d; 失败：%d",
            UAStatisticClient.getTodaySuccCount(this),
            UAStatisticClient.getTodayFailCount(this));
        tvSendTotal.setText(total);
    }

    private void refreshCurrentSendCount(int succCount, int failCount){
        tvResult.setText("本次发送成功条数：" + succCount + "; 失败条数：" + failCount);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (ACTION_DONE.equals(intent.getAction())) {
                    setSendButtnEnable(true);
                    int succCount = intent.getIntExtra(EXTRA_SUCCES_COUNT, 0);
                    int failCount = intent.getIntExtra(EXTRA_FIAL_COUNT, 0);
                    refreshCurrentSendCount(succCount, failCount);
                    refreshTotalSendText();
                } else if (ACTION_PROGRESS.equals(intent.getAction())) {
                    int succCount = intent.getIntExtra(EXTRA_SUCCES_COUNT, 0);
                    int failCount = intent.getIntExtra(EXTRA_FIAL_COUNT, 0);
                    refreshCurrentSendCount(succCount, failCount);
                    refreshTotalSendText();
                } else if(ACTION_CANCLE.equals(intent.getAction())){
                    setSendButtnEnable(true);
                }
            }
        }
    };

    private AlertDialog dialog;

    private void showConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("每条执行时间 1 秒？")
            .setCancelable(false)
            .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

        dialog = builder.create();
        dialog.show();
    }

    private void dismissConfirmDialog(){
        if(dialog != null){
            dialog.dismiss();
        }
    }
}
