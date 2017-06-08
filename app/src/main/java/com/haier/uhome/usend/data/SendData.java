package com.haier.uhome.usend.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @Author: majunling
 * @Data: 2017/1/19
 * @Description:
 */

public class SendData {

    public static final String APP_ID = "MB-UZHSH-0000";
    private static final String APP_KEY = "f50c76fbc8271d361e1f6b5973f54585";
    private static final String APP_CHANNEL = "yunying";
    private static final String APP_VERSIOIN = "2.1.1-2016052401";

    //client id
    private static final String DEFAULT_CID = "64B9BEFC45D0C49F6E37FDD8F05E6881";
    //session，第一次启动记录
    private static final String DEFAULT_SESSION = "1484796436939";
    //手机型号
    private static final String DEFAULT_PHONE_MODEL = "samsung|SM-g9350";

    private String uid;
    private String clientId;
    private String phoneModel;
    private long session;
    private String netType = "WIFI";
    private String location = "";

    //请求头
    private SendHeader header;
    //app启动数据
    private String appStartData;
    //用户启动/绑定数据
    private String userStartData;
    //页面停留数据
    private String pageStayTime;

    private Random random;

    public SendData(String uid, String clientId, String phoneModel, long session, String netType, String location) {
        this.uid = uid;
        this.clientId = clientId;
        this.phoneModel = phoneModel;
        this.session = session;
        this.netType = netType;
        this.location = location;
        random = new Random(System.currentTimeMillis());
        initHeader();
        generateData();
    }

    private void initHeader(){
        header = new SendHeader();
        header.setAppId(APP_ID);
        header.setAppKey(APP_KEY);
        header.setAppChannel(APP_CHANNEL);
        header.setAppVersioin(APP_VERSIOIN);
        header.setSession(String.valueOf(session));
        header.setClientId(clientId);
        header.setUid(uid);
        header.setPhoneModel(phoneModel);
    }

    public SendHeader getHeader() {
        return header;
    }

    public void generateData() {
        final long appStartEventTime = session + random.nextInt(10100) % 10100;
        final long userStartEventTime = appStartEventTime + random.nextInt(10100) % 10100;

        appStartData = assembleAppStartJson(getFormateTime(appStartEventTime), netType, location);
        userStartData = assembleUserStartJson(uid, getFormateTime(userStartEventTime), netType);

        long startTimeInt = session + 10100 + random.nextInt(10100) % 10100;
        long endTimeInt = startTimeInt + 10100 + random.nextInt(50100) % 50100;
        String pageName = "com.haier.uhome.uplus.ui.activity.TabDeviceListActivity";
        pageStayTime = assemblePageStayJson(uid, getFormateTime(startTimeInt), getFormateTime(endTimeInt), pageName,
            netType);
    }

    /**
     * 组装启动事件
     *
     * @param eventTime
     * @param netType
     * @param location
     * @return
     */
    private static String assembleAppStartJson(String eventTime, String netType, String location) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("net", netType);
            jsonObject.put("cmd", 1002);
            jsonObject.put("tm", eventTime);
            jsonObject.put("loc", location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 组装用户绑定、登录
     *
     * @param userId
     * @param eventTime
     * @param netType
     * @return
     */
    private static String assembleUserStartJson(String userId, String eventTime, String netType) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", "t_user_start");
            jsonObject.put("tm", eventTime);
            jsonObject.put("cmd", 1004);
            JSONObject pa = new JSONObject();
            pa.put("userId", userId);
            jsonObject.put("pa", pa);
            jsonObject.put("net", netType);
            jsonObject.put("acc", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 组装页面使用时长
     *
     * @param userId
     * @param startTime
     * @param endTime
     * @param pageName
     * @param netType
     * @return
     */
    private static String assemblePageStayJson(String userId, String startTime, String endTime, String pageName,
                                               String netType) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd", 1003);
            jsonObject.put("st", startTime);
            jsonObject.put("et", endTime);
            jsonObject.put("eid", "t_app_use");
            jsonObject.put("net", "WIFI");
            jsonObject.put("lb", pageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String getFormateTime(long time) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dataFormat.format(new Date(time));
    }

    public String getPageStayTime() {
        return pageStayTime;
    }

    public String getUid() {
        return uid;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public long getSession() {
        return session;
    }

    public String getNetType() {
        return netType;
    }

    public String getLocation() {
        return location;
    }

    public String getAppStartData() {
        return appStartData;
    }

    public String getUserStartData() {
        return userStartData;
    }
}
