package com.haier.uhome.usend;

import android.text.TextUtils;

import com.haier.uhome.usend.data.SendData;
import com.haier.uhome.usend.data.UserData;
import com.haier.uhome.usend.utils.PhoneModel;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @Author: majunling
 * @Data: 2017/1/19
 * @Description:
 */

public class SendDataGenerator {

    //上报数据 默认最大时间 2016-6-20 18:00:00
    private static long maxTime = 1466416800000L;
    //上报数据 默认最小时间 2016-6-20 8:00:00
    private static long minTime = 1466380800000L;

    static {
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

    /**
     * 生成统计数据
     * @param
     * @return
     */
    public static SendData generate(UserData userData){
        final String phoneModel = PhoneModel.getInstance().getRandomPhoneModel();
        String cid = userData.getCid();//generateCidByUid(userData.getUserId(), phoneModel);
        String netType = "WIFI";
        String location = "";
        long session = generateSession();

        return new SendData(userData.getUserId(), cid, phoneModel, session, netType, location);
    }

    /**
     * 随机生成session
     * @return
     */
    public static long generateSession() {
        Random random = new Random(System.currentTimeMillis());
        long max = maxTime - minTime;
        long rand = Math.abs(random.nextLong());
        rand = minTime + rand % max;
        if (rand > maxTime) {
            rand = maxTime;
        }
        return rand;
    }

    /**
     * 生成clientid
     * @param uid
     * @param phoneModel
     * @return
     */
    private static String generateCidByUid(String uid, String phoneModel){

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

    //数字转化成0-F字符串，类似mac字符串
    private static Map<String, String> macMap = new HashMap(){
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
    private static Map<String, String> numMap = new HashMap(){
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
}
