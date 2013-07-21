
package com.cloudsynch.quickshare.utils.thumbnail;

import android.graphics.Bitmap;
import android.provider.MediaStore.Images;

import com.cloudsynch.quickshare.utils.FileUtil;

public class ThumbnailUtils {

    public static Bitmap getBitmap(String path) {
        String type = FileUtil.getFileMimeType(path);
        if (type.startsWith("image/*")) {
            return getImageBitmap(path);
        } else if (type.startsWith("video/*")) {
            return getVideoBitmap(path);
        } else if (type.startsWith("")) {

        }
        return null;
    }

    public static Bitmap getImageBitmap(String path) {
        return null;
    }

    public static Bitmap getVideoBitmap(String path) {
        Bitmap bitmap = null;
        bitmap = android.media.ThumbnailUtils.createVideoThumbnail(path,
                Images.Thumbnails.MICRO_KIND);
        bitmap = android.media.ThumbnailUtils.extractThumbnail(bitmap, 60, 60,
                android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap getApkBitmap(String path) {
        return null;
    }

}
