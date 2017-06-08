package com.haier.uhome.usend.data;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Unique;

/**
 * @Author: majunling
 * @Data: 2016/11/5
 * @Description: 上报数据
 */
@Entity
public class UserData {
    String userId;
    @Unique
    String cid;

    @Keep
    public UserData(String uid){
        userId = uid;
    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Generated(hash = 747751377)
    public UserData(String userId, String cid) {
        this.userId = userId;
        this.cid = cid;
    }

    @Generated(hash = 1838565001)
    public UserData() {
    }
}
