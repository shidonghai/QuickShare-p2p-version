
package com.cloudsynch.quickshare.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class TextUtil {

    public static SpannableString fromString(String s, int color) {
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new ForegroundColorSpan(color), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

}
