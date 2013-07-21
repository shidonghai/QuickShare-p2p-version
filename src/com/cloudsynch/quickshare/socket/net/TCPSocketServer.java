package com.cloudsynch.quickshare.socket.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.cloudsynch.quickshare.utils.LogUtil;

public class TCPSocketServer extends Thread {
	private static final String TAG = TCPSocketServer.class.getName();

	public static interface ServerCallback {
		abstract void onResponse(InputStream inStream);
	}

	private ServerSocket mServerSocket;
	private ServerCallback mCallback = null;
	private boolean mStopServer = false;
	private int mTimeout = 0;

	// indicate whether this TCP sever will "accept" for one time or keep
	// "accept" until "stopSever()" was invoked
	private boolean mPersistent = false;

	public TCPSocketServer(int port) throws java.io.IOException {
		mServerSocket = new ServerSocket(port);
	}

	public void setTimeout(int t) {
		mTimeout = t;
	}

	@Override
	public void run() {
		do {
			try {
				LogUtil.e(TAG, "start to accept file");
				Socket client = mServerSocket.accept();
				client.setSoTimeout(mTimeout);

				if (mCallback != null) {
					mCallback.onResponse(client.getInputStream());
				}
			} catch (IOException e) {
				LogUtil.e(TAG, "accept file error:" + e.getMessage());
				e.printStackTrace();
			}
		} while (mStopServer && mPersistent);

		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCallBack(ServerCallback callback) {
		mCallback = callback;
	}

	public void stopServer() {
		mStopServer = true;

		try {
			mServerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
