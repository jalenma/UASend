package com.haier.uhome.usend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.haier.uhome.usend.events.EventSendStatus;
import com.haier.uhome.usend.utils.PreferencesConstants;
import com.haier.uhome.usend.utils.PreferencesUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendActivity extends Activity {

    private static final String TAG = "SendActivity";

    @BindView(R.id.tv_current_result)
    TextView tvResult;
    @BindView(R.id.tv_today_total)
    TextView tvTodayTotal;
    @BindView(R.id.tv_send_total)
    TextView tvSendTotal;
    @BindView(R.id.et_run_count)
    EditText etRunCount;
    @BindView(R.id.et_run_time)
    EditText etRunTime;
    @BindView(R.id.et_send_inteval)
    EditText etSendInteval;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.btn_pause)
    Button btnPause;
    @BindView(R.id.btn_continue)
    Button btnContinue;

    boolean isRestart = true;

    int runTime;
    int runCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);

        if(UAService.isRunning()){
            setSendButtnEnable(false);
        }
        refreshTodaySendText();
        refershTotalSendText();
        refreshCurrentSendCount(0, 0);

        etRunCount.setText(String.valueOf(PreferencesUtils.getInt(this, PreferencesConstants.RUN_COUNT, 0)));
        etRunTime.setText(String.valueOf(PreferencesUtils.getInt(this, PreferencesConstants.RUN_TIME, 0)));
        etSendInteval.setText(String.valueOf(PreferencesUtils.getInt(this, PreferencesConstants.SEND_INTEVAL, 0)));

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void startSend(){
        int inteval = 0;

        String sendInteval = etSendInteval.getText().toString().trim();
        if(!TextUtils.isEmpty(sendInteval)){
            inteval = Integer.valueOf(sendInteval);
        }

        if(inteval <= 0){
            showToast("间隔必须大于0");
            return;
        }

        PreferencesUtils.putInt(this, PreferencesConstants.SEND_INTEVAL, inteval);
        //PreferencesUtils.putInt(this, PreferencesConstants.SEND_STATISTIC_INDEX, 0);
        try {
            String msg = "发送时间间隔：" + inteval + "秒，是否继续？";
            showConfirmDialog(msg);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.getMessage());
            return;
        }

        /*String timeStr = etRunTime.getText().toString().trim();
        if(!TextUtils.isEmpty(timeStr)){
            runTime = Integer.valueOf(timeStr);
        }
        String countStr = etRunCount.getText().toString().trim();
        if(!TextUtils.isEmpty(countStr)){
            runCount = Integer.valueOf(countStr);
        }

        if(runTime < 1 || runCount < 1){
            showToast("次数和时间必须大于0");
            return;
        }

        try {
            float frequence = UAStatisticClient.calculateFrequency(runCount, runTime * 60);
            String msg = "发送时间间隔：" + frequence + "秒，是否继续？";
            showConfirmDialog(msg);
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.getMessage());
            return;
        }*/
    }

    @OnClick(R.id.btn_send)
    void restartSend(){
        UAStatisticClient.getInstance().resetStatisticData();
        isRestart = true;
        startSend();
    }

    @OnClick(R.id.btn_pause)
    void pauseSend(){
        stopUaService();
    }

    @OnClick(R.id.btn_continue)
    void continueSend(){
        isRestart = false;
        startSend();
    }

    private void startUaService() {
        if(isRestart){
            UAStatisticClient.getInstance().resetStatisticData();
        }
        PreferencesUtils.putInt(this, PreferencesConstants.RUN_TIME, runTime);
        PreferencesUtils.putInt(this, PreferencesConstants.RUN_COUNT, runCount);
        Intent it = new Intent();
        it.setClass(this, UAService.class);
        startService(it);
        setSendButtnEnable(false);
    }

    private void stopUaService() {
        Intent it = new Intent();
        it.setClass(this, UAService.class);
        stopService(it);
    }

    private void setSendButtnEnable(boolean enable) {
        btnSend.setEnabled(enable);
    }

    private void refreshTodaySendText(){
        String total = String.format("今天已经发送成功：%d; 失败：%d",
            UAStatisticClient.getInstance().getTodaySuccCount(this),
            UAStatisticClient.getInstance().getTodayFailCount(this));
        tvTodayTotal.setText(total);
    }
    private void refreshCurrentSendCount(int succCount, int failCount){
        tvResult.setText("本次发送成功条数：" + succCount + "; 失败条数：" + failCount);
    }

    private void refershTotalSendText(){

        String total = String.format("本文件已经发送成功：%d; 失败：%d",
            UAStatisticClient.getInstance().getSendSuccCount(),
            UAStatisticClient.getInstance().getSendFailCount());
        tvSendTotal.setText(total);
    }

    private void showToast(String msg){
        Toast.makeText(this,"次数和时间必须大于0",Toast.LENGTH_LONG).show();
    }


    @Subscribe
    public void onEvent(EventSendStatus sendStatus){
        if(sendStatus == null){
            return;
        }
        int succCount = sendStatus.getSendCount().getSucessCount();
        int failCount = sendStatus.getSendCount().getFailCount();
        //Log.i("jalen", "onEvent, type="+sendStatus.getSendStatus() + ", f=" + failCount +", s="+succCount);
        switch (sendStatus.getSendStatus()){
            case SEND_DONE:
                setSendButtnEnable(true);
                break;
            case SEND_PROGRESS:
                refreshCurrentSendCount(succCount, failCount);
                refreshTodaySendText();
                refershTotalSendText();
                break;
            case SEND_CANCLE:
                setSendButtnEnable(true);
                break;
        }
    }

    private AlertDialog dialog;

    private void showConfirmDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startUaService();
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
