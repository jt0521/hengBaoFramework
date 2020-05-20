package com.core.framework.net;

import android.util.Log;

import com.core.framework.app.MyApplication;
import com.core.framework.develop.LogUtil;
import com.core.framework.netLib.NetLog;
import com.core.framework.util.DESUtil;
import com.core.framework.util.JsonUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by IntelliJ IDEA. User: chenjishi Date: 11-4-11 Time: 上午10:20 To
 * change this template use File | Settings | File Templates.
 */
public class NetworkWorker {
//    public String ACCESS_TOKEN = "";// 用户token-登录之后的

    private static final int CONNECTION_TIMEOUT = 25 * 1000;
    private static final int SO_SOCKET_TIMEOUT = 50 * 1000;// qjb 缩短客户端出现
    // “网络异常小章鱼”的时间

    public static final int NATIVE_ERROR = 600;
    public static final int UNKNOWN_HOST = 601;
    public static final int SOCKET_TIMEOUT = 602;

    private static NetworkWorker inst = new NetworkWorker();

    public static NetworkWorker getInstance() {
        return inst;
    }

    private NetworkWorker() {
    }

    //规定每段显示的长度
    private static int LOG_MAXLENGTH = 2000;

    public static void e(String TAG, String msg) {
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                Log.e(TAG + i, msg.substring(start, end));
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                Log.e(TAG, msg.substring(start, strLength));
                break;
            }
        }
    }

    public HttpRequester generalRequester(Object... params) throws Exception {

        if (params == null || params.length == 0) {
            return new HttpRequester();
        }

        if (params.length > 0 && !(params[0] instanceof HttpRequester)) {
            throw new IllegalArgumentException(
                    "Http request need a HttpRequester param in the first");
        }
        if ((params.length > 1 && DESUtil.SECRET_DES.equals(params[1]) || (params.length == 1 && DESUtil.SECRET_DES.equals(((HttpRequester) params[0]).getSecretMode())))) {//des加密
            LogUtil.e("----http post---传入的加密前参数：" + ((HttpRequester) params[0]).mParams.toString());
            HashMap<String, Object> fileMap = new HashMap<>();
            HttpRequester requester = (HttpRequester) params[0];
            JSONObject object = new JSONObject();
            for (Map.Entry<String, Object> entry : requester.getParams().entrySet()) {
                if (entry.getValue() instanceof File) {
                    fileMap.put(entry.getKey(), entry.getValue());
                } else {
                    object.put(entry.getKey(), entry.getValue());
                }
            }
            String data = DESUtil.encrypt(object.toString(), DESUtil.SECRET_DES_KEY);
            if (params.length > 1 && DESUtil.SECRET_DES.equals(params[1])) {
                requester.getParams().clear();
                requester.getParams().put("data", data);
                requester.getParams().putAll(fileMap);
                LogUtil.d("----http post---传入的参数：" + ((HttpRequester) params[0]).mParams.toString());

                return (HttpRequester) params[0];


            } else {
                HttpRequester requesterNew = new HttpRequester();
                requesterNew.setMethod(requester.getMethod());
                requesterNew.setSecretMode(requester.getSecretMode());
                requesterNew.getParams().put("data", data);
                requesterNew.getParams().putAll(fileMap);
                LogUtil.d("----http post---传入的参数：" + ((HttpRequester) params[0]).mParams.toString());

                return requesterNew;
            }

        }

        return (HttpRequester) params[0];
    }

    /**
     * Asynchronous http get/post callback
     */
    public interface ICallback {
        public abstract void onResponse(int status, String result);
    }

    public static abstract class RequestListener<T> implements ICallback {
        @Override
        public void onResponse(int status, String result) {

        }

        public abstract void onSuccess(T response);
    }

    /**
     * 解密数据，如果不需要解密则直接返回
     *
     * @param params
     */
    private String decryptData(String url, String resultData, Object... params) {
        if (params.length > 1 && DESUtil.SECRET_DES.equals(params[1]) && !url.contains("Public/") && !url.contains("public/")) {
            JSONObject object = null;
            try {
                object = new JSONObject(resultData);
                resultData = DESUtil.decrypt(object.optString("key"), DESUtil.SECRET_DES_KEY);
                e("result=", url + "\n" + resultData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultData;
    }

    private class BaseCallback extends StringCallback {

        public Object[] mParam;
        public Object mHashMapParam;
        public ICallback mICallBack;

        public BaseCallback(Object hashMapParam, Object[] param, ICallback iCallback) {
            mHashMapParam = hashMapParam;
            mParam = param;
            mICallBack = iCallback;
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            int status = NATIVE_ERROR;
            String result = e == null ? "网络错误" : e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
            if (e instanceof UnknownHostException) {
                status = UNKNOWN_HOST;
            } else if (e instanceof InterruptedIOException) {
                status = SOCKET_TIMEOUT;
            } else if (e instanceof TimeoutException && result == null) {
                result = "连接超时";
            }
            if (result == null) {
                result = "网络错误";
            }
            NetLog.onFailure(call, id, e);
            onResponse(status, result);
            if (result.contains("reponse's code is : 401") && MyApplication.getInstance() != null) {
                MyApplication.getInstance().net401(call.request().url().toString(), call.request().method(), mHashMapParam, mParam, mICallBack);
            }
        }

        @Override
        public String parseNetworkResponse(Response response, int id) throws IOException {

            return NetLog.log(response);
        }

        @Override
        public void onResponse(String response, int id) {
            onResponse(200, response);
        }

        public void onResponse(int code, String response) {
        }
    }


    private void addGetParam(HttpRequester requester, GetBuilder builder) {
        if (requester != null && requester.getParams() != null) {
            for (Map.Entry<String, String> entry : requester.getRequestHeaders().entrySet()) {
                String string = entry.getValue();
                if (string == null) {
                    continue;
                }
                builder.addParams(entry.getKey(), string);
            }

            for (Map.Entry<String, Object> entry : requester.getParams().entrySet()) {
                Object object = entry.getValue();
                if (object == null) {
                    continue;
                }
                builder.addParams(entry.getKey(), object + "");
            }
        }
    }

    public void get(final String url, final ICallback callback,
                    final Object... params) {
        try {
            HttpRequester requester = generalRequester(params);
            GetBuilder builder = OkHttpUtils.get()
                    .url(url);
            addGetParam(requester, builder);
            builder.build().execute(new BaseCallback(null, params, callback) {
                                        @Override
                                        public void onResponse(int status, String result) {
                                            result = decryptData(url, result, params);
                                            callback.onResponse(status, result);
                                            e("result=", url + "\n" + result);
                                            if (callback instanceof RequestListener) {
                                                RequestListener requestListener = ((RequestListener) callback);
                                                Type type = null;
                                                try {
                                                    type = ((ParameterizedType) requestListener.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                                                } catch (Exception e) {

                                                }
                                                requestListener.onSuccess(JsonUtils.parseObject(result, type));
                                            }
                                        }
                                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void get(final String url, HashMap<String, String> param, Object tag, final ICallback callback) {
        OkHttpUtils.get()
                .url(url)
                .params(param)
                .tag(tag)
                .build().execute(new BaseCallback(param, null, callback) {

            @Override
            public void onResponse(String response, int id) {
                if (callback != null) {
                    callback.onResponse(200, response);
                }
            }
        });
    }

    private void addPostParam(HttpRequester requester, PostFormBuilder builder) {
        if (requester != null && requester.getParams() != null) {
            for (Map.Entry<String, String> entry : requester.getRequestHeaders().entrySet()) {
                String string = entry.getValue();
                if (string == null) {
                    continue;
                }
                builder.addParams(entry.getKey(), string);
            }
            for (Map.Entry<String, Object> entry : requester.getParams().entrySet()) {
                Object object = entry.getValue();
                if (object == null) {
                    continue;
                }
                if (object instanceof File) {
                    File file = (File) object;
                    builder.addFile(entry.getKey(), file.getName(), (File) object);
                } else {
                    builder.addParams(entry.getKey(), object + "");
                }
            }
        }
    }

    public void post(final String url, final ICallback callback, final Object... params) {
        post(url, callback, null, params);
    }

    public void post(final String url, final ICallback callback, String tag,
                     final Object... params) {
        HttpRequester requester = null;
        try {
            requester = generalRequester(params);
            PostFormBuilder builder = OkHttpUtils.post()
                    .url(url);
            if (tag == null) {
                builder.tag(tag);
            }
            addPostParam(requester, builder);
            builder.build().execute(new BaseCallback(null, params, callback) {
                @Override
                public void onResponse(int status, String result) {
                    result = decryptData(url, result, params);
                    callback.onResponse(status, result);
                    e("result=", url + "\n" + result);
                    if (callback instanceof RequestListener) {
                        RequestListener requestListener = ((RequestListener) callback);
                        Type type = null;
                        try {
                            type = ((ParameterizedType) requestListener.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                        } catch (Exception e) {

                        }
                        requestListener.onSuccess(JsonUtils.parseObject(result, type));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void post(final String url, final Object... params) {
        HttpRequester requester = null;
        try {
            requester = generalRequester(params);
            PostFormBuilder builder = OkHttpUtils.post()
                    .url(url);
            addPostParam(requester, builder);
            builder.build().execute(null);
        } catch (Exception e) {

        }
    }

    /**
     * post同步方法
     *
     * @param url
     * @param params
     * @return
     */
    public String postSynchronize(final String url, final Object... params) {
        HttpRequester requester = null;
        try {
            requester = generalRequester(params);
            PostFormBuilder builder = OkHttpUtils.post()
                    .url(url);
            addPostParam(requester, builder);
            Response response = builder.build().execute();
            return decryptSynchronize(requester, url, response.body().string());
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * get同步方法
     *
     * @param url
     * @param params
     * @return
     */
    public String getSynchronize(final String url, final Object... params) {
        HttpRequester requester = null;
        try {
            requester = generalRequester(params);
            GetBuilder builder = OkHttpUtils.get()
                    .url(url);
            addGetParam(requester, builder);
            Response response = builder.build().execute();
            return decryptSynchronize(requester, url, response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 同步请求解密，列表接口使用
     *
     * @param requester
     * @param result
     * @return
     */
    private String decryptSynchronize(HttpRequester requester, String url, String result) {
        if (requester == null) {
            return result;
        }
        if (DESUtil.SECRET_DES.equals(requester.getSecretMode()) && !url.contains("Public/") && !url.contains("public/")) {
            JSONObject object = null;
            try {
                object = new JSONObject(result);
                result = DESUtil.decrypt(object.optString("key"), DESUtil.SECRET_DES_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}