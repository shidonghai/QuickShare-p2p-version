package com.cloudsynch.quickshare.utils;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * A manager to save or restore network state when some cases need
 * 
 * @author KingBright
 * 
 */
public class NetworkStateManager {
	private WifiManager mWifiMgr;
	private Context mCtx;

	private boolean mWifiEnabled;
	private boolean mApEnabled;

	private static NetworkStateManager instance;

	public static NetworkStateManager getIntance(Context context) {
		if (instance == null) {
			instance = new NetworkStateManager(context);
		}
		return instance;
	}

	private NetworkStateManager(Context context) {
		mCtx = context;
		mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	public void restoreState() {
		if (mApEnabled) {
			// nothing need to do yet
		} else {
			WifiManagerUtils.disableAp(mCtx);
		}

		if (mWifiEnabled) {
			WifiManagerUtils.disableAp(mCtx);
			mWifiMgr.setWifiEnabled(true);
		} else {
			mWifiMgr.setWifiEnabled(false);
		}

		instance = null;
	}

	public void saveState() {
		mApEnabled = WifiManagerUtils.isApEnabled(mCtx);
		mWifiEnabled = mWifiMgr.isWifiEnabled();
	}

}
