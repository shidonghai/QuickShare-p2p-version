package com.cloudsynch.quickshare.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DownloadItemView extends RelativeLayout {
	private int mWidth;
	private int mHeight;

	private RectF mRect;
	private Paint mPaint;

	public DownloadItemView(Context context) {
		super(context);
		init();
	}

	public DownloadItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mRect = new RectF();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.rgb(208, 208, 208));
		mPaint.setStyle(Style.STROKE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = getWidth();
		mHeight = getHeight();
		setRect();

	}

	private void setRect() {
		mRect.set(0, 0, mWidth, mHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRoundRect(mRect, 1, 1, mPaint);
	}
}
