
package com.cloudsynch.quickshare.user;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cloudsynch.quickshare.R;

public class UserAvatarChooser {
    public static Map<String, Integer> mUserDefaultAvatars;
    static {
        mUserDefaultAvatars = new HashMap<String, Integer>();
        mUserDefaultAvatars.put("head_01", R.drawable.head_01);
        mUserDefaultAvatars.put("head_02", R.drawable.header_02);
        mUserDefaultAvatars.put("head_03", R.drawable.header_03);
        mUserDefaultAvatars.put("head_04", R.drawable.header_04);
        mUserDefaultAvatars.put("head_05", R.drawable.header_05);
        mUserDefaultAvatars.put("head_06", R.drawable.header_06);
        mUserDefaultAvatars.put("head_07", R.drawable.header_07);
        mUserDefaultAvatars.put("head_08", R.drawable.header_08);
        mUserDefaultAvatars.put("head_09", R.drawable.header_09);
        mUserDefaultAvatars.put("head_10", R.drawable.header_10);
    }

    public static Bitmap getAvatar(String path, Context c) {
        try {
            if (mUserDefaultAvatars.containsKey(path)) {
                return BitmapFactory
                        .decodeResource(c.getResources(), mUserDefaultAvatars.get(path));
            } else {
                return BitmapFactory.decodeFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
