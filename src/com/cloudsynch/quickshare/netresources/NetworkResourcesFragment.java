package com.cloudsynch.quickshare.netresources;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.BannerViewPager;
import com.cloudsynch.quickshare.widgets.SelectView;
import com.cloudsynch.quickshare.widgets.SelectView.OnItemSelectListener;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;

public class NetworkResourcesFragment extends BaseFragment implements
		OnItemSelectListener, TitlebarClickListener {

	private BannerViewPager mViewPager;
	private SelectView mSelectView;
	private Titlebar mTitlebar;
	private LinearLayout mRecommendContainer;
	private View mHomepage;
	private int mSelected = 0;

	private ArrayList<AlbumListView> mViews = new ArrayList<AlbumListView>();

	@Override
	public View createView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mViewGroup = inflater.inflate(R.layout.networkresource_fragment,
				null);

		mViewPager = (BannerViewPager) mViewGroup.findViewById(R.id.banner);
		mTitlebar = (Titlebar) mViewGroup.findViewById(R.id.title);
		mSelectView = (SelectView) mViewGroup.findViewById(R.id.selectview);
		mRecommendContainer = (LinearLayout) mViewGroup
				.findViewById(R.id.recommendContainer);
		mHomepage = mViewGroup.findViewById(R.id.homepage);

		mViews.add(((AlbumListView) mViewGroup.findViewById(R.id.movies))
				.setType(Networks.Type.MOVIE));
		mViews.add(((AlbumListView) mViewGroup.findViewById(R.id.tvs))
				.setType(Networks.Type.TVPLAY));
		mViews.add(((AlbumListView) mViewGroup.findViewById(R.id.cartoons))
				.setType(Networks.Type.CARTOON));
		mViews.add(((AlbumListView) mViewGroup.findViewById(R.id.news))
				.setType(Networks.Type.NEW));

		setup();
		onCreated();
		return mViewGroup;
	}

	private void onCreated() {
		select(mSelected);
	}

	private void setup() {
		addIgnoreView(mViewPager);

		// setup titlebar
		mTitlebar.setTitle(R.string.network_resources);
		mTitlebar.setLeftImage(R.drawable.list_icon);
		mTitlebar.setRightImage(R.drawable.refresh_icon);
		mTitlebar.setTitlebarClickListener(this);
		mTitlebar.setTitlebarClickListener(new TitlebarClickListener() {
			@Override
			public void onRightClick() {
				refresh();
			}

			@Override
			public void onLeftClick() {
				showMenu();
			}
		});

		// setup select view
		String[] titles = getResources().getStringArray(R.array.select_items);
		mSelectView.addSelectItems(titles, 0);
		mSelectView.setOnItemClickListener(this);

		// setup recommend view
		// for (String title : titles) {
		// RecommendView view = new RecommendView(getActivity());
		// ArrayList list = new ArrayList();
		// for (int j = 0; j < 6; j++) {
		// list.add(new Object());
		// }
		// view.setTitle(title);
		// view.addContent(list);
		// mRecommendContainer.addView(view);
		// view.setOnItemClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// gotoDetail();
		// }
		// });
		// }
	}

	protected void refresh() {
		((AlbumListView) mViews.get(mSelected)).refresh();
	}

	private void select(int index) {
		AlbumListView view = null;

		int count = mViews.size();
		for (int i = 0; i < count; i++) {
			view = mViews.get(i);
			if (i == mSelected) {
				view.setVisibility(View.VISIBLE);
				view.onSelect();
			} else {
				view.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onItemSelect(int index) {
		mSelected = index;
		select(mSelected);
	}

	@Override
	public void onLeftClick() {
		getActivity().finish();
	}

	@Override
	public void onRightClick() {
		// TODO
	}
}
