package com.core.framework.pulltorefreshListview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * author:jjj
 * time: 2017/9/19 15:49
 * TODO:
 */

public class XListViewForNS extends XListView {
    public XListViewForNS(Context context) {
        super(context);
    }

    public XListViewForNS(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XListViewForNS(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mExpandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, mExpandSpec);
    }
}
