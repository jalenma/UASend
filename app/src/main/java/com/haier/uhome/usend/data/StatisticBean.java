package com.haier.uhome.usend.data;

/**
 * @Author: majunling
 * @Data: 2016/11/5
 * @Description: 上报数据
 */
public class StatisticBean {
    String userId;
    String cid;

    public StatisticBean(String uid){
        userId = uid;
    }

    public String getUserId() {
        return userId;
    }

    public String getCid() {
        return cid;
    }
}
