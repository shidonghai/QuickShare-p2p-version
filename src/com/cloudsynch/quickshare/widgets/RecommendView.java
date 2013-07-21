package com.cloudsynch.quickshare.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;

public class RecommendView extends LinearLayout {
	private TextView mTitleView;

	private LinearLayout mContentContainer;
	private ArrayList<LinearLayout> mRowViews = new ArrayList<LinearLayout>();
	private ArrayList<View> mItemViews = new ArrayList<View>();
	private LayoutInflater mInflater;

	private int mColumnCount = 3;
	private int mRowCount = 2;

	private OnClickListener mOnClickListener;

	public RecommendView(Context context) {
		super(context);
		init();
	}

	public RecommendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mInflater = LayoutInflater.from(getContext());

		setOrientation(LinearLayout.VERTICAL);

		View titleContainer = mInflater.inflate(
				R.layout.network_recommend_title, null);

		mTitleView = (TextView) titleContainer.findViewById(R.id.title);

		mContentContainer = new LinearLayout(getContext());
		mContentContainer.setOrientation(LinearLayout.VERTICAL);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 50);
		addView(titleContainer, params);
		LayoutParams contentparams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		addView(mContentContainer, contentparams);

		createRows();
	}

	private void createRows() {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < mRowCount; i++) {
			LinearLayout row = new LinearLayout(getContext());
			row.setOrientation(LinearLayout.HORIZONTAL);
			mRowViews.add(row);
			mContentContainer.addView(row, new LayoutParams(params));
		}
	}

	public void setTitle(int id) {
		mTitleView.setText(id);
	}

	public void setTitle(String title) {
		mTitleView.setText(title);
	}

	public void setRowCount(int count) {
		mRowCount = count;
	}

	public void setColumnCount(int count) {
		mColumnCount = count;
	}

	public void addContent(ArrayList list) {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			int row = i / mColumnCount;
			if (mRowViews.size() > row) {
				LinearLayout mContainer = mRowViews.get(row);
				addContentView(list.get(i), mContainer);
			}
		}
	}

	private void addContentView(Object object, ViewGroup viewGroup) {
		RelativeLayout view = (RelativeLayout) mInflater.inflate(
				R.layout.video_home_view, null);
		RatingView rating = (RatingView) view.findViewById(R.id.rating);
		rating.setMark(8);
		if (mOnClickListener != null) {
			view.setOnClickListener(mOnClickListener);
		}
		LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);

		mItemViews.add(view);
		viewGroup.addView(view, params);
	}

	public void setOnItemClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
		for (View view : mItemViews) {
			view.setOnClickListener(mOnClickListener);
		}
	}
}
