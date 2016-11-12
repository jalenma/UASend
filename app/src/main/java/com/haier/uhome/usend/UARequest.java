package com.haier.uhome.usend;

import android.content.Context;
import android.text.TextUtils;

import com.haier.uhome.usend.log.Log;
import com.haier.uhome.usend.utils.PhoneModel;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Author: majunling
 * @Data: 2016/6/13
 * @Description:
 */
public class UARequest {

    private static final String TAG = "UA-UARequest";
    private static final String URL = "http://uhome.haier.net:6050/logserver/sdklog";


    private static final String APP_CHANNLE = "yunying";
    private static final String APP_VERSIOIN = "2.1.1-2016052401";

    private static UARequest sInstance;
    //上报数据 默认最大时间 2016-6-20 18:00:00
    private static long maxTime = 1466416800000L;
    //上报数据 默认最小时间 2016-6-20 8:00:00
    private static long minTime = 1466380800000L;

    private static final long INV_TIME = 1111011010111L;

    private Random random;

    public static UARequest getInstance() {
        if (sInstance == null) {
            sInstance = new UARequest();
        }
        return sInstance;
    }

    public UARequest() {
        random = new Random(System.currentTimeMillis());

        //最大时间当前时间
        maxTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 8, 0, 0);

        //最小时间当天8点
        minTime = calendar.getTimeInMillis();

        if (minTime >= maxTime) {
            minTime = maxTime - 60 * 60 * 1000;
        }
    }

    public void sendAppAndUserStartBatch(final Context context, final String userId, final RequestResult callback){
        //请求头里的session, app初始化时生成，后面所有事件事件必须必比session大
        final long session = generateSession();

        final String phoneModel = PhoneModel.getInstance().getRandomPhoneModel();
        //请求头里的cid
        final String cid = generateCidByUid(userId, phoneModel);

        final long appStartEventTime = session + random.nextInt(10100) % 10100;
        final long userStartEventTime = appStartEventTime + random.nextInt(10100) % 10100;

        final RequestUserStartResult userStartResult = new RequestUserStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                if(null != callback){
                    callback.onSuccess(code, response);
                }
            }

            @Override
            public void onFailure(int code, String response) {
                if(null != callback){
                    callback.onSuccess(code, response);
                }
            }
        };

        RequestAppStartResult appStartResult = new RequestAppStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                sendUserStartRequest(context, session, userStartEventTime, cid, userId, phoneModel, userStartResult);
            }

            @Override
            public void onFailure(int code, String response) {
            }
        };

        //启动请求
        sendAppStartRequest(context, session, appStartEventTime, cid, userId, phoneModel, appStartResult);
    }

    // app启动请求
    public void sendAppStartRequest(Context context, long session, long appStartEventTime, final String cid,  final
        String userId, String phoneModel, final RequestAppStartResult callback) {
        //启动请求
        String requestBody = generateStartJson(getFormateTime(appStartEventTime));
        Header[] headers = getHeaders("", cid, phoneModel, String.valueOf(session));
        try {
            HttpRequestManager.post(context, URL, headers, new StringEntity(requestBody), new HttpRequestManager
                .RequestTextCallback() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onSuccess(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send app start success pud = " + userId + ", pcd=" + cid);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onFailure(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send app start fail pud = " + userId + ", pcd=" + cid);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //用户启动请求，登录完成后事件
    public void sendUserStartRequest(Context context, long session, long userStartEventTime, final String cid,
                                     final String userId, String phoneModel, final RequestUserStartResult callback) {
        String userStartTime = getFormateTime(userStartEventTime);
        Header[] userHeaders = getHeaders(userId, cid, phoneModel, String.valueOf(session));
        String userStartBody = generateUserStartJson(userId, userStartTime);

        //user start 事件
        try {
            HttpRequestManager.post(context, URL, userHeaders, new StringEntity(userStartBody), new HttpRequestManager
                .RequestTextCallback() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onSuccess(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send user start success userId = " + userId + ", pcd=" + cid);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onFailure(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send user start fail userId = " + userId + ", pcd=" + cid);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void sendRequset(Context context, final String userId, final RequestResult callback) {
        //请求头里的session
        long session = generateSession();
        long tmp = session;
        //将session里的几位设置成0，保证比 请求body里的time小
        session = session - random.nextInt(10100) % 10100;
        //请求body里的time
        String eventTime = getFormateTime(tmp);
        final String cid = generateCid(userId);

        //启动请求
        String requestBody = generateStartJson(eventTime);
        Header[] headers = getHeaders("", cid, "", String.valueOf(session));
        try {
            HttpRequestManager.post(context, URL, headers, new StringEntity(requestBody), new HttpRequestManager
                .RequestTextCallback() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onSuccess(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send app start success pud = " + userId + ", pcd=" + cid);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response) {
                    if(null != callback){
                        callback.onFailure(statusCode, response);
                    }
                    Log.i(TAG, "UA-MI send app start fail pud = " + userId + ", pcd=" + cid);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /*tmp += random.nextInt(10000);
        String userStartTime = getFormateTime(tmp);
        Header[] userHeaders = getHeaders(userId, cid, String.valueOf(session));
        String userStartBody = generateUserStartJson(userId, userStartTime);

        //user start 事件
        try {
            HttpRequestManager.post(context, URL, userHeaders, new StringEntity(userStartBody), new HttpRequestManager
                .RequestTextCallback() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    increateSuccUserStartCount();
                    Log.i(TAG, "send user start success userId = " + userId);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response) {
                    increateAppStartCount();
                    Log.i(TAG, "send user start fail userId = " + userId);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * aid	MB-UZHSH-0000
     * ak	f50c76fbc8271d361e1f6b5973f54585
     * cid	96136CA5E8C7C57AF554A937B6573F7D
     * ss	1465702367445
     * uid	263550
     * av	2.2.0-2016060802
     * ch	inmobi
     * ag	Xiaomi|Redmi Note 3
     * Content-Type	application/json;charset=UTF-8
     * Accept-Charset	UTF-8
     *
     * @return
     */

    private static Header[] getHeaders(String userId, String cid, String phoneModel, String session) {

        BasicHeader appIdHeader = new BasicHeader("aid", "MB-UZHSH-0000");
        BasicHeader akHeader = new BasicHeader("ak", "f50c76fbc8271d361e1f6b5973f54585");
        BasicHeader cidHeader = new BasicHeader("cid", cid);
        BasicHeader ssHeader = new BasicHeader("ss", session);
        BasicHeader uidHeader = new BasicHeader("uid", userId);
        BasicHeader avHeader = new BasicHeader("av", APP_VERSIOIN);
        BasicHeader chHeader = new BasicHeader("ch", APP_CHANNLE);
        BasicHeader agHeader = new BasicHeader("ag", phoneModel);

        BasicHeader conTentType = new BasicHeader("Content-Type", "application/json;charset=UTF-8");
        BasicHeader accCharset = new BasicHeader("Accept-Charset", "UTF-8");
        Header[] headers = {appIdHeader, akHeader, cidHeader, ssHeader, uidHeader, avHeader, chHeader, agHeader,
            conTentType, accCharset};
        return headers;
    }

    private static String generateStartJson(String eventTime) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("net", "WIFI");
            jsonObject.put("cmd", 1002);
            jsonObject.put("tm", eventTime);
            jsonObject.put("loc", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private static String generateUserStartJson(String userId, String eventTime) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("eid", "t_user_start");

            jsonObject.put("tm", "2016-06-12 12:33:32.192");
            jsonObject.put("cmd", 1004);

            JSONObject pa = new JSONObject();
            pa.put("userId", userId);

            jsonObject.put("pa", pa);
            jsonObject.put("net", "WIFI");
            jsonObject.put("acc", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 时间戳转换为 2016-06-12 12:33:32.192
     *
     * @param time
     * @return
     */
    public static String getFormateTime(long time) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dataFormat.format(new Date(time));
    }


    /**
     * 随机生成session
     *
     * @return
     */
    public long generateSession() {
        long max = maxTime - minTime;
        long rand = Math.abs(random.nextLong());
        rand = minTime + rand % max;
        if (rand > maxTime) {
            rand = maxTime;
        }
        return rand;
    }

    /**
     * 根据userid 生成 cid
     *
     * @param uid
     * @return
     */
    public String generateCid(String uid) {
        String source = uid;
        if (TextUtils.isEmpty(uid)) {
            source = String.valueOf(System.currentTimeMillis());
        } else {
            source = uid + String.valueOf(System.currentTimeMillis());
        }
        String cid = string2MD5(source);
        return cid.toUpperCase();
    }

    //数字转化成0-F字符串，类似mac字符串
    private Map<String, String> macMap = new HashMap(){
        {
            put("0", "1C");
            put("1", "D0");
            put("2", "EA");
            put("3", "2D");
            put("4", "3E");
            put("5", "9F");
            put("6", "48");
            put("7", "BA");
            put("8", "E0");
            put("9", "5E");
        }
    };
    //数字转化成另外一组数字
    private Map<String, String> numMap = new HashMap(){
        {
            put("0", "5");
            put("1", "9");
            put("2", "1");
            put("3", "7");
            put("4", "4");
            put("5", "8");
            put("6", "0");
            put("7", "2");
            put("8", "6");
            put("9", "3");
        }
    };

    private String generateCidByUid(String uid, String phoneModel){

        String source = uid;
        if (!TextUtils.isEmpty(uid)) {
            StringBuffer outMacString = new StringBuffer();
            StringBuffer outNumString = new StringBuffer();
            String key = "0";
            for(int i = 0; i< uid.length(); i++){
                key = String.valueOf(uid.charAt(i));
                outMacString.append(macMap.get(key));
                outNumString.append(numMap.get(key));
            }
            String outPhomeModel = TextUtils.isEmpty(phoneModel) ? "00" : String.valueOf(phoneModel.length());
            source = outMacString.toString() + outNumString.toString() + outPhomeModel;
        } else {
            source = uid + String.valueOf(System.currentTimeMillis());
        }
        String cid = string2MD5(source);
        return cid.toUpperCase();
    }


    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    public void cancelRequst(Context context){
        HttpRequestManager.cancelRequest(context);
    }

    public interface RequestResult{
        void onSuccess(int code, String response);
        void onFailure(int code, String response);
    }

    public interface RequestAppStartResult{
        void onSuccess(int code, String response);
        void onFailure(int code, String response);
    }

    public interface RequestUserStartResult{
        void onSuccess(int code, String response);
        void onFailure(int code, String response);
    }
}
