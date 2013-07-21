package com.cloudsynch.quickshare.download;

import android.content.Context;

import com.cloudsynch.quickshare.utils.LogUtil;
import java.util.HashMap;
import java.util.Map;

import com.cloudsynch.quickshare.R;
import com.umeng.common.Log;
import com.xunlei.downloadprovider.thirdpart.DownloadConstant;
import com.xunlei.downloadprovider.thirdpart.IXLDownload;
import com.xunlei.downloadprovider.thirdpart.XLDownloadClient;
import com.xunlei.downloadprovider.thirdpart.callback.OnInitListener;

public class DownloadManager {
	// 授权id
	private static final String CERTIFICATION_ID = "9ee7c951a7ba293e509df8ba0ce9ef04-20130627-145515-02-0001";
	public static final String XUN_LEI_URL = "http://down.sandai.net/mobile/XLDownloadProvider/MobileThunder_Android_1.9.2.1336_XLWXguanwang.apk";
	private static final String PACKAGE_NAME = "com.cloudsynch.quickshare";
	private static final String APP_NAME = "quickshare";
	private static DownloadManager mDownloadManager;
	private XLDownloadClient mXLDClient;
	private IXLDownload mDownload;
	private Context mContext;
	private static final int Error_Code_UNKNOW = -1;
	private static final int ERROR_CODE_NOT_INSTALL = -2;
	private static final int ERROR_CODE_NOT_SUPPORT = -3;
	private int mErrorCode = Error_Code_UNKNOW;

	private static Map<Integer, Integer> mErrorMsg;
	static {
		mErrorMsg = new HashMap<Integer, Integer>();
		mErrorMsg.put(Error_Code_UNKNOW, R.string.dm_error_download_failed);
		mErrorMsg
				.put(DownloadConstant.DownloadError.DOWNLOAD_ERROR_CERTIFICATE_OUTDATE,
						R.string.dm_error_download_failed);
		mErrorMsg
				.put(DownloadConstant.DownloadError.DOWNLOAD_ERROR_CERTIFICATE_OUTDATE,
						R.string.dm_error_download_failed);
		mErrorMsg.put(ERROR_CODE_NOT_INSTALL, R.string.dm_error_not_install);
		mErrorMsg.put(ERROR_CODE_NOT_SUPPORT, R.string.dm_error_not_support);
	}

	private DownloadManager(Context c) {
		mContext = c;
		mXLDClient = new XLDownloadClient(c);
	}

	public static synchronized DownloadManager getInstance(Context c) {
		if (mDownloadManager == null) {
			mDownloadManager = new DownloadManager(c);
		}
		return mDownloadManager;
	}

	public void init() {
		mXLDClient.init(CERTIFICATION_ID, PACKAGE_NAME, APP_NAME,
				new OnInitListener() {

					@Override
					public void onInitCompleted(int code, IXLDownload download) {
						if (DownloadConstant.DownloadError.DOWNLOAD_ERROR_CERTIFICATE_OUTDATE == code) {
							LogUtil.e("init downloader",
									"error:certificate outdate");
							mErrorCode = code;
						} else if (DownloadConstant.DownloadError.DOWNLOAD_ERROR_WRONG_CERTIFICATE == code) {
							LogUtil.e("init downloader",
									"error:wrong certificate");
							mErrorCode = code;
						} else {
							mDownload = download;
						}
					}
				});
	}

	public boolean needDownLoadXunLei() {
		return mErrorCode == ERROR_CODE_NOT_SUPPORT
				|| mErrorCode == ERROR_CODE_NOT_INSTALL;
	}

	/**
	 * 获得错误信息
	 * 
	 * @return
	 */
	public Integer getErrorMsg() {
		return mErrorMsg.get(mErrorCode);
	}

	public boolean isAppInstalled() {
		if (mDownload == null) {
			return false;
		}
		return mDownload.isAppInstalled();
	}

	public long getInstalledVersionCode() {
		if (mDownload != null) {
			return mDownload.getInstalledVersionCode();
		}
		return -1;
	}

	public boolean checkShouLeiEnable() {
		if (mDownload == null) {
			return false;
		}
		if (!isAppInstalled()) {
			mErrorCode = ERROR_CODE_NOT_INSTALL;
			return false;
		}
		if (!isAppVersionSupported()) {
			mErrorCode = ERROR_CODE_NOT_SUPPORT;
			return false;
		}
		return true;
	}

	/**
	 * 判断当前安装手雷版本是否支持SDK接口
	 * 
	 * @return
	 */
	public boolean isAppVersionSupported() {
		if (mDownload == null) {
			return false;
		}
		return mDownload.isAppVersionSupported();
	}

	public boolean createDownloadTask(String url, String file, boolean showTask) {
		if (mDownload == null) {
			LogUtil.e("downloader", "downloader is not initialized");
			return false;
		}
		return mDownload.createDownloadTaskByUrl(url, file, showTask);
	}
}
