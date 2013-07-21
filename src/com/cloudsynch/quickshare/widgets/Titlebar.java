package com.cloudsynch.quickshare.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.utils.Utils;

/**
 * 
 * A common title bar, we can use it everywhere we need it.
 * 
 * @author KingBright
 * 
 */
public class Titlebar extends RelativeLayout {

	private int DP_LENGTH = 30;

	private ImageView mLeftImage;
	private ImageView mRightImage;
	private TextView mTextView;

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				if (v == mLeftImage) {
					mListener.onLeftClick();
				} else {
					mListener.onRightClick();
				}
			}
		}
	};
	private TitlebarClickListener mListener;

	public Titlebar(Context context) {
		super(context);
		init();
	}

	public Titlebar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.titlebar, this);
		setBackgroundResource(R.drawable.titlebar_bg);

		// left
		mLeftImage = (ImageView) findViewById(R.id.left_image);
		// right
		mRightImage = (ImageView) findViewById(R.id.right_image);
		// middle
		mTextView = (TextView) findViewById(R.id.text);

		mLeftImage.setOnClickListener(mClickListener);
		mRightImage.setOnClickListener(mClickListener);
	}

	public void setLeftImage(int imageId) {
		mLeftImage.setImageResource(imageId);
	}

	public void setRightImage(int imageId) {
		mRightImage.setImageResource(imageId);
	}

	public void setTitle(int stringId) {
		mTextView.setText(stringId);
	}

	public void setLeftVisibility(int visibility) {
		mLeftImage.setVisibility(visibility);
	}

	public void setRightVisibility(int visibility) {
		mRightImage.setVisibility(visibility);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return true;
	}

	public void setTitlebarClickListener(TitlebarClickListener listener) {
		mListener = listener;
	}

	public void startRightAnimation(int animId) {
		if (null == mRightImage) {
			return;
		}

		Animation animation = AnimationUtils
				.loadAnimation(getContext(), animId);
		mRightImage.startAnimation(animation);
	}

	public void clearRightAnimation() {
		if (null == mRightImage) {
			return;
		}

		mRightImage.clearAnimation();
	}

	public static interface TitlebarClickListener {
		public void onLeftClick();

		public void onRightClick();
	}

	public void setTitle(String name) {
		mTextView.setText(name);
	}
}
