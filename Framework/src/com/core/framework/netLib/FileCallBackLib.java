package com.core.framework.netLib;

import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import okhttp3.Call;

/**
 * Copyright (C), 2020, nqyw
 * FileName: tgl
 * Author: 10496
 * Date: 2020/3/23 21:25
 * Description:
 * History:
 */
public abstract class FileCallBackLib {
    /**
     * 目标文件存储的文件夹路径
     */
    public String destFileDir;
    /**
     * 目标文件存储的文件名
     */
    public String destFileName;


    public FileCallBackLib(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    public abstract void onError(Exception e, int id);

    /**
     * UI Thread
     *
     * @param
     */
    public void onAfter(int id) {
    }

    public abstract void onResponse(File response, int id);
}
