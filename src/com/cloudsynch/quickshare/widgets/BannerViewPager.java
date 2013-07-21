package com.cloudsynch.quickshare.widgets;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cloudsynch.quickshare.R;

public class BannerViewPager extends RelativeLayout {
	private final int mItemSize = 5;
	private ViewPager mViewPager;
	private BannerPagerAdapter mAdapter;
	private LinearLayout mIndicator;
	private int mSize;

	public BannerViewPager(Context context) {
		super(context);
		initView();
	}

	public BannerViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {

		mViewPager = new ViewPager(getContext());
		LayoutParams pagerParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mAdapter = new BannerPagerAdapter(getContext());
		mAdapter.setSize(mItemSize);
		mViewPager.setAdapter(mAdapter);
		addView(mViewPager, pagerParams);

		mIndicator = new LinearLayout(getContext());
		LayoutParams indicatorParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		indicatorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		addView(mIndicator, indicatorParams);
	}

	public void setBannerResources(ArrayList list) {

	}

	private void addIndicators() {
		for (int i = 0; i < mSize; i++) {

		}
	}

	public void setSize(int size) {
		mSize = size;
		mAdapter.setSize(size);
	}

	class BannerPagerAdapter extends PagerAdapter {
		private LinkedList<ImageView> mViews = new LinkedList<ImageView>();
		private Context mCtx;
		private int mSize;

		public BannerPagerAdapter(Context context) {
			mCtx = context;
		}

		public void setSize(int size) {
			mSize = size;

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView view;

			if (mViews.size() > position) {
				view = mViews.get(position);
			} else {
				view = new ImageView(mCtx);
				mViews.add(view);
			}
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(params);
			view.setScaleType(ScaleType.FIT_XY);
			view.setImageResource(R.drawable.iron_man_1);
			container.addView(view, params);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (mViews.size() > position) {
				View view = mViews.get(position);
				container.removeView(view);
			}
		}

		public void setData(ArrayList data) {
			setSize(data.size());
		}
	}

}
