package com.cloudsynch.quickshare.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.utils.ViewSettings;

public class SelectView extends LinearLayout {
	private ArrayList<View> mItemViews;
	private OnItemSelectListener mListener;
	private OnClickListener mViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int index = mItemViews.indexOf(v);
				setSelect(index);
				if (mListener != null) {
					mListener.onItemSelect(index);
				}
			}
		}
	};

	public SelectView(Context context) {
		super(context);
	}

	public SelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void addSelectItems(String[] items, int bgId) {
		mItemViews = new ArrayList<View>();
		int size = items.length;
		for (int i = 0; i < size; i++) {
			View view = generateView(items[i], 0);
			LayoutParams params = new LayoutParams(0,
					LayoutParams.MATCH_PARENT, items[i].length());

			if (i == 0) {
				ViewSettings.setVisibility(view, R.id.bottom, View.VISIBLE);
			}
			addView(view, params);
			mItemViews.add(view);

			if (i == size - 1) {
				// TODO
				// ViewSettings.setVisibility(view, R.id.more, View.VISIBLE);
			} else {
				addView(gerateSeparate());
			}
			view.setOnClickListener(mViewClickListener);
		}
		setSelect(0);
	}

	private View gerateSeparate() {
		ImageView view = new ImageView(getContext());
		view.setImageResource(R.drawable.select_separate_bar);
		LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		param.gravity = Gravity.CENTER_VERTICAL;
		view.setLayoutParams(param);
		return view;
	}

	private void setSelect(int index) {
		int size = mItemViews.size();
		for (int i = 0; i < size; i++) {
			View view = mItemViews.get(i);
			if (index != i) {
				view.setSelected(false);
				ViewSettings.setVisibility(view, R.id.bottom, View.GONE);
			} else {
				view.setSelected(true);
				ViewSettings.setVisibility(view, R.id.bottom, View.VISIBLE);
			}
		}
	}

	private View generateView(String title, int bgId) {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		View view = inflater.inflate(R.layout.select_view_item, null);

		ViewSettings.setText(view, R.id.text, title);

		return view;
	}

	public void setOnItemClickListener(OnItemSelectListener listener) {
		mListener = listener;
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
	}

	public static interface OnItemSelectListener {
		public void onItemSelect(int index);
	}
}
