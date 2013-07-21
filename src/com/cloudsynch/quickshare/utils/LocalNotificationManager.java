package com.cloudsynch.quickshare.utils;

import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.transport.TransportActivity;

public class LocalNotificationManager {
	private Context mCtx;
	private NotificationManager mManager;

	private static LocalNotificationManager instance;

	public static LocalNotificationManager getInstance(Context ctx) {
		if (instance == null) {
			instance = new LocalNotificationManager(ctx);
		}
		return instance;
	}

	private HashMap<String, Integer> mKV = new HashMap<String, Integer>();

	private LocalNotificationManager(Context ctx) {
		mCtx = ctx;
		mManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void showNotification(String tag, String msg) {
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.when = System.currentTimeMillis();
		notification.defaults |= Notification.DEFAULT_SOUND;

		int id = -1;
		if (!mKV.containsKey(tag)) {
			id = (int) System.currentTimeMillis();
			mKV.put(tag, id);
		} else {
			id = mKV.get(tag);
        }
        Intent intent = new Intent();
        intent.setClass(mCtx,TransportActivity.class);
        intent.setAction(SocketService.ACTION_RECEIVE);
        PendingIntent pendingIntent = PendingIntent.getActivity(mCtx, 0,
                intent, 0);
        notification.setLatestEventInfo(mCtx,
				mCtx.getString(R.string.app_name), msg, pendingIntent);


		mManager.notify(tag, id, notification);
	}

	public void cancel(String tag) {
		int id = -1;
		if (!mKV.containsKey(tag)) {
			mManager.cancel(tag, id);
		}
	}
}
