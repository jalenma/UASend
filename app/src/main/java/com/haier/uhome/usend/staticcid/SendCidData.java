package com.haier.uhome.usend.staticcid;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * ${todo}(这里用一句话描述这个类的作用)
 *
 * @author majunling
 * @date 2017/6/11
 */
@Entity
public class SendCidData {

    String cid;
    String time;

    @Generated(hash = 2025250662)
    public SendCidData(String cid, String time) {
        this.cid = cid;
        this.time = time;
    }

    @Generated(hash = 742861775)
    public SendCidData() {
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString() {
        return "SendCidData["
                + "cid=" + cid
                + ", time=" + time
                + "]";
    }

    public String toFileFormat(){
        return cid + "\t" + time;
    }
}
