package com.haier.uhome.usend;

import android.content.Context;

import com.haier.uhome.usend.log.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestManager {

    private static final String TAG = "UA-HttpRequestManager";

    //edit by mjl, for v2.1 需求要求超时时间改成15秒
    private static final int DEFUALT_TIMEOUT = 15000;
    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(DEFUALT_TIMEOUT);
        client.setConnectTimeout(DEFUALT_TIMEOUT);
    }

    public HttpRequestManager() {
        // TODO Auto-generated constructor stub
    }

    /**
     * 设置请求连接超时
     *
     * @param milliseconds 超时时间，毫秒
     */
    public void setConnectionTimeout(int milliseconds) {
        client.setConnectTimeout(milliseconds);
    }

    /**
     * 设置socket 超时时间
     *
     * @param milliseconds 超时时间，毫秒
     */
    public void setSocketTimeout(int milliseconds) {
        client.setResponseTimeout(milliseconds);
    }

    /**
     * post请求
     *
     * @param context
     * @param url     请求url
     * @param headers 请求header
     * @param entity  请求entity
     * @param handler 请求返回处理
     */
    public static void post(Context context, String url, Header[] headers, HttpEntity entity,
                            AsyncHttpResponseHandler handler) {
        client.post(context, url, headers, entity, null, handler);
    }

    /**
     * post请求
     *
     * @param context
     * @param url
     * @param headers
     * @param entity
     * @param callback
     */
    public static void post(final Context context, final String url, final Header[] headers,
                            final HttpEntity entity, final RequestTextCallback callback) {
        TextHttpResponseHandler handler = new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] responseHeaders, String responseString) {
                log("post TextHttpResponse onSuccess", url, headers, entity, responseHeaders,
                    statusCode, responseString, null);
                if (callback != null) {
                    callback.onSuccess(statusCode, responseHeaders, responseString);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] responseHeaders, String responseString,
                                  Throwable throwable) {
                log("post TextHttpResponse onFailure", url, headers, entity, responseHeaders,
                    statusCode, responseString, throwable);
                if (callback != null) {
                    callback.onFailure(statusCode, responseHeaders, responseString);
                }
            }
        };
        client.post(context, url, headers, entity, null, handler);
    }


    public static void get(final Context context, final String url, final Header[] headers,
                           final RequestTextCallback callback) {
        TextHttpResponseHandler handler = new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] responseHeaders, String responseString) {
                log("get TextHttpResponse onSuccess", url, headers, null, responseHeaders,
                    statusCode, responseString, null);
                if (callback != null) {
                    callback.onSuccess(statusCode, responseHeaders, responseString);

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] responseHeaders, String responseString,
                                  Throwable throwable) {
                log("get TextHttpResponse onFailure", url, headers, null, responseHeaders,
                    statusCode, responseString, throwable);
                if (callback != null) {
                    callback.onFailure(statusCode, responseHeaders, responseString);

                }
            }
        };

        client.get(context, url, headers, null, handler);
    }

    private static void log(String msg, String url, Header[] inHeaders, HttpEntity entity,
                            Header[] outHeaders, int statusCode, String responseString, Throwable throwable) {
        try {
            Log.i(TAG, msg + "url=" + url
                + ", inHeaders=" + Arrays.toString(inHeaders)
                + ", entity=" + (entity == null ? "null" : EntityUtils.toString(entity))
                + ", statusCode=" + statusCode
                + ", outHeaders=" + Arrays.toString(outHeaders)
                + ", responseString=" + responseString
                + ",throwable=" + throwable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String headerToString(Header[] headers) {
        StringBuilder sb = new StringBuilder();
        for (Header header : headers) {
            sb.append(" ").append(header.toString());
        }
        return sb.toString();
    }

    /**
     * 取消网络请求
     *
     * @param context
     */
    public static void cancelRequest(Context context) {
        client.cancelRequests(context, true);
    }

    public interface RequestTextCallback {
        void onFailure(int statusCode, Header[] headers, String response);

        void onSuccess(int statusCode, Header[] headers, String response);
    }

    public interface RequestBinaryCallback {

        void onResult(int statusCode, Header[] headers, byte[] response);
    }


    /*-------------------------------------------*/

    public static void batchPost(){

    }

    public static class BatchData {
        ArrayList<Data> dataList;

        public BatchData(){
            dataList = new ArrayList<>();
        }

        public void add(String url, Map<String, String> headers, String body) {
            Data data = new Data();
            data.url = url;
            data.body = body;
            data.headers = new HashMap<>();
            if (headers != null && !headers.isEmpty()) {
                data.headers.putAll(headers);
            }
        }

        public void clear(){
            dataList.clear();
        }

        class Data {
            String url;
            Map<String, String> headers;
            String body;
        }
    }

    public interface RequestBatchDataCallback {
        void onFailure(int statusCode, Header[] headers, String response);

        void onSuccess(int statusCode, Header[] headers, String response);

        void onProcess(int statusCode, Header[] headers, String response);
    }
}
