package com.cloudsynch.quickshare;

import android.app.Application;

import com.cloudsynch.quickshare.download.DownloadManager;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Xiaohu on 13-6-3.
 */
public class QuickShareApplication extends Application {

	private ImageWorker mImageWorker;

	private static Application mApp;

	public static Application getApplication() {
		return mApp;
	}

	public static void destroy() {
		mApp = null;
	}

	public ImageWorker getImageWorker() {
		return mImageWorker;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mImageWorker = new ImageWorker(this);
		MobclickAgent.setDebugMode(true);
		MyInfo.getInstance(getApplicationContext());
		FileUtil.initSavePath();
		DownloadManager.getInstance(this).init();
		EventManager.getInstance().init(this);

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}

	public void resume() {
		mApp = this;
	}

}
