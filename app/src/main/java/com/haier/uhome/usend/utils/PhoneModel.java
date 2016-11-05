package com.haier.uhome.usend.utils;

import android.content.Context;
import android.text.TextUtils;

import com.haier.uhome.usend.FileUtil;
import com.haier.uhome.usend.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @Author: majunling
 * @Data: 2016/10/22
 * @Description:
 */
public class PhoneModel {

    private final String FILE_NAME = "phoneModelList.txt";

    Context context;

    private String[] phoneModelArr;

    private static PhoneModel sInstance;

    private PhoneModel(Context context){
        this.context = context;
        loadData();
    }

    public static void init(Context context){
        sInstance = new PhoneModel(context);
    }

    public static PhoneModel getInstance(){
        if(null == sInstance){
            throw new RuntimeException("not init");
        }
        return sInstance;
    }

    public String[] getPhoneModelArr() {
        return phoneModelArr;
    }

    /**
     * 随机获取一个手机型号
     */
    public String getRandomPhoneModel(){
        Random random = new Random();
        int index = random.nextInt(phoneModelArr.length - 1);
        Log.i("jalen", phoneModelArr[index]);
        return phoneModelArr[index];
    }

    private void loadData(){
        String data = null;
        try {
            data = loadPreparedPhoneModel();
//            Log.i("jalen", data);
            if(!TextUtils.isEmpty(data)){
                phoneModelArr = data.split("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadPreparedPhoneModel() throws IOException {
        InputStream inputStream = null;
        String ret = null;
        try {
            inputStream = context.getAssets().open(FILE_NAME);
            ret =  new String(FileUtil.readInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != inputStream){
                inputStream.close();
            }
            return ret;
        }
    }
}
