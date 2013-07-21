package com.cloudsynch.quickshare.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
	private static final int IO_BUFFER_SIZE = 8 * 1024;

	public static String getNameFromUrl(String urlString) {
		if (!TextUtils.isEmpty(urlString)) {
			int index = urlString.lastIndexOf("/");
			return urlString.substring(index + 1);
		}
		return null;
	}

	public static File fetchImageByUrl(String urlString, String storePath) {
		File dir = new File(storePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File targetFile = new File(dir, getNameFromUrl(urlString));
		if (!targetFile.exists()) {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return targetFile;
		}

		disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			OutputStream outputStream = new FileOutputStream(targetFile);
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(),
					IO_BUFFER_SIZE);
			out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return targetFile;
		} catch (final IOException e) {
			Log.e("download bitmap", "Error downloading bitmap - " + e);
			targetFile.delete();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
			}
		}
		return null;
	}

	private static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
