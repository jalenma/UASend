package com.haier.uhome.usend;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.haier.uhome.usend.edit.SendInfoEditActivity;
import com.haier.uhome.usend.staticfile.SendActivity;
import com.haier.uhome.usend.staticcid.RadomCidActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_file_static)
    public void gotoFileStatic(){
        Intent it = new Intent();
        it.setClass(this, SendActivity.class);
        startActivity(it);
    }

    @OnClick(R.id.btn_radom_cid_static)
    public void gotoRadomCidStatic(){
        Intent it = new Intent();
        it.setClass(this, RadomCidActivity.class);
        startActivity(it);
    }
    @OnClick(R.id.btn_channel_edit)
    public void gotoChannelEdit(){
        Intent it = new Intent();
        it.setClass(this, SendInfoEditActivity.class);
        startActivity(it);
    }

}
