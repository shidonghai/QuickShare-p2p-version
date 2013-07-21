package com.cloudsynch.quickshare.user;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.db.UserTable;
import com.cloudsynch.quickshare.entity.UserInfo;

public class MyInfo {
	private static MyInfo mUserManager = new MyInfo();
	private UserInfo mUserInfo;

	private List<OnInfoUpdateListener> mListeners = new ArrayList<OnInfoUpdateListener>();

	private MyInfo() {
	}

	public static MyInfo getInstance(Context ctx) {
		if (mUserManager.mUserInfo == null) {
			mUserManager.initUser(ctx);
		}
		return mUserManager;
	}

	public static MyInfo getInstance() {
		return mUserManager;
	}

	public void initUser(Context c) {
		Cursor cusor = null;
		try {
			cusor = c.getContentResolver().query(UserTable.CONTENT_URI, null,
					UserTable.Columns.TYPE + "=?",
					new String[] { UserInfo.UserType.ME + "" }, null);
			if (cusor != null && cusor.moveToFirst()) {
				mUserInfo = UserInfo.getUserInfoFromCursor(cusor);
			}
			if (mUserInfo == null) {
				createDefaultUser(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cusor != null) {
				cusor.close();
			}
		}

	}

	public UserInfo getInfo() {
		return mUserInfo;
	}

	public void setUser(UserInfo uInfo) {
		mUserInfo = uInfo;
	}

	public void updateUserInfo(Context c) {
		ContentValues vs = UserTable.converUserToValues(this.mUserInfo);
		c.getContentResolver().update(UserTable.CONTENT_URI, vs,
				UserTable.Columns._ID + "=" + mUserInfo.id, null);
	}

	private void createDefaultUser(Context c) {
		mUserInfo = new UserInfo();
		mUserInfo.name = c.getString(R.string.pi_user_default_name);
		mUserInfo.nickname = c.getString(R.string.pi_user_default_name);
		mUserInfo.signture = c.getString(R.string.pi_user_default_signture);
		mUserInfo.avatar = c.getString(R.string.pi_user_default_avatar);
		mUserInfo.male = UserInfo.MaleType.TYPE_MALE;
		mUserInfo.block = "no";
		mUserInfo.status = "0";
		mUserInfo.data = "";
		mUserInfo.type = UserInfo.UserType.ME;
		ContentValues cv = UserTable.converUserToValues(mUserInfo);
		Uri uri = c.getContentResolver().insert(UserTable.CONTENT_URI, cv);
		mUserInfo.id = (int) ContentUris.parseId(uri);
	}

	public void register(OnInfoUpdateListener listener) {
		mListeners.add(listener);
	}

	public void updateUserInfo(Context context, UserInfo userInfo) {
		mUserInfo = userInfo;
		ContentValues cv = UserTable.converUserToValues(mUserInfo);
		int res = context.getContentResolver().update(UserTable.CONTENT_URI,
				cv, UserTable.Columns._ID + "=?",
				new String[] { mUserInfo.id + "" });
		if (res > 0) {
			Toast.makeText(context, R.string.pi_user_info_saved,
					Toast.LENGTH_LONG).show();
		}

		for (OnInfoUpdateListener listener : mListeners) {
			listener.onInfoUpdate(mUserInfo);
		}
	}

	public static interface OnInfoUpdateListener {
		public void onInfoUpdate(UserInfo info);
	}
}
