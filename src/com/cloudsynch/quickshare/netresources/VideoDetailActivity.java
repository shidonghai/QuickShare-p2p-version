package com.cloudsynch.quickshare.netresources;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.download.DownloadManager;
import com.cloudsynch.quickshare.ui.DownloadAlertDialog;
import com.cloudsynch.quickshare.utils.*;
import com.cloudsynch.quickshare.utils.thumbnail.ImageResizer;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;
import com.cloudsynch.quickshare.widgets.RatingView;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;

public class VideoDetailActivity extends FragmentActivity implements
		OnClickListener, OnItemClickListener {
	private static final String TAG = VideoDetailActivity.class.getName();

	private RatingView mRatingView;

	private Album mAlbum;

	private ImageWorker mImageWorker;

	private View mDescriptionSelectView;
	private View mDownloadSelectView;

	private View mIntroView;
	private View mDownloadView;
	private GridView mDownloadGridView;
	private GridView mDownloadGridViewHd;
	private DownloadAdapter mDownloadAdapter;
	private DownloadAdapter mDownloadAdapterHd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_detail_layout);

		mAlbum = (Album) getIntent().getSerializableExtra("data");
		mImageWorker = ((QuickShareApplication) getApplication())
				.getImageWorker();

		setupView();

		display();
	}

	private void setupView() {
		Titlebar titlebar = (Titlebar) findViewById(R.id.titleview);
		titlebar.setTitle(mAlbum.name);
		titlebar.setLeftImage(R.drawable.return_button);
		titlebar.setRightImage(R.drawable.refresh_icon);
		titlebar.setTitlebarClickListener(new TitlebarClickListener() {
			@Override
			public void onRightClick() {
			}

			@Override
			public void onLeftClick() {
				VideoDetailActivity.this.finish();
			}
		});
		mRatingView = (RatingView) findViewById(R.id.rating);

		// select view
		mDescriptionSelectView = findViewById(R.id.description_select);
		mDownloadSelectView = findViewById(R.id.download_select);
		mDescriptionSelectView.setSelected(true);
		ViewSettings.setVisibility(mDescriptionSelectView, R.id.bottom_bar,
				View.VISIBLE);
		ViewSettings.setVisibility(mDownloadSelectView, R.id.bottom_bar,
				View.GONE);

		mDescriptionSelectView.setOnClickListener(this);
		mDownloadSelectView.setOnClickListener(this);

		mIntroView = findViewById(R.id.intro_view);
		mDownloadView = findViewById(R.id.download_view);

		mDownloadGridView = (GridView) findViewById(R.id.grid_view);
		mDownloadGridViewHd = (GridView) findViewById(R.id.grid_view_hd);

		mDownloadAdapter = new DownloadAdapter(this, mAlbum.download);
		mDownloadGridView.setAdapter(mDownloadAdapter);
		mDownloadGridView.setOnItemClickListener(this);

		mDownloadAdapterHd = new DownloadAdapter(this, mAlbum.downloadHd);
		mDownloadGridViewHd.setAdapter(mDownloadAdapterHd);
		mDownloadGridViewHd.setOnItemClickListener(this);

		LogUtil.e("downloads", mAlbum.download.size() + "");
	}

	private void display() {
		Bitmap defaultBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.video_bg_01)).getBitmap();
		mImageWorker.loadImage(mAlbum.logo,
				(ImageView) findViewById(R.id.icon), defaultBitmap,
				new ImageWorker.LoadMethod() {
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
						return ImageResizer.decodeSampledBitmapFromFile(
								image.getPath(),
								Utils.dip2px(getApplicationContext(), 80),
								Utils.dip2px(getApplicationContext(), 120));
					}
				});

		ViewSettings.setText(this, R.id.title, mAlbum.name);
		mRatingView.setMark(Float.parseFloat(mAlbum.score));

		ViewSettings.setText(this, R.id.area,
				getString(R.string.area, mAlbum.area));
		ViewSettings.setText(this, R.id.type,
				getString(R.string.type, mAlbum.type));
		ViewSettings.setText(this, R.id.format,
				getString(R.string.format, mAlbum.format));
		ViewSettings.setText(this, R.id.resolution, R.string.resolution);

		ViewSettings.setText(mIntroView, R.id.year,
				getString(R.string.year, mAlbum.year));
		ViewSettings.setText(mIntroView, R.id.director,
				getString(R.string.director, mAlbum.director));
		ViewSettings.setText(mIntroView, R.id.actor,
				getString(R.string.actor, mAlbum.actor));
		ViewSettings.setText(mIntroView, R.id.language,
				getString(R.string.language, mAlbum.language));
		ViewSettings.setText(mIntroView, R.id.intro_text, mAlbum.intro);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.description_select: {
			mDescriptionSelectView.setSelected(true);
			mDownloadSelectView.setSelected(false);
			ViewSettings.setVisibility(mDescriptionSelectView, R.id.bottom_bar,
					View.VISIBLE);
			ViewSettings.setVisibility(mDownloadSelectView, R.id.bottom_bar,
					View.GONE);
			showIntroduction();
			break;
		}
		case R.id.download_select: {
			mDescriptionSelectView.setSelected(false);
			mDownloadSelectView.setSelected(true);
			ViewSettings.setVisibility(mDescriptionSelectView, R.id.bottom_bar,
					View.GONE);
			ViewSettings.setVisibility(mDownloadSelectView, R.id.bottom_bar,
					View.VISIBLE);
			showDownloads();
			break;
		}
		}
	}

	private void showDownloads() {
		mIntroView.setVisibility(View.GONE);
		mDownloadView.setVisibility(View.VISIBLE);
	}

	private void showIntroduction() {
		mIntroView.setVisibility(View.VISIBLE);
		mDownloadView.setVisibility(View.GONE);
	}

	private class DownloadAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<Download> mList;

		public DownloadAdapter(Context context, ArrayList<Download> downloadList) {
			mInflater = LayoutInflater.from(context);
			mList = downloadList;
		}

		@Override
		public int getCount() {
			return mList == null ? 0 : mList.size();
		}

		@Override
		public Download getItem(int position) {
			return mList == null ? null : mList.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup group) {
			view = mInflater.inflate(R.layout.download_item, null);

			Download download = getItem(position);
			if (download != null) {
				LogUtil.e("getView", download.download_name);
				((TextView) view.findViewById(R.id.name))
						.setText(download.download_name);
				view.setTag(download);
			}
			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		EventManager.getInstance().onEvent(EventConstant.NET_SOURCE_DOWNLOAD);

		DownloadManager dm = null;
		try {
			dm = DownloadManager.getInstance(this);
			Download download = (Download) mDownloadAdapter.getItem(position);

			if (download == null) {
				return;
			}

			if (!dm.checkShouLeiEnable()) {
				showDownloadDialog(dm);
				return;
			}

			dm.createDownloadTask(download.download_url,
					download.download_name, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showDownloadDialog(final DownloadManager dm) {
		DownloadAlertDialog dialog = DownloadAlertDialog.newInstance(dm
				.getErrorMsg());
		dialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dm.needDownLoadXunLei()) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(DownloadManager.XUN_LEI_URL));
					startActivity(intent);
				}
			}
		});
		dialog.show(getSupportFragmentManager(), dialog, "download");
	}
}
