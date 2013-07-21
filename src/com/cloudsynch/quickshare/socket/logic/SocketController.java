package com.cloudsynch.quickshare.socket.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.db.HistoryTable.Columns;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.socket.connection.ConnectionManager;
import com.cloudsynch.quickshare.socket.connection.ConnectionUtils;
import com.cloudsynch.quickshare.socket.logic.TransferController.TransferTask;
import com.cloudsynch.quickshare.socket.model.DiscoveryMessage;
import com.cloudsynch.quickshare.socket.model.FileTransferMessage;
import com.cloudsynch.quickshare.socket.model.MessageFactory;
import com.cloudsynch.quickshare.socket.model.SMessage;
import com.cloudsynch.quickshare.socket.model.StatusUpdateMessage;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.socket.net.SocketUtils;
import com.cloudsynch.quickshare.socket.net.TCPSocketServer;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.utils.StorageManager;
import com.google.gson.Gson;

public class SocketController {
	private static final String TAG = SocketController.class.getName();
	private MessageCenter mMsgCenter;
	private ConnectionManager mConnMgr;
	private Handler mHandler;
	private Context mContext;

	private boolean mWaiting;

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public SocketController(Context context) {
		mContext = context;
		mMsgCenter = MessageCenter.getInstance();
		mConnMgr = ConnectionManager.getInstance();

		// register to listen incoming message
		MessageListener discoverListener = new MessageListener() {
			@Override
			public void onEvent(int type, SMessage msg) {

				android.os.Message message = new android.os.Message();
				message.what = SMessage.MSG_DISCOVER;
				message.obj = msg;
				if (mHandler != null)
					mHandler.sendMessage(message);
			}
		};

		discoverListener.addFilterType(MessageListener.MSG_USER_DISCOVER_ACK
				| MessageListener.MSG_USER_DISCOVER_REQ);
		mMsgCenter.addMessageListener(discoverListener);

		// register to listening File transfer Message
		MessageListener transferListener = new MessageListener() {
			@Override
			public void onEvent(int type, SMessage msg) {

				android.os.Message message = new android.os.Message();
				message.what = SMessage.MSG_FILE_TRANSFER;
				message.obj = msg;
				if (mHandler != null) {
					mHandler.sendMessage(message);
				}
			}
		};

		transferListener.addFilterType(MessageListener.MSG_TRANSFER_REQUEST
				| MessageListener.MSG_TRANSFER_CONFIRM);
		mMsgCenter.addMessageListener(transferListener);

		// register to listen user status update message
		MessageListener statusListener = new MessageListener() {
			@Override
			public void onEvent(int type, SMessage msg) {
				Message message = new Message();
				message.what = SMessage.MSG_STATUS_UPDATE;
				message.obj = msg;
				if (mHandler != null) {
					mHandler.sendMessage(message);
				}
			}
		};

		statusListener.addFilterType(MessageListener.MSG_USER_STATUS_UPDATE);
		mMsgCenter.addMessageListener(statusListener);

	}

	public void discover(final int reqId) {
		new Thread() {
			public void run() {
				LogUtil.e(TAG, "start to discover");
				// compose a discovery message
				DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
						.createMessage(SMessage.MSG_DISCOVER);

				// this is a request message
				msg.setMsgDirection(SMessage.REQ);

				// NOTE: do not use broadcast address like "192.168.1.255", some
				// AP
				// disabled the broadcast features for security sake, instead,
				// we scan
				// all the machine in the LAN at port SIGNAL_PORT

				int ip = ConnectionUtils.getLocalIp(mContext);
				int mask = ConnectionUtils.getLocalNetmask(mContext);

				int subDeviceNum = ((mask ^ 0xFFFFFFFF));
				int subNetIp = ip & mask;

				// add my info
				msg.setData(UserManager.getInstance(mContext).getMyInfo()
						.toString());

				String ipAddr = null;// ConnectionUtils.int2Ip(subNetIp |
										// subDeviceNum);
				msg.setMessageID(reqId);

				for (int i = 1; i < subDeviceNum; i++) {
					// don't need to discovery myself
					if (ip == subNetIp + i) {
						continue;
					}

					ipAddr = ConnectionUtils.int2Ip(subNetIp + i);

					// set recipient address, port
					msg.setRemotePort(ConnectionManager.SIGNAL_PORT);
					msg.setRemoteAdress(ipAddr);

					try {
						mConnMgr.sendMessage(1000, ipAddr, msg.toJsonString()
								.getBytes("utf-8"));

						// add 15 tick sleep to avoid socket buffer over follow
						Thread.sleep(2);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void responseForDiscover(SMessage message) {
		LogUtil.e(TAG, "response for discover");

		if (!mWaiting) {
			LogUtil.e(TAG, "not waiting, pass");
			return;
		}

		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(message.getMessageID());

		// set recipient address, port
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);
		msg.setRemoteAdress(message.getRemoteAddress());
		LogUtil.e("user ip", message.getRemoteAddress());
		// add my info
		msg.setData(UserManager.getInstance(mContext).getMyInfo().toString());

		try {
			mConnMgr.sendMessage(msg.getMessageID(), msg.getRemoteAddress(),
					msg.toJsonString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void updateMyStatus(int userStatus) {

		StatusUpdateMessage msg = (StatusUpdateMessage) MessageFactory
				.createMessage(SMessage.MSG_STATUS_UPDATE);

		msg.setStatus(userStatus);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		UserManager um = UserManager.getInstance(mContext);

		msg.setData(um.getMyInfo().toString());

		for (User user : um.getUserList()) {
			try {
				mConnMgr.sendMessage(1000, user.ip, msg.toJsonString()
						.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void responseForTransfer(FileTransferMessage message) {
		// find a free TCP port to for file transferring
		final int port = SocketUtils.getFreeTCPPort();

		// compose ACK message
		FileTransferMessage msg = (FileTransferMessage) MessageFactory
				.createMessage(SMessage.MSG_FILE_TRANSFER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setFilePath(message.getFilePath());
		final long fileLength = message.getFileLength();
		msg.setMessageID(message.getMessageID());
		msg.setRemoteAdress(ConnectionUtils.int2Ip(ConnectionUtils
				.getLocalIp(mContext)));
		msg.setRemotePort(port);

		// target IP to receive the file
		msg.setTargetIp(message.getRemoteAddress());
		// target port to receive the file
		msg.setTargetPort(message.getRemotePort());

		msg.setData(message.getData());

		final String fileName = msg.getFilePath().substring(
				msg.getFilePath().lastIndexOf("/"));

		File saveFile = createFile(fileName);

		if (saveFile != null) {
			LogUtil.e("accept file", "prepare to accept");
			prepareToAccept(fileLength, saveFile, msg.getData());

			try {
				mConnMgr.sendMessage(msg.getMessageID(), msg.getTargetIp(), msg
						.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else {
			LogUtil.e("accept file", "will not accept file");
		}
	}

	private File createFile(String fileName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File dir = new File(StorageManager.FILE_STORE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File saveFile = new File(dir, fileName);

			if (saveFile.exists()) {
				if (saveFile.delete()) {
					LogUtil.e("accept file", "successfully delete the file");
				} else {
					LogUtil.e("accept file",
							"can not delete the file, over write the file may cause unknown problem");
				}
			}

			try {
				if (saveFile.createNewFile()) {
					LogUtil.e("accept file", "successfully created file");
					return saveFile;
				} else {
					LogUtil.e("accept file", "failed to create file");
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Intent intent = new Intent(SocketService.SOCKET_SDCARD_AVAILABLE_ACTION);
		mContext.sendBroadcast(intent);
		LogUtil.e("accept file", "sd card is not available");
		return null;
	}

	private void prepareToAccept(final long fileLength, final File saveFile,
			final String data) {
		// start TCP server to wait file transfer, 80s timeout
		try {
			TCPSocketServer fileServer = new TCPSocketServer(
					ConnectionManager.TRANSFER_PORT);
			fileServer.setTimeout(30000);
			fileServer.setCallBack(new TCPSocketServer.ServerCallback() {

				@Override
				public void onResponse(InputStream inStream) {
					// TODO save the file according to the file type

					FileOutputStream outStream = null;

					TransferTask task = (TransferTask) new Gson().fromJson(
							data, TransferTask.class);
					if (task.to == null || task.from == null) {
						LogUtil.e(TAG, "accepter is "
								+ (task.to == null ? "null" : task.to.name)
								+ ", sender is " + task.from == null ? "null"
								: task.from.name);
						return;
					}
					HistoryTask ht = new HistoryTask(task, mContext);
					long id = 0;
					try {
						outStream = new FileOutputStream(saveFile);
						long received = 0;
						int len = 0;
						id = ht.insert(saveFile.getPath(),
								HistoryInfo.HistoryType.HISTORY_TYPE_RECV);
						int count = 0;
						byte[] buffer = new byte[1024];

						TransferInfo info = new TransferInfo();
						info.direction = SocketService.TRANSFER_IN;
						info.percent = 0;
						info.task = task;

						mHandler.obtainMessage(SocketService.TRANSFER_STARTED,
								info).sendToTarget();
						while ((len = inStream.read(buffer, 0, buffer.length)) != -1) {
							received += len;
							outStream.write(buffer, 0, len);
							if (count % 500 == 0) {
								int percent = (int) (received * 100 / fileLength);
								info.percent = percent;
								mHandler.obtainMessage(
										SocketService.TRANSFER_PROGRESS, info);
								LogUtil.e("receive progress", "sent:" + percent
										+ "%");
								if (id != 0) {
									ht.updateProgress(received, id);
								}
							}
							count++;
						}

						mHandler.obtainMessage(SocketService.TRANSFER_FINISHED,
								info).sendToTarget();
						info.percent = 100;
						if (id != 0) {
							ht.updateProgress(received, id);
							ht.updateStatus(
									HistoryInfo.Status.STATUS_TRANSFERING_FINISH,
									id);
						}
						inStream.close();
						outStream.flush();
						outStream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			fileServer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startToSendFile(FileTransferMessage message) {
		mConnMgr.startFileTransfer(message.getFilePath(),
				message.getRemoteAddress(), message.getRemotePort(),
				message.getMessageID(), message.getData(), mHandler);
	}

	public void destory() {
		mMsgCenter.destory();
		mMsgCenter = null;
		mConnMgr.release();
		mConnMgr = null;
	}

	public void startToWait() {
		mWaiting = true;
	}

	public void stopWaiting() {
		mWaiting = false;
	}

	public void requestJoin(UserGroup group, int cmd) {
		LogUtil.e(TAG, "request join");

		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);

		// set recipient address, port
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);
		msg.setRemoteAdress(group.host.ip);
		LogUtil.e(TAG, "host ip:" + group.host.ip);
		// add my info
		msg.setData(UserManager.getInstance(mContext).getMyInfo().toString());

		try {
			mConnMgr.sendMessage(msg.getMessageID(), msg.getRemoteAddress(),
					msg.toJsonString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void reponseForJoin(ArrayList<User> users, int cmd) {
		LogUtil.e(SocketController.class.getName(), "response for join");

		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);

		// set recipient address, port
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		for (User user : users) {
			msg.setRemoteAdress(user.ip);
			// add my info
			LogUtil.e(TAG, "request ip:" + user.ip);
			msg.setData(UserManager.getInstance(mContext).getMyInfo()
					.toString());

			try {
				mConnMgr.sendMessage(msg.getMessageID(),
						msg.getRemoteAddress(),
						msg.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void leave(int cmd) {
		UserManager um = UserManager.getInstance(mContext);
		ArrayList<User> users = um.getUserList();

		User myInfo = um.getMyInfo();
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		Gson gson = new Gson();
		msg.setData(gson.toJson(myInfo));

		for (User user : users) {
			msg.setRemoteAdress(user.ip);
			try {
				mConnMgr.sendMessage(msg.getMessageID(),
						msg.getRemoteAddress(),
						msg.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		um.getUserList().clear();
	}

	public void dissolve(int cmd) {
		UserManager um = UserManager.getInstance(mContext);
		ArrayList<User> users = um.getUserList();

		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		Gson gson = new Gson();

		msg.setData(gson.toJson(um.getMyInfo()));
		for (User user : users) {
			msg.setRemoteAdress(user.ip);
			try {
				mConnMgr.sendMessage(msg.getMessageID(),
						msg.getRemoteAddress(),
						msg.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		um.getUserList().clear();
	}

	public boolean isWaiting() {
		return mWaiting;
	}

	public void notifyHostStop(User user, int cmd) {
		LogUtil.e(TAG, "notify the user the host has stopped waiting");
		UserGroup group = new UserGroup();
		group.host = UserManager.getInstance(mContext).getMyInfo();

		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		Gson gson = new Gson();
		msg.setData(gson.toJson(group));

		msg.setRemoteAdress(user.ip);
		try {
			mConnMgr.sendMessage(msg.getMessageID(), msg.getRemoteAddress(),
					msg.toJsonString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void notifyUpdate(User myinfo, int cmd) {
		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		Gson gson = new Gson();
		msg.setData(gson.toJson(myinfo));

		ArrayList<User> users = UserManager.getInstance(mContext).getUserList();
		for (User user : users) {
			msg.setRemoteAdress(user.ip);
			try {
				mConnMgr.sendMessage(msg.getMessageID(),
						msg.getRemoteAddress(),
						msg.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void notifyAllUsers(ArrayList<User> users, int cmd) {
		// compose a discovery message
		DiscoveryMessage msg = (DiscoveryMessage) MessageFactory
				.createMessage(SMessage.MSG_DISCOVER);

		msg.setMsgDirection(SMessage.ACK);
		msg.setMessageID(cmd);
		msg.setRemotePort(ConnectionManager.SIGNAL_PORT);

		Gson gson = new Gson();
		msg.setData(gson.toJson(users));

		for (User user : users) {
			msg.setRemoteAdress(user.ip);
			try {
				mConnMgr.sendMessage(msg.getMessageID(),
						msg.getRemoteAddress(),
						msg.toJsonString().getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void requestFileTransfer(TransferTask task) {
		FileTransferMessage message = (FileTransferMessage) MessageFactory
				.createMessage(SMessage.MSG_FILE_TRANSFER);
		message.setFilePath(task.info.path);
		message.setFileLength(task.info.length);
		message.setTargetIp(task.to.ip);
		message.setTargetPort(808);
		message.setRemoteAdress(ConnectionUtils.int2Ip(ConnectionUtils
				.getLocalIp(mContext)));
		message.setRemotePort(808);
		message.setMsgDirection(SMessage.REQ);

		task.from = UserManager.getInstance(mContext).getMyInfo();
		message.setData(task.toString());
		try {
			mConnMgr.sendMessage(message.getMessageID(), message.getTargetIp(),
					message.toJsonString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static class HistoryTask {
		private TransferTask mInfo;
		private Context mContext;

		public HistoryTask(TransferTask info, Context context) {
			this.mInfo = info;
			this.mContext = context;
		}

		public long insert(String path, int historytype) {
			ContentValues values = new ContentValues();
			values.put(HistoryTable.Columns.PATH, path);
			values.put(HistoryTable.Columns.DATE, System.currentTimeMillis()
					+ "");
			values.put(HistoryTable.Columns.RECIVER, mInfo.to.name);
			values.put(HistoryTable.Columns.RECV_SIZE, "0");
			values.put(HistoryTable.Columns.SENDER, mInfo.from.name);
			values.put(HistoryTable.Columns.STATUS,
					HistoryInfo.Status.STATUS_TRANSFERING);
			values.put(HistoryTable.Columns.TOTAL_SIZE, mInfo.info.length);
			values.put(HistoryTable.Columns.TYPE, historytype);

			try {
				Uri uri = mContext.getContentResolver().insert(
						HistoryTable.CONTENT_URI, values);
				return ContentUris.parseId(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

		public boolean updateProgress(long progress, long id) {
			ContentValues values = new ContentValues();
			values.put(HistoryTable.Columns.RECV_SIZE, "" + progress);
			if (mContext.getContentResolver().update(HistoryTable.CONTENT_URI,
					values, Columns._ID + " = " + id, null) > 0) {
				return true;
			}
			return false;
		}

		public boolean updateStatus(String status, long id) {
			ContentValues values = new ContentValues();
			values.put(HistoryTable.Columns.STATUS, status);
			if (mContext.getContentResolver().update(HistoryTable.CONTENT_URI,
					values, Columns._ID + " = " + id, null) > 0) {
				return true;
			}
			return false;
		}
	}

}
