package com.haier.uhome.usend;

import android.content.Context;
import android.text.TextUtils;

import com.haier.uhome.usend.log.Log;

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
    //默认最大时间 2016-6-20 18:00:00
    private static long MAX_TIME = 1466416800000L;
    //默认最小时间 2016-6-20 8:00:00
    private static long MIN_TIME = 1466380800000L;

    private static final long INV_TIME = 1111011010111L;

    private Random random;

    private int successAppStartCount = 0;
    private int failAppStartCount = 0;
    private int successUserStartCount = 0;
    private int failUserStartCount = 0;


    public static UARequest getInstance() {
        if (sInstance == null) {
            sInstance = new UARequest();
        }
        return sInstance;
    }

    public UARequest() {
        random = new Random(System.currentTimeMillis());

        //最大时间当前时间
        MAX_TIME = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 8, 0, 0);

        //最小时间当天8点
        MIN_TIME = calendar.getTimeInMillis();

        if (MIN_TIME >= MAX_TIME) {
            MIN_TIME = MAX_TIME - 60 * 60 * 1000;
        }
    }

    public void reset() {
        successAppStartCount = 0;
        failAppStartCount = 0;
        successUserStartCount = 0;
        failUserStartCount = 0;
    }

    public int getSuccessAppStartCount() {
        return successAppStartCount;
    }

    public int getFailAppStartCount() {
        return failAppStartCount;
    }

    public int getSuccessUserStartCount() {
        return successUserStartCount;
    }

    public int getFailUserStartCount() {
        return failUserStartCount;
    }

    /**
     * 启动事件成功
     */
    private void increateSuccAppStartCount() {
        //synchronized (this){
        successAppStartCount++;
        //}
    }

    private void increateFailAppStartCount() {
        //synchronized (this){
        failAppStartCount++;
        //}
    }

    /**
     * 用户启动事件成功
     */
    private void increateSuccUserStartCount() {
        //synchronized (this){
        successUserStartCount++;
        //}
    }

    private void increateAppStartCount() {
        //synchronized (this){
        failUserStartCount++;
        //}
    }

    public void sendRequset(Context context, final String userId) {
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
        Header[] headers = getHeaders("", cid, String.valueOf(session));
        try {
            HttpRequestManager.post(context, URL, headers, new StringEntity(requestBody), new HttpRequestManager
                .RequestTextCallback() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String response) {
                    increateSuccAppStartCount();
                    Log.i(TAG, "UA-MI send app start success pud = " + userId + ", pcd=" + cid);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response) {
                    increateFailAppStartCount();
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

        Log.i(TAG, "result:" + getSendResult());
    }

    public String getSendResult() {
        return "successUserStartCount = " + successUserStartCount + ", failUserStartCount=" + failUserStartCount
            + ", successAppStartCount=" + successAppStartCount + ", failAppStartCount=" + failAppStartCount;
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

    private static Header[] getHeaders(String userId, String cid, String session) {

        BasicHeader appIdHeader = new BasicHeader("aid", "MB-UZHSH-0000");
        BasicHeader akHeader = new BasicHeader("ak", "f50c76fbc8271d361e1f6b5973f54585");
        BasicHeader cidHeader = new BasicHeader("cid", cid);
        BasicHeader ssHeader = new BasicHeader("ss", session);
        BasicHeader uidHeader = new BasicHeader("uid", userId);
        BasicHeader avHeader = new BasicHeader("av", APP_VERSIOIN);
        BasicHeader chHeader = new BasicHeader("ch", APP_CHANNLE);
        BasicHeader agHeader = new BasicHeader("ag", "");

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
        long max = MAX_TIME - MIN_TIME;
        long rand = Math.abs(random.nextLong());
        rand = MIN_TIME + rand % max;
        if (rand > MAX_TIME) {
            rand = MAX_TIME;
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
}
