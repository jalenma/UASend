package com.haier.uhome.usend;

import android.content.Context;

import com.haier.uhome.usend.data.SendData;
import com.haier.uhome.usend.data.SendHeader;
import com.haier.uhome.usend.log.Log;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.UnsupportedEncodingException;
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

    private static UARequest sInstance;

    private Random random;

    public static UARequest getInstance() {
        if (sInstance == null) {
            sInstance = new UARequest();
        }
        return sInstance;
    }

    public UARequest() {
        random = new Random(System.currentTimeMillis());
    }

    private Header[] generateHeader(SendHeader sendHeader) {
        if (sendHeader == null || sendHeader.getHeaders().isEmpty()) {
            return null;
        }
        int size = sendHeader.getHeaders().size();
        int i = 0;
        Header[] headers = new Header[size];
        for (Map.Entry<String, String> entry : sendHeader.getHeaders().entrySet()) {
            headers[i++] = new BasicHeader(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * app启动，app使用
     * @param context
     * @param sendData
     * @param callback
     */
    public void sendAppStartUse(final Context context, final SendData sendData, final RequestResult
            callback) {

        RequestAppStartResult appStartResult = new RequestAppStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                //App use 事件
                sendAppUseRequest(context, sendData, new RequestAppUseResult() {
                    @Override
                    public void onSuccess(int code, String response) {
                        if (null != callback) {
                            callback.onSuccess(code, response);
                        }
                    }

                    @Override
                    public void onFailure(int code, String response) {
                        if (null != callback) {
                            callback.onFailure(code, response);
                        }
                    }
                });
            }

            @Override
            public void onFailure(int code, String response) {
                if (null != callback) {
                    callback.onFailure(code, response);
                }
            }
        };

        //app启动(app start)请求
        sendAppStartRequest(context, sendData, appStartResult);
    }


    public void sendAppAndUserStartBatch(final Context context, final SendData sendData, final RequestResult callback) {

        final RequestUserStartResult userStartResult = new RequestUserStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                sendAppUseRequest(context, sendData, new RequestAppUseResult() {
                        @Override
                        public void onSuccess(int code, String response) {
                            if (null != callback) {
                                callback.onSuccess(code, response);
                            }
                        }

                        @Override
                        public void onFailure(int code, String response) {
                            if (null != callback) {
                                callback.onFailure(code, response);
                            }
                        }
                    });
            }

            @Override
            public void onFailure(int code, String response) {
                if (null != callback) {
                    callback.onFailure(code, response);
                }
            }
        };

        /*RequestAppStartResult appStartResult = new RequestAppStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                sendUserStartRequest(context, sendData, userStartResult);
            }

            @Override
            public void onFailure(int code, String response) {
                if (null != callback) {
                    callback.onFailure(code, response);
                }
            }
        };*/
        RequestAppStartResult appStartResult = new RequestAppStartResult() {
            @Override
            public void onSuccess(int code, String response) {
                //sendUserStartRequest(context, sendData, userStartResult);
                sendAppUseRequest(context, sendData, new RequestAppUseResult() {
                    @Override
                    public void onSuccess(int code, String response) {
                        if (null != callback) {
                            callback.onSuccess(code, response);
                        }
                    }

                    @Override
                    public void onFailure(int code, String response) {
                        if (null != callback) {
                            callback.onFailure(code, response);
                        }
                    }
                });
            }

            @Override
            public void onFailure(int code, String response) {
                if (null != callback) {
                    callback.onFailure(code, response);
                }
            }
        };

        //启动请求
        sendAppStartRequest(context, sendData, appStartResult);
    }

    // app启动请求
    public void sendAppStartRequest(Context context, final SendData sendData, final RequestAppStartResult callback) {
        //启动请求
        try {
            HttpRequestManager.post(context, URL, generateHeader(sendData.getHeader()), new StringEntity(sendData
                    .getAppStartData()),
                new
                    HttpRequestManager
                        .RequestTextCallback() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String response) {
                            if (null != callback) {
                                callback.onSuccess(statusCode, response);
                            }
                            Log.i(TAG, "UA-MI send app start success pud = " + sendData.getUid() + ", pcd=" +
                                sendData.getClientId());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String response) {
                            if (null != callback) {
                                callback.onFailure(statusCode, response);
                            }
                            Log.i(TAG, "UA-MI send app start fail pud = " + sendData.getUid() + ", pcd=" + sendData
                                .getClientId());
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //用户启动请求，登录完成后事件
    public void sendUserStartRequest(Context context, final SendData sendData, final RequestUserStartResult callback) {

        //user start 事件
        try {
            HttpRequestManager.post(context, URL, generateHeader(sendData.getHeader()), new StringEntity(sendData
                    .getUserStartData()),
                new
                    HttpRequestManager
                        .RequestTextCallback() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String response) {
                            if (null != callback) {
                                callback.onSuccess(statusCode, response);
                            }
                            Log.i(TAG, "UA-MI send user start success userId = " + sendData.getUid() + ", pcd=" +
                                sendData.getClientId());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String response) {
                            if (null != callback) {
                                callback.onFailure(statusCode, response);
                            }
                            Log.i(TAG, "UA-MI send user start fail userId = " + sendData.getUid() + ", pcd=" +
                                sendData.getClientId());
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 页面使用统计
     *
     * @param context
     * @param callback
     */
    public void sendAppUseRequest(Context context, final SendData sendData, final RequestAppUseResult callback) {

        try {
            HttpRequestManager.post(context, URL, generateHeader(sendData.getHeader()), new StringEntity(sendData
                .getPageStayTime()), new
                HttpRequestManager
                    .RequestTextCallback() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String response) {
                        if (null != callback) {
                            callback.onSuccess(statusCode, response);
                        }
                        Log.i(TAG, "UA-MI send app use success userId = " + sendData.getUid() + ", pcd=" + sendData
                            .getClientId());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String response) {
                        if (null != callback) {
                            callback.onFailure(statusCode, response);
                        }
                        Log.i(TAG, "UA-MI send app use fail userId = " + sendData.getUid() + ", pcd=" + sendData
                            .getClientId());
                    }
                });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void cancelRequst(Context context) {
        HttpRequestManager.cancelRequest(context);
    }

    public interface RequestResult {
        void onSuccess(int code, String response);

        void onFailure(int code, String response);
    }

    public interface RequestAppStartResult {
        void onSuccess(int code, String response);

        void onFailure(int code, String response);
    }

    public interface RequestUserStartResult {
        void onSuccess(int code, String response);

        void onFailure(int code, String response);
    }

    public interface RequestAppUseResult {
        void onSuccess(int code, String response);

        void onFailure(int code, String response);
    }
}
