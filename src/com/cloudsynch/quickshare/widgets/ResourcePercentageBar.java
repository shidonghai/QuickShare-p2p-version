/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudsynch.quickshare.widgets;


import java.util.Collection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cloudsynch.quickshare.R;

/**
 *
 */
public class ResourcePercentageBar extends View {
    private final Paint mEmptyPaint = new Paint();

    private Collection<Entry> mEntries;

    private int mMinTickWidth = 1;

    private long mMax;

    public static class Entry {
        public final float percentage;
        public final Paint paint;

        protected Entry(float percentage, Paint paint) {
            this.percentage = percentage;
            this.paint = paint;
        }
    }

    public ResourcePercentageBar(Context context, AttributeSet attrs) {
        super(context, attrs);

//        setBackgroundResource(R.drawable.resource_progress_bg);
        mEmptyPaint.setColor(getResources().getColor(R.color.resource_empty_paint));
        mEmptyPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        final int top = getPaddingTop();
        final int bottom = getHeight() - getPaddingBottom();
        RectF rect = new RectF();
        rect.left = left;
        rect.right = right;
        rect.top = top;
        rect.bottom = bottom;
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.WHITE);
//        canvas.drawRoundRect(rect, 20, 20, paint);
//        Path path = new Path();
//        path.addRoundRect(rect, 20, 20, Path.Direction.CW);
//        canvas.clipPath(path);

        final int width = right - left;


        float lastX = left;
        if (mEntries != null) {
            for (final Entry e : mEntries) {
                final float entryWidth;
                if (e.percentage == 0.0f) {
                    entryWidth = 0.0f;
                } else {
                    entryWidth = Math.max(mMinTickWidth, width * e.percentage);
                }

                final float nextX = lastX + entryWidth;
                if (nextX > right) {
                    canvas.drawRect(lastX, top, right, bottom, e.paint);
                    return;
                }

                canvas.drawRect(lastX, top, nextX, bottom, e.paint);
                lastX = nextX;
            }
        }

        canvas.drawRect(lastX, top, right, bottom, mEmptyPaint);

    }

    /**
     * Sets the background for this chart. Callers are responsible for later
     * calling {@link #invalidate()}.
     */
    @Override
    public void setBackgroundColor(int color) {
        mEmptyPaint.setColor(color);
    }

    /**
     * Adds a new slice to the percentage bar chart. Callers are responsible for
     * later calling {@link #invalidate()}.
     *
     * @param percentage the total width that
     * @param color      the color to draw the entry
     */
    public static Entry createEntry(float percentage, int color) {
        final Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);

        return new Entry(percentage, p);
    }

    public void setEntries(Collection<Entry> entries) {
        mEntries = entries;
    }

    public void setMax(long max) {
        mMax = max;
    }
}
