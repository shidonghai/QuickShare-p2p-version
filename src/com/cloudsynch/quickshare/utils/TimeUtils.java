
package com.cloudsynch.quickshare.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class TimeUtils {
    public static String format(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String format(String time) {
        try {
            long t = Long.parseLong(time);
            return format(t);
        } catch (Exception e) {
            return format(System.currentTimeMillis());
        }
    }

    public static String parseSec(int timeMs) {
        if (timeMs < 0) {
            timeMs = 0;
        }
        int totalSeconds = timeMs % 1000 >= 500 ? timeMs / 1000 + 1
                : timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;

        Formatter mFormatter = new java.util.Formatter(null,
                Locale.getDefault());
        return mFormatter.format("%02d:%02d", minutes, seconds).toString();

    }
}
