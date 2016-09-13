package com.haier.uhome.usend.setting;


/**
 * @Author: majunling
 * @Data: 2016/6/27
 * @Description:
 */
public interface ResultCallback<T extends Object> {
    void onSuccess(T result);
    void onFailure(T result);
}
