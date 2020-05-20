package com.core.framework.net;

import android.app.Application;

import com.core.framework.net.callback.ResponseListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.OkHttpRequestBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.builder.PostStringBuilder;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 重写OkHttpUtils，方便替换、写入全局监听
 */

public class CustomOkHttpUtils {
    private static ResponseListener mResponseUiListener;
    private final static int CONNECT_TIME_OUT = 60 * 1000;
    private final static int READ_TIME_OUT = 60 * 1000;
    private final static int WRITE_TIME_OUT = 60 * 1000;

    public static ResponseListener getResponseUiListener() {
        return mResponseUiListener;
    }

    public static void setResponseUiListener(ResponseListener mResponseUiListener) {
        CustomOkHttpUtils.mResponseUiListener = mResponseUiListener;
    }
}
