package com.cloudsynch.quickshare.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * This class is used to manage the toast. it will cancel the old one as soon as
 * possible when a new one is asked to be shown.
 * 
 * @author KingBright
 * 
 */
public class ToastManager {
	private Toast mToast;
	private Context mContext;

	private static ToastManager mToastManager;

	private ToastManager(Context ctx) {
		if (ctx == null) {
			throw new NullPointerException("Context can not be null!");
		}
		mContext = ctx;
	}

	public ToastManager getInstance(Context ctx) {
		if (mToastManager != null) {
			mToastManager = new ToastManager(ctx);
		}
		return mToastManager;
	}

	public void showToast(int stringId) {
		cancel();
		mToast.setText(stringId);
		mToast.show();
	}

	public void showToast(String string) {
		cancel();
		mToast.setText(string);
		mToast.show();
	}

	public void setViewAndShow(View view) {
		cancel();
		mToast.setView(view);
		mToast.show();
	}

	private void cancel() {
		if (mToast != null) {
			mToast.cancel();
		} else {
			mToast = new Toast(mContext);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
	}

	public void destroy() {
		mToast.cancel();
		mToast = null;
		mContext = null;
		mToastManager = null;
	}
}
