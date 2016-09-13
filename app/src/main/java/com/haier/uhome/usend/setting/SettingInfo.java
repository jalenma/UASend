package com.haier.uhome.usend.setting;

/**
 * @Author: majunling
 * @Data: 2016/6/27
 * @Description:
 */
public class SettingInfo {
    // 管理员是否开启
    private boolean uaSwitch;
    // 请求条数
    private int sendCount;

    //其他用户是否开启
    private boolean otherUaSwitch;

    public boolean isUaSwitch() {
        return uaSwitch;
    }

    public void setUaSwitch(boolean uaSwitch) {
        this.uaSwitch = uaSwitch;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public boolean isOtherUaSwitch() {
        return otherUaSwitch;
    }

    public void setOtherUaSwitch(boolean otherUaSwitch) {
        this.otherUaSwitch = otherUaSwitch;
    }
}
