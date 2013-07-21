package com.cloudsynch.quickshare.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Xiaohu on 13-6-13.
 */
public class SettingManager {
    public static final String SETTING_SHARED_PREF = "settings";

    public static final String VOICE_NOTIFY = "voice_notify";

    public static final String AUTO_DISCONNECT = "auto_disconnect";

    public static final String CHECK_VERSION = "check_version";

    public static boolean isVoiceNotify(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(VOICE_NOTIFY, true);
    }

    public static void setVoiceNotify(Context context, boolean isChecked) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(VOICE_NOTIFY, isChecked).commit();
    }

    public static boolean isAutoDisconnect(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AUTO_DISCONNECT, true);
    }

    public static void setAutoDisconnect(Context context, boolean isChecked) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(AUTO_DISCONNECT, isChecked).commit();
    }

    public static boolean isAutoCheckVersion(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(CHECK_VERSION, true);
    }

    public static void setAutoCheckVersion(Context context, boolean isChecked) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(CHECK_VERSION, isChecked).commit();
    }

    public static void setPreferences(Context context, boolean isChecked, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                SETTING_SHARED_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, isChecked).commit();
    }

}
