package com.core.framework.net;

import java.io.Serializable;

public class ResultBean<T> implements Serializable {
    public int code;
    public T data;
    public String msg;

    public boolean isCodeSuccess() {
        return code == 200;
    }
}
