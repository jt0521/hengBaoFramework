package com.core.framework.netLib;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.core.framework.BuildConfig;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class NetLog {
    private final static Charset UTF8 = Charset.forName("UTF-8");


    /**
     * 接口错误时处理日志
     *
     * @param call
     * @param code
     * @see #loggerOnFailure(Call, int, String, StackTraceElement[], boolean)
     */
    public static void onFailure(Call call, int code, Throwable t) {
        boolean connectEx = t instanceof ConnectException || t instanceof SocketTimeoutException;
        loggerOnFailure(call, code, t.getMessage(), t.getStackTrace(), connectEx);
    }

    /**
     * 接口错误时处理日志
     *
     * @param call
     * @param code
     * @param msg
     */
    private static void loggerOnFailure(Call call, int code, String msg,
                                        StackTraceElement[] exStackTrace,
                                        boolean connectEx) {
        try {
            Request request = call != null ? call.request() : null;
            if (request == null) {
                request = getRequest(call);
            }

            StringBuilder sb = new StringBuilder();
            String errorMsg = "--> code: " + code + ",  msg: " + msg;
            if (request != null) {
                String requestInfo = "==> " + request.method() + " " + request.url() + " \n";
                String requestBody = readContent(request);
                if (requestBody == null) {
                    requestBody = getContent(request);
                }
                requestBody = "--> requestBody: " + requestBody + " \n";
                sb.append(requestInfo);
                sb.append(requestBody);
            }
            sb.append(errorMsg);

            handleLog(sb, exStackTrace);
            sb = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("net.framework", "CallRequestHelper :" + e.getMessage());
        }
    }

    /**
     * 获取请求(反射)
     *
     * @param call
     * @return
     */
    private static Request getRequest(Call call) {
        if (call == null) {
            return null;
        }

        Field[] fields = call.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return null;
        }

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                if (TextUtils.equals("delegate", name)) {
                    Call delegateCall = (Call) field.get(call);
                    return getRequest(delegateCall);
                } else if (TextUtils.equals("rawCall", name)) {
                    okhttp3.Call rawCall = (okhttp3.Call) field.get(call);
                    return rawCall.request();
                }
            }
        } catch (IllegalAccessException e) {

        }

        return null;
    }

    public static String log(Response response) {
        if (BuildConfig.DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("==> " + response.request().method());
            builder.append("   ");
            builder.append(response.request().url().toString());
            builder.append("\n");

            String requestBody = readContent(response.request());
            if (requestBody == null) {
                requestBody = getContent(response.request());
            }
            builder.append("--> requestBody: " + requestBody);

            String result = null;
            try {
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.append("\n");
            builder.append("--> " + result);
            handleLog(builder, null);
            return result;
        }
        try {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 处理日志
     *
     * @param sb
     */
    public final static void handleLog(StringBuilder sb, StackTraceElement[] exStackTrace) {
        StackTraceElement[] callSackTrace = Thread.currentThread().getStackTrace();
//        String lineInfo = StackTraceUtil.getStackMsg(callSackTrace, NetLog.class,
//                1, 0, exStackTrace);

        long millis = System.currentTimeMillis();
        Date date = new Date();
        date.setTime(millis);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date) + ", ts:" + millis + "\n";
//        sb.insert(0, lineInfo);
        sb.insert(0, time);
        sb.append("\n>>>\n>>>");
        String logBody = sb.toString();

        if (BuildConfig.DEBUG) {
            Log.e("net.framework", logBody);
        } else {
            //TODO

        }
    }

    /**
     * 获取请求参数(反射)
     *
     * @param request
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private static String getContent(Request request) {
        RequestBody requestBody = null;
        if (request == null || (requestBody = request.body()) == null) {
            return "";
        }

        Field[] fields = requestBody.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return null;
        }

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                if (TextUtils.equals("val$content", name)) {
                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }

                    if (isMultipart(contentType)) {
                        return "upload file content ...";
                    }

                    byte[] bytes = (byte[]) field.get(requestBody);
                    return new String(bytes, charset);
                }
            }
        } catch (IllegalAccessException e) {

        }

        return null;
    }

    private static String readContent(Request request) {
        if (request == null || (request.body()) == null) {
            return null;
        }
        return readContent(request.body());
    }

    /**
     * 从流中读取请求参数
     *
     * @param requestBody
     * @return
     */
    public static String readContent(RequestBody requestBody) {
        Buffer buffer = null;
        try {
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (isMultipart(contentType)) {
                return "upload file content ...";
            }

            buffer = new Buffer();
            requestBody.writeTo(buffer);
            if (isPlaintext(buffer)) {
                String content = buffer.readString(charset);
                return content;
            }
        } catch (Exception e) {

        } finally {
            if (buffer != null) {
                buffer.clear();
            }
            try {
                buffer.close();
            } catch (Throwable e) {
            }

        }

        return null;
    }

    /**
     * 是否是文件上传
     *
     * @param contentType
     * @return
     */
    private static boolean isMultipart(MediaType contentType) {
        String contentTypeStr = contentType != null ? contentType.toString() : "";
        if (!TextUtils.isEmpty(contentTypeStr)) {
            if (contentTypeStr.toLowerCase().contains("multipart/form-body")) {
                return true;
            }
        }

        return false;
    }

    /***
     *
     * @param buffer
     * @return
     */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
