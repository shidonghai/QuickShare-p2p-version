package com.cloudsynch.quickshare.netresources;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.StorageManager;
import com.cloudsynch.quickshare.utils.Utils;
import com.cloudsynch.quickshare.utils.thumbnail.ImageResizer;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;
import com.cloudsynch.quickshare.widgets.RatingView;

public class AlbumListView extends RelativeLayout implements
		NetworkTask.Callback<Album>, OnScrollListener, OnClickListener {
	private static final int REQUEST_COUNT = 20;

	private final int REQUEST_WIDTH = Utils.dip2px(getContext(), 80);
	private final int REQUEST_HEIGHT = Utils.dip2px(getContext(), 120);

	private boolean mGetting = false;
	private AlbumAdapter mAdapter;
	private NetworkTask<Album> mNetworkTask;
	private String mType;

	private ImageWorker mImageWorker;

	private View mFootView;

	private Bitmap mDefault;

	private ListView mListView;
	private View mErrorView;
	private View mLoadingView;

	private ImageWorker.LoadMethod mLoadMethod = new ImageWorker.LoadMethod() {
		@Override
		public Bitmap processBitmap(Object obj, Context context) {
			if (obj == null || !(obj instanceof String)) {
				return null;
			}
			File image = Utils.fetchImageByUrl((String) obj,
					StorageManager.CACHE_STORE_PATH);
			if (image == null) {
				return null;
			}
			return ImageResizer.decodeSampledBitmapFromFile(image.getPath(),
					REQUEST_WIDTH, REQUEST_HEIGHT);
		}
	};

	private OnScrollListener mWorkerScrollListener;

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view,
				int position, long id) {
			Context context = getContext();
			// Goto detail
			Album album = mAdapter.getItem(position);
			Intent intent = new Intent();
			intent.putExtra("data", album);
			intent.setClass(context, VideoDetailActivity.class);
			context.startActivity(intent);
		}
	};

	public AlbumListView(Context context) {
		super(context);
		init(context);
	}

	public AlbumListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(getContext()).inflate(R.layout.album_list_view,
				this);
		mListView = (ListView) findViewById(R.id.listview);
		mErrorView = findViewById(R.id.error_view);
		mLoadingView = findViewById(R.id.loading_view);

		mErrorView.setOnClickListener(this);

		// init footView
		if (mFootView == null) {
			mFootView = LayoutInflater.from(getContext()).inflate(
					R.layout.loading_more_view, null);
			mListView.addFooterView(mFootView);
			mFootView.setVisibility(View.GONE);
		}

		// Network task
		mNetworkTask = new NetworkTask<Album>();
		mNetworkTask.setCallback(this);
		mNetworkTask.setParser(new AlbumParser());

		// adapter
		mAdapter = new AlbumAdapter(context);
		mListView.setAdapter(mAdapter);

		// scroll listener
		mListView.setOnScrollListener(this);

		mImageWorker = ((QuickShareApplication) getContext()
				.getApplicationContext()).getImageWorker();
		mWorkerScrollListener = mImageWorker.getScrollerListener();

		mListView.setOnItemClickListener(mOnItemClickListener);

		mDefault = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.video_bg_01)).getBitmap();

		QuickShareApplication app = (QuickShareApplication) context
				.getApplicationContext();
		mImageWorker = app.getImageWorker();
	}

	public AlbumListView setType(String type) {
		mType = type;
		return this;
	}

	@Override
	public void onSuccess(Result<Album> result) {
		mGetting = false;
		if (result.album.size() != 0) {
			addContent(result);
		}

		if (mAdapter.getCount() > 0) {
			mLoadingView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		} else {
			mLoadingView.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mErrorView.setVisibility(View.VISIBLE);
		}
		dismissFootView();
	}

	@Override
	public void onFail(Throwable error, String content) {
		mGetting = false;
		dismissFootView();
		if (mAdapter.getCount() == 0) {
			mLoadingView.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mErrorView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount >= totalItemCount && !mGetting) {
			if (!mAdapter.isEmpty()) {
				mGetting = true;
				showFootView();
				load();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mWorkerScrollListener.onScrollStateChanged(view, scrollState);
	}

	public void addContent(Result<Album> album) {
		mAdapter.add(album);
	}

	public void onSelect() {
		String event = EventConstant.NET_SOURCE_TYPES.get(mType);
		EventManager.getInstance().onEvent(event);

		if (mAdapter.isEmpty()) {
			if (mType == null) {
				throw new NullPointerException("type must be set");
			}
			mGetting = true;
			load();
		}
	}

	private class AlbumAdapter extends BaseAdapter {
		private Result<Album> mAlbums;
		private LayoutInflater mInflater;

		public AlbumAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mAlbums = new Result<Album>();
		}

		public boolean isEmpty() {
			return mAlbums.album.size() == 0;
		}

		public void add(Result<Album> album) {
			mAlbums.addAll(album);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mAlbums.album.size() == 0 ? 0 : mAlbums.album.size();
		}

		@Override
		public Album getItem(int position) {
			return (Album) mAlbums.album.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null || convertView.getTag() == null) {
				holder = new Holder();
				convertView = mInflater.inflate(R.layout.video_view, null);
				holder.init(convertView);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.display(getItem(position));
			return convertView;
		}

		class Holder {
			ImageView icon;
			TextView title;
			RatingView rating;
			TextView year;
			TextView area;
			TextView type;

			public void init(View view) {
				icon = (ImageView) view.findViewById(R.id.icon);
				title = (TextView) view.findViewById(R.id.title);
				rating = (RatingView) view.findViewById(R.id.rating);
				year = (TextView) view.findViewById(R.id.year);
				area = (TextView) view.findViewById(R.id.area);
				type = (TextView) view.findViewById(R.id.type);
			}

			public void display(Album album) {
				// TODO display icon
				mImageWorker.loadImage(album.logo, icon, mDefault, mLoadMethod);
				title.setText(album.name);
				if (album.score != null
						&& !TextUtils.isEmpty(album.score.trim())) {
					rating.setMark(Float.parseFloat(album.score));
				}
				year.setText(album.year);
				area.setText(album.area);
				type.setText(album.type);
			}
		}

		public void clear() {
			mAlbums.album.clear();
			notifyDataSetChanged();
		}
	}

	private void showFootView() {
		mFootView.setVisibility(View.VISIBLE);
	}

	private void dismissFootView() {
		mFootView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		if (v == mErrorView) {
			mErrorView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
			load();
		}
	}

	private void load() {
		mNetworkTask.request(Networks.UrlBuilder.build(mType,
				mAdapter.getCount(), REQUEST_COUNT));
	}

	public void refresh() {
		mErrorView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);
		mAdapter.clear();
		load();
	}
}
