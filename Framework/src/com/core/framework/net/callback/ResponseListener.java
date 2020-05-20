package com.core.framework.net.callback;

import android.view.View;

/**
 * 网络响应ui处理监听,建议由application实现
 */
public interface ResponseListener {
    /**
     * @param code 授权失败码
     * @param msg  授权消息
     * @return true if the callback consumed the long click, false otherwise
     */
    public boolean onResponseUi(int code, String msg);

    /**
     * 处理网络等待提示view的显示与关闭
     *
     * @param mComeFrom  请求页面对象
     * @param isShow     [true,显示][false,关闭]
     * @param cancelable 是否可手动关闭，isShow为true时有效
     */
    public void dealProgressView(Object mComeFrom, boolean isShow, boolean cancelable);

    /**
     * 处理网络请求失败view的显示与隐藏
     *
     * @param mComeFrom
     * @param isShow    是否显示
     * @param listener  失败view点击事件监听
     */
    public void dealFailedView(Object mComeFrom, boolean isShow, View.OnClickListener listener);

    /**
     * 处理网络请求生命周期
     *
     * @param mComeFrom
     * @param isAdd     是否添加，发起网络请求即添加，网络结束即删除
     * @param tag       网络请求唯一标识
     */
    public void dealRequestTag(Object mComeFrom, boolean isAdd, String tag);
}