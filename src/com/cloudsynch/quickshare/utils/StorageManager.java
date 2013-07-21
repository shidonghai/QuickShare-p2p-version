package com.cloudsynch.quickshare.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.os.Environment;
import android.os.StatFs;

public class StorageManager {
	public StorageManager() {
	}

	private static final String BASE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/quickshare";
	public static final String CACHE_STORE_PATH = BASE_PATH + "/.cache";
	public static final String FILE_STORE_PATH = BASE_PATH + "/file";
	public static final String AVATAR_STORE_PATH = BASE_PATH + "./avatar";
	public static final String CRASH_LOG_STORE_PATH = BASE_PATH + "/crash";

	static {
		File cacheDir = new File(CACHE_STORE_PATH);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		File nomedia = new File(cacheDir, ".nomedia");
		if (!nomedia.exists()) {
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		File avatarDir = new File(AVATAR_STORE_PATH);
		if (!avatarDir.exists()) {
			avatarDir.mkdirs();
		}
		nomedia = new File(avatarDir, ".nomedia");
		if (!nomedia.exists()) {
			try {
				nomedia.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		File fileDir = new File(FILE_STORE_PATH);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}

		File logDir = new File(CRASH_LOG_STORE_PATH);
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
	}

	public static String getTotalSpace() {
		File path = Environment.getExternalStorageDirectory();

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getBlockCount();
		DecimalFormat fnum = new DecimalFormat("##0.00");
		// Unit Conversions to GB
		return fnum
				.format(availableBlocks * blockSize / 1024 / 1024 / 1024.00f)
				+ "GB";

	}

	public static String getTotalFreeSpace() {
		File path = Environment.getExternalStorageDirectory();

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getAvailableBlocks();
		DecimalFormat fnum = new DecimalFormat("##0.00");
		// Unit Conversions to GB
		return fnum
				.format(availableBlocks * blockSize / 1024 / 1024 / 1024.00f)
				+ "GB";

	}

}