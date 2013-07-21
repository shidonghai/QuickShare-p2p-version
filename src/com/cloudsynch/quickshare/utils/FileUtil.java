package com.cloudsynch.quickshare.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.cloudsynch.quickshare.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

public class FileUtil {
	public static final String INBOX_ROOT_PATH = "/quickshare";
	public static final int BYTES = 0;
	public static final int KB = 1;
	public static final int MB = 2;
	public static final int GB = 3;
	public static final File INBOX_FILE = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath() + INBOX_ROOT_PATH);
	public static final String SAVE_PATH = INBOX_FILE + "/downloads/";
	public static final String AVATAR_PATH = INBOX_FILE + "/avatar/";

	public static void initSavePath() {
		File dir = new File(SAVE_PATH);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
		dir = new File(AVATAR_PATH);
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();
		}
	}

	public static String formatFromByte(String size) {
		try {

			long s = Long.parseLong(size);
			return formatFromByte(s);
		} catch (Exception e) {
			return formatFromByte(0);
		}
	}

	public static String formatFromByte(long bytes) {
		float size = bytes;
		int suffix = 2;
		size = size / 1024 / 1024;
		while (size > 512) {
			size /= 1024;
			suffix++;
		}
		StringBuilder result = new StringBuilder();
		BigDecimal format = new BigDecimal(Float.toString(size));
		result.append(format.setScale(2, BigDecimal.ROUND_HALF_UP));
		switch (suffix) {
		case BYTES:
			result.append("bytes");
			break;
		case KB:
			result.append("KB");
			break;
		case MB:
			result.append("MB");
			break;
		case GB:
			result.append("GB");
			break;
		}
		return result.toString();
	}

	public static void scanFileAsync(Context ctx, File file) {
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(file));
		ctx.sendBroadcast(scanIntent);
	}

	public static File getOrCreateFile(File path, String name) {
		return getOrCreateFile(path.getAbsolutePath(), name);
	}

	public static File getOrCreateFile(String path, String name) {
		try {
			if (path == null || name == null) {
				return null;
			}
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(dir, name);
			if (!file.exists()) {
				file.createNewFile();
			}
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFileExtension(String file) {
		int index = file.lastIndexOf(".");
		if (index > -1) {
			return file.substring(index + 1);
		}
		return null;
	}

	public static String getFileMimeType(String file) {
		String ext = getFileExtension(file);
		if (ext != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String type = mime.getMimeTypeFromExtension(ext);
			return type == null ? "*/*" : type;
		}
		return "*/*";
	}

	public static int getFileType(String path) {
		String type = getFileMimeType(path);
		if (type.startsWith("image/")) {
			return R.string.resource_manager_photo;
		} else if (type.startsWith("video/")) {
			return R.string.resource_manager_video;
		} else if (type.startsWith("application/vnd.android.package-archive")) {
			return R.string.resource_manager_apk;
		} else if (type.startsWith("text/")) {
			return R.string.resource_manager_doc;
		} else {
			return R.string.resource_manager_other;
		}
	}

	public static boolean saveFile(FileInputStream fis, FileOutputStream fos) {
		if (fis == null || fos == null) {
			return false;
		}
		try {
			byte[] buffer = new byte[1024 * 8];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			return true;
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	public static String getLastModifyDateString(File file) {
		if (null == file) {
			return null;
		}

		Date date = new Date(file.lastModified());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.getDefault());
		return dateFormat.format(date);
	}
}
