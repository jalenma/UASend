package com.haier.uhome.usend.data;

/**
 * 请求头
 *
 * @Author: majunling
 * @Data: 2017/1/19
 * @Description:
 */

import java.util.HashMap;
import java.util.Map;

/**
 BasicHeader appIdHeader = new BasicHeader("aid", "MB-UZHSH-0000");
 BasicHeader akHeader = new BasicHeader("ak", "f50c76fbc8271d361e1f6b5973f54585");
 BasicHeader cidHeader = new BasicHeader("cid", cid);
 BasicHeader ssHeader = new BasicHeader("ss", session);
 BasicHeader uidHeader = new BasicHeader("uid", userId);
 BasicHeader avHeader = new BasicHeader("av", APP_VERSIOIN);
 BasicHeader chHeader = new BasicHeader("ch", APP_CHANNEL);
 BasicHeader agHeader = new BasicHeader("ag", phoneModel);

 BasicHeader conTentType = new BasicHeader("Content-Type", "application/json;charset=UTF-8");
 BasicHeader accCharset = new BasicHeader("Accept-Charset", "UTF-8");
 */
public class SendHeader {


    private Map<String, String> headers;

    public SendHeader() {
        init();
    }

    private void init() {
        headers = new HashMap<>();
        headers.put("aid", "");
        headers.put("ak", "");
        headers.put("ch", "");
        headers.put("cid", "");
        headers.put("av", "");
        headers.put("ss", "");
        headers.put("uid", "");
        headers.put("ag", "");
        headers.put("Content-Type", "application/json;charset=UTF-8");
        headers.put("Accept-Charset", "UTF-8");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setAppId(String appId) {
        headers.put("aid", appId);
    }

    public void setAppKey(String appKey) {
        headers.put("ak", appKey);
    }

    public void setAppChannel(String channel) {
        headers.put("ch", channel);
    }

    public void setAppVersioin(String appVersioin) {
        headers.put("av", appVersioin);
    }

    public void setClientId(String clientId) {
        headers.put("cid", clientId);
    }

    public void setSession(String session) {
        headers.put("ss", session);
    }

    public void setUid(String uid) {
        headers.put("uid", uid);
    }

    public void setPhoneModel(String phoneModel) {
        headers.put("ag", phoneModel);
    }


}
