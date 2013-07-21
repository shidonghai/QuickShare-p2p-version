package com.cloudsynch.quickshare.socket.connection;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ConnectionUtils {
	private static String mMACAddress = null;

	public static String getLocalMacAddress(Context ctx) {
		if (mMACAddress != null) {
			return mMACAddress;
		} else {

			WifiManager wifi = (WifiManager) ctx
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifi.getConnectionInfo();

			mMACAddress = info.getMacAddress();
		}

		return mMACAddress;
	}

	public static int getLocalIp(Context ctx) {

		WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

		int ip = ((dhcpInfo.ipAddress & 0xff) << 24)
				| ((dhcpInfo.ipAddress & 0xff00) << 8)
				| ((dhcpInfo.ipAddress & 0xff0000) >>> 8)
				| ((dhcpInfo.ipAddress & 0xff000000) >>> 24);

		return ip;
	}

	public static String int2Ip(int ipAddr) {
		StringBuffer ipBuf = new StringBuffer();
		ipBuf.insert(0, ipAddr & 0xff).insert(0, '.')
				.insert(0, (ipAddr >>>= 8) & 0xff).insert(0, '.')
				.insert(0, (ipAddr >>>= 8) & 0xff).insert(0, '.')
				.insert(0, (ipAddr >>>= 8) & 0xff);

		return ipBuf.toString();
	}

	public static int getLocalNetmask(Context ctx) {
		WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

		int mask = ((dhcpInfo.netmask & 0xff) << 24)
				| ((dhcpInfo.netmask & 0xff00) << 8)
				| ((dhcpInfo.netmask & 0xff0000) >>> 8)
				| ((dhcpInfo.netmask & 0xff000000) >>> 24);

		return mask;
	}
}
