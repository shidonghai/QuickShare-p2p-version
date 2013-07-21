package com.cloudsynch.quickshare.socket;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.promp.PromptActivity;
import com.cloudsynch.quickshare.socket.promp.PromptDialog;
import com.cloudsynch.quickshare.socket.transfer.FileInfo;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.utils.WifiManagerUtils;

public class SocketRemoteController {
	private Activity mContext;
	private Handler mHandler;

	private ArrayList<User> mUsers = new ArrayList<User>();
	private ArrayList<UserGroup> mGroups = new ArrayList<UserGroup>();

	private static SocketRemoteController mController;

	public static SocketRemoteController getInstance(Activity context) {
		if (mController == null) {
			mController = new SocketRemoteController(context);
		}
		return mController;
	}

	private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				LogUtil.e("receiver", "try to connect ap");
				WifiManagerUtils.connectDefaultAp(mContext);

				LogUtil.e("receiver", "start scanning");
				startService(SocketService.CMD_DISCOVER);
				mContext.unregisterReceiver(this);
			}
		}
	};

	private SocketRemoteController(Activity context) {
		mContext = context;
	}

	private void startService(int cmd) {
		if (mContext == null) {
			return;
		}
		Intent intent = new Intent();
		intent.setClass(mContext, SocketService.class);
		intent.putExtra(SocketService.CMD_CODE, cmd);
		mContext.startService(intent);
	}

	public void stopScanning() {
		startService(SocketService.CMD_STOP_DISCOVER);
	}

	public boolean containsUser(User user) {
		if (!mUsers.contains(user)) {
			mUsers.add(user);
			return true;
		}
		return false;
	}

	public ArrayList<User> getUsers() {
		return mUsers;
	}

	public boolean containsGroup(UserGroup group) {
		if (!mGroups.contains(group)) {
			mGroups.add(group);
			return true;
		}
		return false;
	}

	public ArrayList<UserGroup> getGroups() {
		return mGroups;
	}

	public void clearUsersAndGroups() {
		mGroups.clear();
		mUsers.clear();
	}

	public void requestToAdd(UserGroup group) {
		Intent intent = new Intent();
		intent.setClass(mContext, SocketService.class);
		intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_REQUEST_JOIN);
		intent.putExtra(SocketService.DATA, group);
		mContext.startService(intent);
	}

	public void startWaiting() {
		startService(SocketService.CMD_WAIT);
	}

	public void startScanning() {
		// if wifi available, then auto connect to AP, otherwise, enable the
		// wifi and scan for AP to connect
		if (!WifiManagerUtils.isWifiAvailable(mContext)) {
			WifiManagerUtils.enableWifi(mContext);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (mContext != null
							&& !WifiManagerUtils.getWifiEnabled(mContext)) {
						LogUtil.e("wifi status", "wifi is not available");
						try {
							LogUtil.e("enabling wifi", "wait 2s for it");
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (mContext == null) {
						return;
					}
					IntentFilter intentFilter = new IntentFilter();
					intentFilter
							.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
					mContext.registerReceiver(mWifiReceiver, intentFilter);
					WifiManagerUtils.scan(mContext);
				}
			}).start();
		} else {
			WifiManagerUtils.connectDefaultAp(mContext);
			startService(SocketService.CMD_DISCOVER);
		}
	}

	public void stopWaiting() {
		startService(SocketService.CMD_STOP_WAITING);
	}

	public void refuse(ArrayList<User> user) {
		Intent intent = new Intent();
		intent.setClass(mContext, SocketService.class);
		intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_REFUSE);
		intent.putExtra(SocketService.DATA, user);
		mContext.startService(intent);
	}

	public void accept(ArrayList<User> user) {
		Intent intent = new Intent();
		intent.setClass(mContext, SocketService.class);
		intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_ACCEPT);
		intent.putExtra(SocketService.DATA, user);
		mContext.startService(intent);
	}

	public void sendFiles(List<File> list) {
		ArrayList<FileInfo> fileList = createFileList(list);

		Intent intent = new Intent();
		intent.setClass(mContext, SocketService.class);
		intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_TRANSFER);
		intent.putExtra(SocketService.DATA, fileList);
		mContext.startService(intent);
	}

	private ArrayList<FileInfo> createFileList(List<File> list) {
		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		for (File file : list) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.name = file.getName();
			fileInfo.path = file.getAbsolutePath();
			fileInfo.length = file.length();
			fileInfo.type = 0;
			fileList.add(fileInfo);
		}
		return fileList;
	}

	public void create(Handler handler) {
		mHandler = handler;
		if (WifiManagerUtils.isWifiAvailable(mContext)) {
			WifiInfo info = WifiManagerUtils.getWifiInfo(mContext);
			Message msg = mHandler.obtainMessage(PromptDialog.EVENT_CREATED,
					PromptDialog.NET_WIFI, 0,
					info == null ? "wifi" : info.getSSID());
			mHandler.sendMessageDelayed(msg, 2000);
			mHandler = null;
			return;
		}
		WifiManagerUtils.setupDefaultAp(mContext);
		new Thread() {
			public void run() {
				int time = 0;
				while (mContext != null
						&& !WifiManagerUtils.isApEnabled(mContext)) {
					try {
						Thread.sleep(2000);
						LogUtil.e("check ap enabled", "false");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (mContext == null) {
					return;
				}
				LogUtil.e("check ap enabled", "true");
				Message msg = mHandler.obtainMessage(
						PromptDialog.EVENT_CREATED, PromptDialog.NET_AP, 0,
						WifiManagerUtils.getApName());
				mHandler.sendMessageDelayed(msg, 500);
				mHandler = null;
				return;
			};
		}.start();
	}

	public void showDialog(int type, Serializable obj) {
		startActivity(type, obj);
	}

	public void showDialog(int type) {
		startActivity(type, null);
	}

	public void startActivity(int type, Serializable obj) {
		if (mContext == null) {
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt(PromptDialog.KEY_TYPE, type);
		if (obj != null) {
			bundle.putSerializable(PromptDialog.KEY_OBJ, obj);
		}
		Intent intent = new Intent();
		intent.putExtra("extra", bundle);
		intent.setClass(mContext, PromptActivity.class);
		mContext.startActivity(intent);
	}

	public void sendQuitBroadcast() {
		if (mContext != null) {
			Intent intent = new Intent("quit");
			mContext.sendBroadcast(intent);
		}
	}

	public void dismissDialog() {
		Intent intent = new Intent(PromptDialog.ACTION_DISMISS_DIALOG);
		mContext.sendBroadcast(intent);
	}

	private int mType = -1;

	public void setDialogType(int type) {
		mType = type;
	}

	public int getDialogType() {
		return mType;
	}

	public void dissolve() {
		startService(SocketService.CMD_DISSOLVE);
	}

	public void leave() {
		startService(SocketService.CMD_LEAVE);
	}

	public static SocketRemoteController getMyInstance(Activity activity) {
		return new SocketRemoteController(activity);
	}

}
