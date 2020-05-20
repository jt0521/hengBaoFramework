package com.core.framework.netLib;

import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Copyright (C), 2020, nqyw
 * FileName: tgl
 * Author: 10496
 * Date: 2020/3/23 21:16
 * Description:
 * History:
 */
public abstract class CallbackLib {

    public void onResponse(java.lang.String response, int id) {

    }

    public abstract void onError(Exception e, int id);

}
