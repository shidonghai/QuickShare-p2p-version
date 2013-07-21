package com.cloudsynch.quickshare.utils;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This Util class is designed to set view more quickly by codes.
 * 
 * @author KingBright
 * 
 */
public class ViewSettings {

	/**
	 * If you are sure the view is a text view , then use this method to set
	 * text.
	 * 
	 * @param activity
	 * @param viewId
	 * @param stringId
	 */
	public static void setText(Activity activity, int viewId, int stringId) {
		if (viewId < 1 || stringId < 1) {
			return;
		}
		((TextView) activity.findViewById(viewId)).setText(stringId);
	}

	/**
	 * If you are sure the view is a text view , then use this method to set
	 * text.
	 * 
	 * @param parentView
	 * @param viewId
	 * @param stringId
	 */
	public static void setText(View parentView, int viewId, int stringId) {
		((TextView) parentView.findViewById(viewId)).setText(stringId);
	}

	/**
	 * If you are sure the view is a text view , then use this method to set
	 * text.
	 * 
	 * @param parentView
	 * @param viewId
	 * @param stringId
	 */
	public static void setText(View parentView, int viewId, String string) {
		((TextView) parentView.findViewById(viewId)).setText(string);
	}

	public static void setText(Activity activity, int viewId, String name) {
		((TextView) activity.findViewById(viewId)).setText(name);
	}

	/**
	 * If you are sure the view is ImageView, then use this method to set Image.
	 * 
	 * @param activity
	 * @param viewId
	 * @param imageId
	 */
	public static void setImage(Activity activity, int viewId, int imageId) {
		if (viewId < 1 || imageId < 1) {
			return;
		}
		((ImageView) activity.findViewById(viewId)).setImageResource(imageId);
	}

	/**
	 * If you are sure the view is ImageView, then use this method to set Image.
	 * 
	 * @param parentView
	 * @param viewId
	 * @param imageId
	 */
	public static void setImage(View parentView, int viewId, int imageId) {
		if (viewId < 1 || imageId < 1) {
			return;
		}
		((ImageView) parentView.findViewById(viewId)).setImageResource(imageId);
	}

	public static void setVisibility(View view, int viewId, int visibility) {
		if (viewId < 1) {
			return;
		}
		view.findViewById(viewId).setVisibility(visibility);
	}

	public static void setOnClickListener(View parentView, int viewId,
			OnClickListener onClickListener) {
		parentView.findViewById(viewId).setOnClickListener(onClickListener);
	}

	public static void setRelativeGravity(View parentView, int viewId,
			int gravity) {
		if (viewId < 1) {
			return;
		}
		View view = parentView.findViewById(viewId);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		params.addRule(gravity);
		view.setLayoutParams(params);
	}

	public static void setTextColor(View parentView, int viewId, int color) {
		if (viewId < 1) {
			return;
		}
		((TextView) parentView.findViewById(viewId)).setTextColor(color);
	}

}
