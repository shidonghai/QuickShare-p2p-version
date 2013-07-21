package com.cloudsynch.quickshare.socket.connection;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

import com.cloudsynch.quickshare.socket.net.TCPRequestManager;
import com.cloudsynch.quickshare.socket.net.TCPSocketRequest;
import com.cloudsynch.quickshare.socket.net.UDPRequestManager;
import com.cloudsynch.quickshare.socket.net.UDPSocketRequest;
import com.cloudsynch.quickshare.socket.net.UDPSocketServer;

/**
 * ConnectionManager
 * <p>
 * Description:
 * </p>
 * <p>
 * Date: 2013-3-29
 * </p>
 * 
 * @author Shi Donghai
 */

// change to local service in project, activity bind to the service to invoke
// the features
public class ConnectionManager {
	// this is not safe, in the future, we need define a series of "default"
	// ports so that we can try to find an available one
	public static int SIGNAL_PORT = 34521;

	public static int TRANSFER_PORT = 10103;

	private static ConnectionManager mConnMgr = null;
	private UDPSocketServer mMsgServer = null;
	private UDPRequestManager mMsgReqManager = null;
	private TCPRequestManager mFileTransManager = null;
	private ConnectionCallback mCallback = null;

	public static interface ConnectionCallback {
		void onMessageReceived(String ip, int port, byte[] data);

		void onMessageSend(long reqId, boolean succeeded);

		void onFileTransferStart(long reqId);

		void onFileTransferFinished(long reqId);

		void onFileTransferError(long reqId);

		void onFileTransferProgress(long reqId, int sizeTransferred);
	}

	public static ConnectionManager getInstance() {
		if (mConnMgr == null) {
			mConnMgr = new ConnectionManager();
			mConnMgr.init();
		}

		return mConnMgr;
	}

	public void setCallBack(ConnectionCallback callback) {
		mCallback = callback;
	}

	private ConnectionManager() {

	}

	// request to start file transferring
	public void startFileTransfer(final String path, String addr, int port,
			final long reqId, String data, Handler handler) {
		if (mFileTransManager == null) {
			mFileTransManager = new TCPRequestManager();
			mFileTransManager.startProcess();
		}

		try {
			// start transfer file
			TCPSocketRequest req = new TCPSocketRequest(TRANSFER_PORT,
					InetAddress.getByName(addr), data, handler);
			Log.e("transfer", port + "");
			req.setRequestID(reqId);
			req.setData(new File(path));
			req.setCallBack(new TCPSocketRequest.TCPRequestCallback() {

				@Override
				public void onStart() {
					mCallback.onFileTransferStart(reqId);
				}

				@Override
				public void onProgressUpdate(int sizeTransferred) {
					mCallback.onFileTransferProgress(reqId, sizeTransferred);
				}

				@Override
				public void onFinish() {
					mCallback.onFileTransferFinished(reqId);
				}

				@Override
				public void onError(String errorInfo) {
					mCallback.onFileTransferError(reqId);
				}
			});
			mFileTransManager.Request(req);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// send message by using UDP, no port need because we used fixed port
	public void sendMessage(long reqId, String addr, byte[] data) {
		// setCallBack
		UDPSocketRequest request = new UDPSocketRequest(SIGNAL_PORT, addr);
		request.setRequestData(data);

		request.setCallBack(new UDPSocketRequest.UDPRequestCallback() {

			@Override
			public void onSend(long reqId, boolean succeeded) {
				Log.w("ShareApp", "message:" + reqId
						+ " send File Trans REQ succeeded = " + succeeded);

				mCallback.onMessageSend(reqId, true);
			}
		});

		mMsgReqManager.Request(request);
	}

	public void init() {

		Log.w("ShareApp", "Connection Manager started");

		try {
			// start the UDPSocketServer to wait incoming message
			mMsgServer = new UDPSocketServer(SIGNAL_PORT);
			mMsgServer.setCallBack(new UDPSocketServer.ServerCallback() {

				@Override
				public void onDataReceived(DatagramPacket data) {
					String ip = data.getAddress().getHostAddress();
					int port = data.getPort();

					mCallback.onMessageReceived(ip, port, data.getData());
				}
			});

			mMsgServer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// start the UDPRequestManager
		mMsgReqManager = new UDPRequestManager();
		mMsgReqManager.startProcess();
	}

	public void release() {
		mMsgServer.stopServer();
		mMsgServer = null;

		// stop udp request pool

		// stop tcp request pool
	}
}
