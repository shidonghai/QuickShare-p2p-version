package com.cloudsynch.quickshare.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;

public class RatingView extends LinearLayout {
	private TextView mTextView;
	private ArrayList<ImageView> mImageViews;

	public RatingView(Context context) {
		super(context);
		init();
	}

	public RatingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mImageViews = new ArrayList<ImageView>();

		setOrientation(LinearLayout.HORIZONTAL);
		for (int i = 0; i < 5; i++) {
			ImageView imageView = new ImageView(getContext());
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_VERTICAL;
			params.setMargins(1, 1, 1, 1);
			imageView.setImageResource(R.drawable.grey_star);
			addView(imageView, params);
			mImageViews.add(imageView);
		}
		mTextView = new TextView(getContext());
		mTextView.setTextColor(getContext().getResources().getColor(
				R.color.black));
		mTextView.setTextSize(14);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		addView(mTextView, params);
	}

	public void setMark(float mark) {
		for (ImageView image : mImageViews) {
			image.setImageResource(R.drawable.grey_star);
		}
		// TODO
		int count = Math.round(mark / 2);

		for (int i = 0; i < count; i++) {
			mImageViews.get(i).setImageResource(R.drawable.blue_star);
		}

		setText((int) mark + "");
	}

	public void setText(String rate) {
		mTextView.setText(rate + getContext().getString(R.string.mark));
	}
}
