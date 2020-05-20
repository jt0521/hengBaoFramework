package com.core.framework.base;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Window;

import com.core.framework.R;
import com.core.framework.netLib.NetLib;
import com.gyf.immersionbar.ImmersionBar;

/**
 * Copyright (C), 2020, nqyw
 * FileName: tgl
 * Author: 10496
 * Date: 2020/4/1 1:03
 * Description:
 * History:
 */
public class BaseThemeActivity extends FragmentActivity {

    protected ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setStatusBarStyle();
    }

    /**
     * 是否可以使用沉浸式
     *
     * @return 默认使用
     */
    public boolean isImmersionBarEnabled() {
        return true;
    }

    /**
     * 设置状态栏背景
     */
    protected void setStatusBarStyle() {
        if (!isImmersionBarEnabled()) {
            return;
        }
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar
                .statusBarColor(getStatusBarColorResId())
                //.statusBarView(R.id.fitSystemView)
                .barColorInt(ContextCompat.getColor(this, getStatusBarColorResId()))
                .navigationBarColor(R.color.black);
        if (getStatusBarColorResId() == R.color.status_bar_white) {
            mImmersionBar.statusBarDarkFont(true, 0.2f);
        }
        mImmersionBar.init();
    }

    /**
     * 设置状态栏背景色资源id
     *
     * @return
     */
    protected @ColorRes
    int getStatusBarColorResId() {
        return R.color.main_color;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetLib.cancelTag(this);
    }
}
