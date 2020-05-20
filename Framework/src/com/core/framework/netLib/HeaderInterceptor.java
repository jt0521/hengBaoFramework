package com.core.framework.netLib;

import android.text.TextUtils;

import com.core.framework.app.MyApplication;
import com.core.framework.app.devInfo.DeviceInfo;
import com.core.framework.app.oSinfo.AppConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * http Header拦截器 公共
 */
public class HeaderInterceptor implements Interceptor {
    /**
     * keys
     */
    private static final String KEY_USER_AGENT = "User-Agent";
    private static final String KEY_TIMESTAMP = "X-Timestamp"; // 当前事件戳毫秒;
    private static final String KEY_SIGNATURE = "X-Signature"; // 大写的请求方法+ \n +时间戳+ \n +token+ \n
    private static final String KEY_ENCRYPT = "X-Encrypt"; // 固定传sha256
    private static final String KEY_UUID = "X-Uuid"; // 设备码

    /**
     * 寿险
     */
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json; charset=utf-8";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    private String mUserAgent;

    public HeaderInterceptor() {
        mUserAgent = getUserAgent();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request.Builder builder = chain.request().newBuilder();

        //赋值静态可变量
        builder.addHeader("X-Tuan800-Platform", "Android")
                .addHeader(KEY_USER_AGENT, getUserAgent())
                .addHeader(ACCEPT, ACCEPT_VALUE)
                .addHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .addHeader(AUTHORIZATION,"Bearer "+ GobalData.TOKEN_SX);
        if (TextUtils.equals(chain.request().method(), "GET")) {
            builder.addHeader("Accept-Encoding", "gzip");
        }
        Request request = builder.build();
        return chain.proceed(request);
    }

    private String getUserAgent() {
        if (!TextUtils.isEmpty(mUserAgent)) {
            return mUserAgent;
        }
        StringBuilder sb = new StringBuilder("tbbz|");
        sb.append(AppConfig.CLIENT_TAG).append("|").append(DeviceInfo.getDeviceId()).append("|").append("Android")
                .append("|").append(MyApplication.getInstance().getVersionName()).append("|")
                .append(AppConfig.PARTNER_ID);
        return sb.toString();
    }
}
