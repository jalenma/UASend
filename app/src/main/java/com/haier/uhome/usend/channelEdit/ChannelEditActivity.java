package com.haier.uhome.usend.channelEdit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.haier.uhome.usend.R;
import com.haier.uhome.usend.data.Channels;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class ChannelEditActivity extends AppCompatActivity {

    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.et_channels)
    EditText etChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_edit);

        ButterKnife.bind(this);

        etChannels.setText(Channels.getChannelsString(this));

        etChannels.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etChannels.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String validReg = "[a-zA-Z0-9\\-,]+";

                Pattern pattern = Pattern.compile(validReg);
                if (!pattern.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        }});
    }


    @OnClick(R.id.btn_save)
    public void save() {
        String channels = etChannels.getText().toString();
        if(TextUtils.isEmpty(channels)){
            return;
        }
        Channels.saveChannels(this, channels.split(","));
    }
}
