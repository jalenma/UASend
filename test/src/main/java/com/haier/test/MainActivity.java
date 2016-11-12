package com.haier.test;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvPhoneInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String phoneInfo = Build.MANUFACTURER + "|" + Build.MODEL;

        tvPhoneInfo = (TextView)findViewById(R.id.txt);
        tvPhoneInfo.setText(phoneInfo);
    }
}
