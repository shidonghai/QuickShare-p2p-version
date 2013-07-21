package com.cloudsynch.quickshare.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class WifiManagerUtils {

	public static final int WIFI_AP_STATE_FAILED = 14;
	public static final String DEFAULT_AP_NAME_PREFIX = "QuickShare_";
	public static final String DEFAULT_AP_NAME = DEFAULT_AP_NAME_PREFIX
			+ Build.BRAND;

	public static boolean isApEnabled(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return isWifiApEnable(wm);
	}

	public static void enableWifi(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wm.setWifiEnabled(true);
	}

	public static boolean getWifiEnabled(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}

	public static void setupDefaultAp(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		setWifiApEnabled(wm, true);
	}

	public static void disableAp(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		setWifiApEnabled(wm, false);
	}

	public static boolean isWifiAvailable(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						LogUtil.e("wifi state", "already connected");
						return true;
					}
				}
			}
		}
		LogUtil.e("wifi state", "not sconnected");
		return false;
	}

	public static WifiInfo getWifiInfo(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (!wm.isWifiEnabled()) {
			return null;
		}
		WifiInfo wifiInfo = wm.getConnectionInfo();
		return wifiInfo;
	}

	public static int getWifiApStatus(WifiManager wifiManager) {
		return 0;
	}

	public static boolean setWifiApEnabled(WifiManager wifiManager,
			WifiConfiguration cfg, boolean flag) {
		try {
			if (wifiManager.isWifiEnabled() && flag) {
				wifiManager.setWifiEnabled(false);
			}
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			Boolean enable = (Boolean) method.invoke(wifiManager, cfg, flag);
			if (!flag) {
				wifiManager.setWifiEnabled(true);
			}
			return enable;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static Boolean isWifiApEnable(WifiManager wifiManager) {
		Method method;
		try {
			method = wifiManager.getClass().getMethod("isWifiApEnabled");
			Boolean enable = (Boolean) method.invoke(wifiManager);
			return enable;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static WifiConfiguration getWifiApConfigurationParam() {
		WifiConfiguration cfg = new WifiConfiguration();
		cfg.SSID = DEFAULT_AP_NAME;
		cfg.preSharedKey = "";
		cfg.allowedKeyManagement.set(KeyMgmt.NONE);
		return cfg;
	}

	public static WifiConfiguration getWifiApConfiguration(
			WifiManager wifiManager) {
		try {
			Method method = wifiManager.getClass().getMethod(
					"getWifiApConfiguration");
			return (WifiConfiguration) method.invoke(wifiManager);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void connectDefaultAp(Context context) {
		try {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> rsList = wm.getScanResults();
			for (ScanResult sr : rsList) {
				if (sr.SSID.startsWith(WifiManagerUtils.DEFAULT_AP_NAME_PREFIX)) {
					// TODO
					LogUtil.e("connecting", "try to connect " + sr.SSID);
					WifiManagerUtils.connect2WifiAp(wm, sr.SSID, sr.BSSID);
					return;
				}
			}
		} catch (Exception e) {
			LogUtil.e("scan result", "no need to handle this");
		}
		LogUtil.e("scan result", "no available ap");
	}

	public static boolean connect2WifiAp(WifiManager wifiManager, String ssid,
			String bssid) {
		WifiConfiguration apConfig = new WifiConfiguration();
		apConfig.BSSID = bssid;
		apConfig.SSID = "\"" + ssid + "\"";
		apConfig.hiddenSSID = true;
		apConfig.status = WifiConfiguration.Status.ENABLED;
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		apConfig.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		int wcgID = isWifiConfigurationExist(wifiManager, apConfig);
		if (wcgID == -1) {
			wcgID = wifiManager.addNetwork(apConfig);
		}
		boolean flag = wifiManager.enableNetwork(wcgID, true);
		return wifiManager.reconnect();
	}

	public static void checkHTCHotspot(WifiConfiguration cfg) {
		if (Build.MODEL.indexOf("HTC") < 0) {
			return;
		}
		Field wifiApProfile;
		try {
			wifiApProfile = WifiConfiguration.class
					.getDeclaredField("mWifiApProfile");
			wifiApProfile.setAccessible(true);
			Object wifiPro = wifiApProfile.get(cfg);
			wifiApProfile.setAccessible(false);

			if (wifiPro != null) {
				Field ssidField = wifiPro.getClass().getDeclaredField("SSID");
				ssidField.setAccessible(true);
				ssidField.set(wifiPro, cfg.SSID);
				ssidField.setAccessible(false);

				Field bssidField = wifiPro.getClass().getDeclaredField("BSSID");
				bssidField.setAccessible(true);
				bssidField.set(wifiPro, cfg.BSSID);
				bssidField.setAccessible(false);

				Field secureTypeField = wifiPro.getClass().getDeclaredField(
						"secureType");
				Log.d("wyg",
						"secureTypeField----------->>"
								+ secureTypeField.get(wifiPro));
				secureTypeField.setAccessible(true);
				secureTypeField.set(wifiPro, "open");
				secureTypeField.setAccessible(false);

				// Field keyField = wifiPro.getClass().getDeclaredField("key");
				// keyField.setAccessible(true);
				// keyField.set(wifiPro, "123123");
				// keyField.setAccessible(false);

				// IP地址设置
				Field ipField = wifiPro.getClass()
						.getDeclaredField("ipAddress");
				ipField.setAccessible(true);
				ipField.set(wifiPro, "192.168.1.1");
				ipField.setAccessible(false);

				// 子网掩码设置
				Field ipmask = wifiPro.getClass().getDeclaredField(
						"dhcpSubnetMask");
				ipmask.setAccessible(true);
				ipmask.set(wifiPro, "255.255.255.0");
				ipmask.setAccessible(false);

				// 起始IP设置
				Field sipField = wifiPro.getClass().getDeclaredField(
						"startingIP");
				sipField.setAccessible(true);
				sipField.set(wifiPro, "192.168.1.100");
				sipField.setAccessible(false);

				Field dhcp = wifiPro.getClass().getDeclaredField("dhcpEnable");
				secureTypeField.setAccessible(true);
				secureTypeField.set(wifiPro, 1);
				secureTypeField.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int isWifiConfigurationExist(WifiManager wifiManager,
			WifiConfiguration cfg) {
		if (cfg == null) {
			return -1;
		}
		List<WifiConfiguration> confList = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration wifiConfiguration : confList) {
			if (wifiConfiguration.networkId == cfg.networkId
					|| (wifiConfiguration.SSID.equals(cfg.SSID) && wifiConfiguration.BSSID
							.equals(cfg.BSSID))) {
				return wifiConfiguration.networkId;
			}
		}
		return -1;
	}

	public boolean setWifiApConfiguration(WifiManager wifiManager,
			WifiConfiguration config) {
		try {
			Method method = wifiManager.getClass().getMethod(
					"setWifiApConfiguration", WifiConfiguration.class);
			return (Boolean) method.invoke(wifiManager, config);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public int setWifiApConfig(WifiManager wifiManager, WifiConfiguration config) {
		try {
			Method method = wifiManager.getClass().getMethod("setWifiApConfig",
					WifiConfiguration.class);
			return (Integer) method.invoke(wifiManager, config);
		} catch (Exception e) {
			e.printStackTrace();
			return WIFI_AP_STATE_FAILED;
		}
	}

	public static boolean setWifiApEnabled(WifiManager wifiManager, boolean flag) {
		try {
			if (wifiManager.isWifiEnabled() && flag) {
				wifiManager.setWifiEnabled(false);
			}
			WifiConfiguration cfg = null;
			if (flag) {
				cfg = WifiManagerUtils.getWifiApConfigurationParam();// new
				checkHTCHotspot(cfg);
			} else {
				cfg = WifiManagerUtils.getWifiApConfiguration(wifiManager);
			}

			Boolean enable = WifiManagerUtils.setWifiApEnabled(wifiManager,
					cfg, flag);
			if (!flag) {
				wifiManager.setWifiEnabled(true);
			}
			return enable;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getApName() {
		return DEFAULT_AP_NAME;
	}

	public static void scan(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		boolean flag = wm.startScan();
		LogUtil.e("wifi", "scanning " + flag);
	}

}
