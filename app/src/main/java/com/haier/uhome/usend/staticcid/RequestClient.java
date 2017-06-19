package com.haier.uhome.usend.staticcid;

import android.content.Context;
import com.haier.uhome.usend.utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ${todo}(这里用一句话描述这个类的作用)
 *
 * @author majunling
 * @date 2017/6/9
 */

public class RequestClient {

    //随机cid执行间隔
    public static final String RADOM_SEND_INTEVAL = "radom_send_inteval";

    //发送条数
    public static final String RADOM_SEND_COUNT = "radom_send_count";

    //已经发送条数
    public static final String RADOM_AlREADY_SEND_COUNT = "radom_already_send_count";

    /**
     * 发送间隔
     * @param context
     * @return
     */
    public static int getSendInteval(Context context){
        return PreferencesUtils.getInt(context, RADOM_SEND_INTEVAL, 1);
    }

    public static void setSendInteval(Context context, int value){
        PreferencesUtils.putInt(context, RADOM_SEND_INTEVAL, value);
    }

    /**
     * 请求条数
     * @param context
     * @return
     */
    public static int getSendCount(Context context){
        return PreferencesUtils.getInt(context, RADOM_SEND_COUNT, 0);
    }

    public static void setSendCount(Context context, int value){
        PreferencesUtils.putInt(context, RADOM_SEND_COUNT, value);
    }

    /**
     * 已经请求条数
     * @param context
     * @return
     */
    public static int getAlreadySendCount(Context context){
        return PreferencesUtils.getInt(context, RADOM_AlREADY_SEND_COUNT, 0);
    }

    public static void setAlreadySendCount(Context context, int value){
        PreferencesUtils.putInt(context, RADOM_AlREADY_SEND_COUNT, value);
        for(Observer observer : observerList){
            observer.onAlreadySendDataChange(value);
        }
    }

    private static List<Observer> observerList = new ArrayList<>();

    public static void addObserver(Observer observer){
        observerList.add(observer);
    }

    public static void removeObserver(Observer observer){
        observerList.remove(observer);
    }

    /**
     * 获取剩余需要请求条数
     * @param context
     * @return
     */
    public static int getRemainSendCount(Context context){
        return getSendCount(context) - getAlreadySendCount(context);
    }


    public static abstract class Observer{
        public void onAlreadySendDataChange(int num){}
    }
}
