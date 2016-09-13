package com.haier.uhome.usend.setting;

import android.content.Context;
import android.text.TextUtils;

import com.haier.uhome.usend.HttpRequestManager;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Author: majunling
 * @Data: 2016/6/27
 * @Description:
 */
public class SettingClient {
    private static SettingClient instance;

    private static final String SEVER_URL = "http://code.taobao.org/svn/uatest/trunk/ua.config";

    private SettingInfo settingInfo;

    public static SettingClient getInstance() {
        if (instance == null) {
            instance = new SettingClient();
        }
        return instance;
    }

    private SettingClient() {
        settingInfo = new SettingInfo();
    }

    public SettingInfo getSettingInfo() {
        return settingInfo;
    }

    public void getSettings(Context context, final ResultCallback<SettingInfo> resultCallback) {
        HttpRequestManager.get(context, SEVER_URL, null, new HttpRequestManager.RequestTextCallback() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String response) {
                if (!TextUtils.isEmpty(response)) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int uaSwitch = jsonObject.optInt("ua_switch");
                        settingInfo.setUaSwitch(uaSwitch == 1);
                        settingInfo.setSendCount(jsonObject.optInt("send_count"));

                        //其他用户开关
                        int otherUaSwitch = jsonObject.optInt("other_user_switch");
                        settingInfo.setOtherUaSwitch(otherUaSwitch == 95);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if(resultCallback != null){
                    resultCallback.onSuccess(settingInfo);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response) {
                if(resultCallback != null){
                    resultCallback.onFailure(settingInfo);
                }
            }
        });
    }


}
