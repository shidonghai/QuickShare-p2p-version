package com.cloudsynch.quickshare.socket.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.util.Log;

/**
 * UDPSocketServer
 * <p>
 * Description:
 * </p>
 * <p>
 * Date: 2013-3-29
 * </p>
 * 
 * @author Shi Donghai
 */

public class UDPSocketServer extends Thread {
	public static interface ServerCallback {
		abstract void onDataReceived(DatagramPacket data);
	}

	private DatagramSocket mServerSocket;
	private DatagramPacket mRcvPackage;

	private ServerCallback mCallback = null;

	private boolean mStopServer = false;
	private byte[] mReadBuf = new byte[2048];

	public UDPSocketServer(int port) throws java.io.IOException {
		mServerSocket = new DatagramSocket(port);
		mRcvPackage = new DatagramPacket(mReadBuf, mReadBuf.length);
	}

	@Override
	public void run() {
		super.run();

		while (!mStopServer) {
			try {
				Log.w("ShareApp", "UDP server start to receive data at port: "
						+ mServerSocket.getLocalPort());
				mReadBuf = new byte[2048];
				mRcvPackage.setData(mReadBuf, 0, mReadBuf.length);
				mServerSocket.receive(mRcvPackage);
				Log.w("ShareApp", "Data received...");

				if (mCallback != null) {
					mCallback.onDataReceived(mRcvPackage);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		mServerSocket.close();
	}

	public void setCallBack(ServerCallback callback) {
		mCallback = callback;
	}

	public void stopServer() {
		mStopServer = true;

		if (mServerSocket != null) {
			mServerSocket.close();
		}
	}
}
