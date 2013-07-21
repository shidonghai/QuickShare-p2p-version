package com.cloudsynch.quickshare.widgets;

import android.widget.GridView;

/**
 * Created by Xiaohu on 13-6-14.
 */
public class ExpandedGridView extends GridView {

    public ExpandedGridView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * none scroll.
     */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

    }

}
