package com.haier.uhome.usend.staticcid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anye.greendao.gen.DaoSession;
import com.anye.greendao.gen.SendCidDataDao;
import com.haier.uhome.usend.DBHelper;
import com.haier.uhome.usend.FileUtil;
import com.haier.uhome.usend.R;
import com.haier.uhome.usend.log.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RadomCidActivity extends AppCompatActivity {

    private static final String TAG = "RadomCidActivity";
    @BindView(R.id.et_send_inteval)
    EditText etSendIntvel;
    @BindView(R.id.et_send_count)
    EditText etSendCount;
    @BindView(R.id.btn_send)
    Button btnStart;
    @BindView(R.id.btn_pause)
    Button btnPause;
    @BindView(R.id.btn_go_on)
    Button btnGoOn;
    @BindView(R.id.btn_export)
    Button btnExport;
    @BindView(R.id.tv_result)
    TextView tvSendResult;

    ProgressDialog progressDialog;

    private static final String exportDir = "/ua_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radom_cid);

        ButterKnife.bind(this);

        etSendCount.setText(String.valueOf(RequestClient.getSendCount(this)));
        etSendIntvel.setText(String.valueOf(RequestClient.getSendInteval(this)));
        etSendIntvel.addTextChangedListener(new IntevalTextWatcher());

        RequestClient.addObserver(observer);
        RadomCidService.addObserver(serviceObserver);
    }


    @Override
    protected void onDestroy() {
        RequestClient.removeObserver(observer);
        RadomCidService.addObserver(serviceObserver);
        super.onDestroy();
    }

    @OnClick(R.id.btn_send)
    public void send() {
        RequestClient.setSendCount(this, Integer.valueOf(etSendCount.getText().toString().trim()));
        RequestClient.setSendInteval(this, Integer.valueOf(etSendIntvel.getText().toString().trim()));
        startSendService(RadomCidService.CMD_START);
    }

    @OnClick(R.id.btn_pause)
    public void pause() {
        startSendService(RadomCidService.CMD_PAUSE);
    }

    @OnClick(R.id.btn_go_on)
    public void goOn() {
        startSendService(RadomCidService.CMD_GO_ON);
    }

    private void startSendService(int cmd) {
        Intent it = new Intent(this, RadomCidService.class);
        it.putExtra(RadomCidService.INTENT_CMD, cmd);
        startService(it);
    }

    @OnClick(R.id.btn_clean)
    public void cleanData() {

        AlertDialog.Builder build = new AlertDialog.Builder(this)
                .setMessage("此操作会清除所有操作数据，无法找回，请确定已经导出数据并备份，确定清除?")
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHelper.getInstance().getDBSessioin(RadomCidActivity.this).getSendCidDataDao().deleteAll();
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        build.create().show();

    }


    @OnClick(R.id.btn_export)
    public void exportData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在导出");
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                DaoSession daoSession = DBHelper.getInstance().getDBSessioin(RadomCidActivity.this);
                String selectTimeSql = "SELECT DISTINCT " + SendCidDataDao.Properties.Time.columnName
                        + " FROM " + SendCidDataDao.TABLENAME;
                Cursor c = daoSession.getDatabase().rawQuery(selectTimeSql, null);
                ArrayList<String> timeList = new ArrayList<>();
                try {
                    if (c != null) {
                        if (c.moveToFirst()) {
                            do {
                                timeList.add(c.getString(0));
                            } while (c.moveToNext());
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
                if (timeList.size() < 1) {
                    exportError(new Exception("No data"));
                    return;
                }
                File dir = FileUtil.openFile(exportDir);
                dir.mkdirs();
                String fileName = "";
                int size = timeList.size();
                int parseCount = 0;
                for (String time : timeList) {
                    fileName = exportDir + File.separator + time + ".txt";
                    List<SendCidData> list = daoSession.getSendCidDataDao()
                            .queryBuilder().where(SendCidDataDao.Properties.Time.eq(time)).list();

                    Log.i(TAG, "exprot file=" + fileName + ", size=" + list.size());
                    File file = FileUtil.openFile(fileName);
                    file.delete();

                    try {
                        file.createNewFile();
                        BufferedWriter bw = null;
                        bw = new BufferedWriter(new FileWriter(file));
                        for (SendCidData data : list) {
                            bw.write("\"MB-UZHSH-0000\"\t" +
                                    "\"" + data.cid + "\"" + "\t" +
                                    "\"null\"" + "\t" +
                                    "\"" + data.getTime() + "\"" +
                                    "\n");
                        }
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        parseCount++;
                        if (parseCount >= size) {
                            exportDone();
                        }
                    }
                }
            }
        }).start();
    }

    private void exportError(final Exception e) {
        Log.i(TAG, "导出异常", e);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RadomCidActivity.this,
                        "导出异常" + e,
                        Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    private void exportDone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RadomCidActivity.this,
                        "导出完成，导出至存储卡 " + exportDir + " 目录",
                        Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    class IntevalTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            boolean enable = false;
            if (!TextUtils.isEmpty(s.toString())) {
                int value = Integer.valueOf(s.toString());
                enable = value > 0;
            }

            btnStart.setEnabled(false);
        }
    }

    RequestClient.Observer observer = new RequestClient.Observer() {
        @Override
        public void onAlreadySendDataChange(final int num) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (num >= RequestClient.getSendCount(RadomCidActivity.this)) {
                        Toast.makeText(RadomCidActivity.this, "发送完成", Toast.LENGTH_LONG).show();
                        btnStart.setEnabled(true);
                        btnGoOn.setEnabled(true);
                        btnPause.setEnabled(false);
                    }
                    tvSendResult.setText("已发送：" + num + "条");
                }
            });
        }
    };

    RadomCidService.Observer serviceObserver = new RadomCidService.Observer() {
        @Override
        public void onStatusChanged(final RadomCidService.Status status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    serviceStatusChanged(status);
                }
            });
        }
    };

    private void serviceStatusChanged(RadomCidService.Status status) {
        switch (status) {
            case RUNNING:
                btnStart.setEnabled(false);
                btnGoOn.setEnabled(false);
                btnPause.setEnabled(true);
                break;
            case PAUSE:
            case STOP:
                btnStart.setEnabled(true);
                btnGoOn.setEnabled(true);
                btnPause.setEnabled(false);
                break;
        }
    }
}
