package com.core.framework.netLib;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Copyright (C), 2020, nqyw
 * FileName: tgl
 * Author: 10496
 * Date: 2020/3/23 21:31
 * Description:
 * History:
 */
public class NetLib {

    public static void getLib(String url, final CallbackLib callbackLib) {
        getLib(url, null, callbackLib);
    }

    public static void getLib(String url, HashMap<String, String> hashMap, final CallbackLib callbackLib) {
        GetBuilder builder = OkHttpUtils.get()
                .url(url);
        if (hashMap != null) {
            builder.params(hashMap);
        }
        builder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                if (callbackLib != null) {
                    callbackLib.onError(e, id);
                }
            }

            @Override
            public void onResponse(String response, int id) {
                if (callbackLib != null) {
                    callbackLib.onResponse(response, id);
                }
            }
        });
    }

    /**
     * 下载文件
     *
     * @param url
     * @param tag
     * @param fileCallBackLib
     */
    public static void getFile(String url, Object tag, final FileCallBackLib fileCallBackLib) {
        OkHttpUtils.get()
                .url(url)
                .tag(tag)
                .build().execute(new FileCallBack(fileCallBackLib.destFileDir, fileCallBackLib.destFileName) {
            @Override
            public void onError(Call call, Exception e, int id) {
                fileCallBackLib.onError(e, id);
            }

            @Override
            public void onResponse(File response, int id) {
                fileCallBackLib.onResponse(response, id);
            }

            @Override
            public void onAfter(int id) {
                super.onAfter(id);
                fileCallBackLib.onAfter(id);
            }
        });
    }

    public static void cancelTag(Object tag) {
        OkHttpUtils.getInstance().cancelTag(tag);
    }

}
