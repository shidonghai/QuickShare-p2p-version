package com.cloudsynch.quickshare.socket.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.socket.logic.SocketController;
import com.cloudsynch.quickshare.socket.logic.SocketController.HistoryTask;
import com.cloudsynch.quickshare.socket.logic.TransferController.TransferTask;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.google.gson.Gson;

public class TCPSocketRequest extends SocketRequest {
	private static final String TAG = TCPSocketRequest.class.getName();

	public static interface TCPRequestCallback {
		abstract void onStart();

		abstract void onError(String errorInfo);

		abstract void onFinish();

		abstract void onProgressUpdate(int sizeTransferred);
	}

	private Socket mSocket = null;
	private TCPRequestCallback mCallback = null;
	private int mTargetPort = 0;
	private InetAddress mTargetAddress = null;
	private InputStream mInputStream = null;
	private Handler mHandler;
	private long mLength;
	private String mData;

	public TCPSocketRequest(int port, InetAddress addr, String data,
			Handler handler) {
		mTargetPort = port;
		mTargetAddress = addr;
		mHandler = handler;
		mData = data;
	}

	public void setCallBack(TCPRequestCallback callback) {
		mCallback = callback;
	}

	@Override
	public void startRequst() {
		try {
			mSocket = new Socket(mTargetAddress, mTargetPort);
			mSocket.setSoTimeout(mTimeout);

			OutputStream os = mSocket.getOutputStream();

			// read the input stream by unit of 4K
			byte[] buffer = new byte[1024];
			int len = 0;

			if (mCallback != null) {
				mCallback.onStart();
			}

			long sent = 0;
			int count = 0;

			TransferTask task = (TransferTask) new Gson().fromJson(mData,
					TransferTask.class);
			Context context = QuickShareApplication.getApplication();
			if (context == null) {
				LogUtil.e(TAG, "the context is null, so transfer can not go on");
				return;
			}
			TransferInfo info = new TransferInfo();
			info.task = task;
			info.percent = 0;
			info.direction = SocketService.TRANSFER_OUT;

			mHandler.obtainMessage(SocketService.TRANSFER_STARTED, info)
					.sendToTarget();

			HistoryTask ht = new SocketController.HistoryTask(task, context);
			// HistoryTask ht = new HistoryTask(info, mContext);
			long id = ht.insert(task.info.path,
					HistoryInfo.HistoryType.HISTORY_TYPE_SEND);
			while ((len = mInputStream.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, len);

				sent += len;
				// need to update progress
				if (count % 500 == 0) {
					int percent = (int) (sent * 100 / mLength);
					info.percent = percent;

					mHandler.obtainMessage(SocketService.TRANSFER_PROGRESS,
							info).sendToTarget();

					LogUtil.e("sent progress", "sent:" + percent + "%");
					if (id != 0) {
						ht.updateProgress(sent, id);
					}
				}
				count++;
			}

			os.flush();
			mHandler.obtainMessage(SocketService.TRANSFER_FINISHED, info)
					.sendToTarget();
			if (id != 0) {
				ht.updateProgress(sent, id);
				ht.updateStatus(HistoryInfo.Status.STATUS_TRANSFERING_FINISH,
						id);
			}
			mHandler = null;
		} catch (IOException e) {
			Log.e("transfer", e.toString());
			e.printStackTrace();
		} finally {
			try {
				mCallback.onFinish();
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void setData(InputStream istream) {
		mInputStream = istream;
	}

	public void setData(File file) {
		try {
			mInputStream = new FileInputStream(file);
			mLength = file.length();
		} catch (FileNotFoundException e) {
			Log.e("error", "file not open");
			if (mCallback != null) {
				// TODO: define the message in a separate file
				mCallback.onError("Cannot open the file");
			}
			e.printStackTrace();
		}
	}

}
