package com.cloudsynch.quickshare.socket.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cloudsynch.quickshare.db.UserTable;
import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.socket.connection.ConnectionUtils;
import com.cloudsynch.quickshare.user.MyInfo;

public class UserManager {
	private static final int MAX_SIZE = 4;

	public static final int USER_CHANGE_NONE = 1;
	public static final int USER_CHANGE_ADD = 2;
	public static final int USER_CHANGE_REMOVE = 3;

	private Context mCtx;
	private static UserManager instance;
	private User myInfo = new User();
	private ArrayList<User> mUserList = new ArrayList<User>();
	private ArrayList<UserChangedListener> mListeners = new ArrayList<UserChangedListener>();

	private MyInfo.OnInfoUpdateListener mInfoUpdateListener = new MyInfo.OnInfoUpdateListener() {

		@Override
		public void onInfoUpdate(UserInfo info) {
			UserInfo my = MyInfo.getInstance(mCtx).getInfo();
			myInfo.name = my.name;
			myInfo.photo = my.avatar;
		}

	};

	public void addWhiteList(ArrayList<User> users) {
		for (User user : users) {
			if (isInList(user)) {
				continue;
			}
			UserInfo mUserInfo = createUserInfo(user);
			ContentValues cv = UserTable.converUserToValues(mUserInfo);
			mCtx.getContentResolver().insert(UserTable.CONTENT_URI, cv);
		}
	}

	public void addList(ArrayList<User> user) {

	}

	private UserInfo createUserInfo(User user) {
		return createUserInfo(user, "no");
	}

	private UserInfo createUserInfo(User user, String block) {
		UserInfo mUserInfo = new UserInfo();
		mUserInfo.name = user.name;
		mUserInfo.nickname = "";
		mUserInfo.signture = user.signature;
		mUserInfo.avatar = user.photo;
		mUserInfo.male = UserInfo.MaleType.TYPE_MALE;
		mUserInfo.block = block;
		mUserInfo.status = "0";
		mUserInfo.data = user.identifier;
		mUserInfo.type = UserInfo.UserType.OTHER;
		return mUserInfo;
	}

	private void update(User user) {
		ContentValues cv = UserTable.converUserToValues(createUserInfo(user));
		mCtx.getContentResolver().update(UserTable.CONTENT_URI, cv,
				UserTable.Columns.DATA + " = ?",
				new String[] { user.identifier });
	}

	public boolean isInList(User user) {
		try {
			Cursor cursor = mCtx.getContentResolver().query(
					UserTable.CONTENT_URI,
					null,
					UserTable.Columns.DATA + " = ? AND "
							+ UserTable.Columns.BLOCK + " = ?",
					new String[] { user.identifier, "no" }, null);
			return cursor.getCount() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	private UserManager(Context context) {
		mCtx = context;
		init();
	}

	public void clear() {
		synchronized (mUserList) {
			mUserList.clear();
		}
		for (UserChangedListener l : mListeners) {
			l.notifyUserChanged(mUserList, USER_CHANGE_REMOVE);
		}
	}

	private void init() {
		// TODO here we should read from db to init the user info
		myInfo.identifier = ConnectionUtils.getLocalMacAddress(mCtx);
		myInfo.ip = ConnectionUtils.int2Ip(ConnectionUtils.getLocalIp(mCtx));

		UserInfo my = MyInfo.getInstance(mCtx).getInfo();
		myInfo.name = my.name;

		MyInfo.getInstance(mCtx).register(mInfoUpdateListener);
	}

	public static UserManager getInstance(Context context) {
		if (instance == null) {
			instance = new UserManager(context);
		}
		return instance;
	}

	public boolean updateUserStatus(User user) {
		boolean flag = false;
		synchronized (mUserList) {
			for (User u : mUserList) {
				if (u.identifier.equals(user.identifier)) {
					u.update(user);
					update(user);
					flag = true;
				}
			}
			if (flag) {
				for (UserChangedListener l : mListeners) {
					l.notifyUserStatusChanged(mUserList, user);
				}
			}
		}
		return flag;
	}

	public boolean addUser(User user) {
		if (user.identifier == null) {
			return false;
		}
		synchronized (mUserList) {
			if (!mUserList.contains(user) && !user.equals(myInfo)) {
				mUserList.add(user);
				for (UserChangedListener l : mListeners) {
					l.notifyUserChanged(mUserList, USER_CHANGE_ADD);
				}
				return true;
			}
			return false;
		}
	}

	public boolean removerUser(User user) {
		synchronized (mUserList) {
			if (mUserList.contains(user)) {
				mUserList.remove(user);

				for (UserChangedListener l : mListeners) {
					l.notifyUserChanged(mUserList, USER_CHANGE_REMOVE);
				}
				return true;
			}
			return false;
		}
	}

	public ArrayList<User> getUserList() {
		synchronized (mUserList) {
			return mUserList;
		}
	}

	public void registerCallback(UserChangedListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
			listener.notifyUserChanged(mUserList, USER_CHANGE_NONE);
		}
	}

	public void unregisterCallback(UserChangedListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public boolean isFull() {
		return mUserList.size() >= MAX_SIZE;
	}

	public User getMyInfo() {
		return myInfo;
	}

	public static interface UserChangedListener {
		public void notifyUserChanged(ArrayList<User> list, int status);

		public void notifyUserStatusChanged(ArrayList<User> list, User user);
	}
}
