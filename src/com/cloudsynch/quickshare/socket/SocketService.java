package com.cloudsynch.quickshare.socket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.socket.logic.SocketController;
import com.cloudsynch.quickshare.socket.logic.TransferController;
import com.cloudsynch.quickshare.socket.logic.TransferController.TransferTask;
import com.cloudsynch.quickshare.socket.model.DiscoveryMessage;
import com.cloudsynch.quickshare.socket.model.FileTransferMessage;
import com.cloudsynch.quickshare.socket.model.SMessage;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.socket.transfer.FileInfo;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.user.MyInfo.OnInfoUpdateListener;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.google.gson.Gson;

public class SocketService extends Service {
	protected static final String TAG = "socket service";

	public static final String SOCKET_TRANSFER_REQUEST = "transfer_request";
	public static final String SOCKET_TRANSFER_ACTION = "transfer";
	public static final String SOCKET_DISSOLVE_ACTION = "dissolve";
	public static final String SOCKET_LEAVE_ACTION = "leave";
	public static final String SOCKET_SDCARD_AVAILABLE_ACTION = "sdcard_not_available";
	public static final String SOCKET_TRANSFER_MSG = "transfer_message";
	public static final String SOCKET_RESULT_ACTION = "result";
	public static final String RESULT_CODE = "result_code";
	public static final String RESULT_DATA = "result_data";
	public static final String CMD_CODE = "cmd_code";
	public static final String DATA = "data";
	public static final String TARGET_USER = "target_user";
	public static final String TRANSFER_END = "transfer_end";
	public static final String TRANSFER_STATUS = "transfer_status";
	public static final String FILE_LENGTH = "file_length";
	public static final String SENT_LENGTH = "sent_length";
	public static final String PROGRESS = "progress";
    public static final String ACTION_RECEIVE = "com.cloudsynch.quickshare.action.RECEIVE";

	public static final int CMD_DISCOVER = 100;
	public static final int CMD_TRANSFER = 101;
	public static final int CMD_UPDATE = 102;
	public static final int CMD_WAIT = 103;
	public static final int CMD_STOP_WAITING = 104;
	public static final int CMD_REQUEST_JOIN = 105;
	public static final int CMD_ACCEPT = 106;
	public static final int CMD_REFUSE = 107;
	public static final int CMD_STOP_DISCOVER = 108;
	public static final int CMD_NOTIFY_ALL = 109;
	public static final int CMD_DISSOLVE = 110;
	public static final int CMD_LEAVE = 111;
	public static final int CMD_DESTROY = 112;
	public static final int CMD_HEART_BEAT = 113;
	public static final int CMD_QUIT = 114;

	public static final int RESPONSE_GROUP_DISCOVER = 200;
	public static final int RESPONSE_TRANSFER = 201;
	public static final int RESPONSE_STOP_WAITING = 202;

	public static final int REQEUST_TRANSFER = 300;
	public static final int REQEUST_FRIEND = 301;
	public static final int REQUEST_REFUSED = 302;
	public static final int REQUEST_ACCEPTED = 303;
	public static final int REQUEST_TRANSFER_DELAY = 304;
	public static final int REQUEST_PHOTO = 305;

	public static final int TRANSFER_STARTED = 400;
	public static final int TRANSFER_FINISHED = 401;
	public static final int TRANSFER_PROGRESS = 402;
	public static final int TRANSFER_RESPONSE_REFUSED = 403;
	public static final int TRANSFER_RESPONSE_ACCEPT = 404;
	public static final int TRANSFER_OUT = 405;
	public static final int TRANSFER_IN = 406;

	public static final int USER_STATUS_UPDATE = 500;

	public SocketController mController;
	private UserManager mManager;

	private TransferController mTransferController;

	private Handler mHandler = new Handler() {
		private int times = 0;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CMD_HEART_BEAT: {
				// TODO
				break;
			}
			case CMD_ACCEPT: {
				accept(msg);
				break;
			}
			case CMD_REFUSE: {
				refuse(msg);
				break;
			}
			case CMD_REQUEST_JOIN: {
				requestJoin(msg);
				break;
			}
			case CMD_STOP_WAITING: {
				stopWaiting();
				break;
			}
			case CMD_WAIT: {
				startWaiting();
				break;
			}
			case CMD_UPDATE: {
				break;
			}
			case CMD_TRANSFER: {
				initTransferTask(msg);
				requestAllUserToTransfer();
				break;
			}
			case CMD_DISCOVER: {
				times++;
				discoverArround(msg, CMD_DISCOVER);
				if (times < 9) {
					sendEmptyMessageDelayed(CMD_DISCOVER, 6000);
				} else {
					times = 0;
				}
				break;
			}
			case CMD_STOP_DISCOVER: {
				removeMessages(CMD_DISCOVER);
				times = 0;
				break;
			}
			case SMessage.MSG_DISCOVER: {
				onDiscoverMsg(msg);
				break;
			}
			case SMessage.MSG_FILE_TRANSFER: {
				FileTransferMessage transferMessage = (FileTransferMessage) msg.obj;
				if (transferMessage.getMsgDirection() == SMessage.REQ) {
					Log.e("transfer", "respond for file transfer request");

					Gson gson = new Gson();
					TransferTask transferTask = gson.fromJson(
							transferMessage.getData(), TransferTask.class);
					if (!mManager.getUserList().contains(transferTask.from)) {
						LogUtil.e("TAG",
								"I'm not in the group now, I won't accept the file");
						return;
					}

					mController.responseForTransfer(transferMessage);
					Intent intent = new Intent(SOCKET_RESULT_ACTION);
					intent.putExtra(RESULT_CODE, REQEUST_TRANSFER);
					sendBroadcast(intent);
				} else {
					Log.e("transfer", "get response and start to transfer file");
					mController.startToSendFile(transferMessage);
				}
				break;
			}
			case TRANSFER_RESPONSE_REFUSED: {
				FileTransferMessage transferMessage = (FileTransferMessage) msg.obj;
				mController.responseForTransfer(transferMessage);
				break;
			}
			case TRANSFER_RESPONSE_ACCEPT: {
				FileTransferMessage transferMessage = (FileTransferMessage) msg.obj;
				mController.responseForTransfer(transferMessage);
				break;
			}
			case TRANSFER_STARTED: {
				// notify UI
				Intent intent = new Intent(SOCKET_TRANSFER_ACTION);
				intent.putExtra(TRANSFER_STATUS, TRANSFER_STARTED);
				intent.putExtra(DATA, (Serializable) msg.obj);
				sendBroadcast(intent);
				break;
			}
			case TRANSFER_FINISHED: {
				// notify UI
				TransferInfo info = (TransferInfo) msg.obj;

				Intent intent = new Intent(SOCKET_TRANSFER_ACTION);
				intent.putExtra(TRANSFER_STATUS, TRANSFER_FINISHED);
				intent.putExtra(DATA, info);
				sendBroadcast(intent);

				// continue next
				LogUtil.e("transfer", "finished");
				if (info.direction == TRANSFER_OUT) {
					LogUtil.e("transfer", "start next");
					User user = info.task.to;
					TransferTask task = mTransferController.pollTask(user);
					if (task != null) {
						Message message = mHandler.obtainMessage(
								REQUEST_TRANSFER_DELAY, task);
						mHandler.sendMessageDelayed(message, 3000);
					}
				}
				break;
			}
			case TRANSFER_PROGRESS: {
				Intent intent = new Intent(SOCKET_TRANSFER_ACTION);
				intent.putExtra(DATA, (Serializable) msg.obj);
				intent.putExtra(TRANSFER_STATUS, TRANSFER_PROGRESS);
				sendBroadcast(intent);
				break;
			}
			case REQUEST_TRANSFER_DELAY: {
				requestToTransfer((TransferTask) msg.obj);
				break;
			}
			case SMessage.MSG_STATUS_UPDATE: {
				break;
			}
			default:
				break;
			}
		}

	};

	public SocketService() {
	}

	private void refuse(Message msg) {
		ArrayList<User> users = (ArrayList<User>) msg.obj;
		mController.reponseForJoin(users, SocketService.CMD_REFUSE);
	}

	private void accept(Message msg) {
		ArrayList<User> users = (ArrayList<User>) msg.obj;
		for (User user : users) {
			mManager.addUser(user);
		}
		mController.reponseForJoin(users, SocketService.CMD_ACCEPT);

		notifyAllUsers(CMD_NOTIFY_ALL);
	}

	private void notifyAllUsers(int cmd) {
		ArrayList<User> users = mManager.getUserList();
		mController.notifyAllUsers(users, cmd);
	}

	private void requestJoin(Message msg) {
		mController.requestJoin((UserGroup) msg.obj, REQEUST_FRIEND);
	}

	/**
	 * when receive a discover message
	 * 
	 * @param msg
	 */
	private void onDiscoverMsg(android.os.Message msg) {
		DiscoveryMessage discoverMsg = (DiscoveryMessage) msg.obj;
		Gson gson = new Gson();

		if (discoverMsg.getMsgDirection() == SMessage.REQ) {
			LogUtil.e("discover", "some one is discovering");
			mController.responseForDiscover(discoverMsg);
		} else if (discoverMsg.getMsgDirection() == SMessage.ACK) {
			switch (discoverMsg.getMessageID()) {
			case USER_STATUS_UPDATE: {
				User user = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				mManager.updateUserStatus(user);
				break;
			}
			case CMD_LEAVE: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				stranger.ip = discoverMsg.getRemoteAddress();
				if (mManager.removerUser(stranger)) {
					Intent intent = new Intent(SOCKET_LEAVE_ACTION);
					intent.putExtra(RESULT_DATA, stranger);
					sendBroadcast(intent);
				}
				break;
			}
			case CMD_DISSOLVE: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				if (!mManager.getUserList().contains(stranger)) {
					return;
				}
				mManager.clear();

				Intent intent = new Intent(SOCKET_DISSOLVE_ACTION);
				intent.putExtra(RESULT_DATA, stranger);
				sendBroadcast(intent);
				break;
			}
			case REQEUST_FRIEND: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				stranger.ip = discoverMsg.getRemoteAddress();
				if (mController.isWaiting()) {
					LogUtil.e("discover", "a user is requesting to join");
					Intent intent = new Intent(SOCKET_RESULT_ACTION);
					intent.putExtra(RESULT_CODE, REQEUST_FRIEND);
					intent.putExtra(RESULT_DATA, stranger);
					sendBroadcast(intent);
				} else {
					mController.notifyHostStop(stranger, RESPONSE_STOP_WAITING);
					LogUtil.e(TAG, "notify the user host is stopped");
				}
				return;
			}
			case RESPONSE_STOP_WAITING: {
				UserGroup group = (UserGroup) gson.fromJson(
						discoverMsg.getData(), UserGroup.class);
				Intent intent = new Intent(SOCKET_RESULT_ACTION);
				intent.putExtra(RESULT_CODE, RESPONSE_STOP_WAITING);
				intent.putExtra(RESULT_DATA, group);
				sendBroadcast(intent);
				return;
			}
			case CMD_ACCEPT: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				stranger.ip = discoverMsg.getRemoteAddress();
				LogUtil.e("discover", "group host accepts your request");
				Intent intent = new Intent(SOCKET_RESULT_ACTION);
				intent.putExtra(RESULT_CODE, REQUEST_ACCEPTED);
				intent.putExtra(RESULT_DATA, stranger);
				mManager.addUser(stranger);
				sendBroadcast(intent);
				return;
			}
			case CMD_REFUSE: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				LogUtil.e("discover", "group host refuses your request");
				Intent intent = new Intent(SOCKET_RESULT_ACTION);
				intent.putExtra(RESULT_CODE, REQUEST_REFUSED);
				intent.putExtra(RESULT_DATA, stranger);
				sendBroadcast(intent);
				return;
			}
			case CMD_DISCOVER: {
				User stranger = (User) gson.fromJson(discoverMsg.getData(),
						User.class);
				stranger.ip = discoverMsg.getRemoteAddress();
				LogUtil.e("discover", "a group is discovered");
				Intent intent = new Intent(SOCKET_RESULT_ACTION);
				intent.putExtra(RESULT_CODE, RESPONSE_GROUP_DISCOVER);
				intent.putExtra(RESULT_DATA, stranger);
				sendBroadcast(intent);
				mHandler.removeMessages(CMD_DISCOVER);
				return;
			}
			case CMD_NOTIFY_ALL: {

				User[] users = gson.fromJson(discoverMsg.getData(),
						User[].class);
				for (User user : users) {
					if (mManager.addUser(user)) {
						LogUtil.e(TAG, "new user added");
					}
				}
				return;
			}
			}
		}
	}

	/**
	 * To discover nearby group
	 * 
	 * @param msg
	 */
	private void discoverArround(android.os.Message msg, int cmd) {
		mController.discover(cmd);
	}

	/**
	 * start to wait for user to join
	 */
	private void startWaiting() {
		mController.startToWait();
	}

	/**
	 * stop waiting
	 */
	private void stopWaiting() {
		mController.stopWaiting();
	}

	private void initTransferTask(Message msg) {
		ArrayList<FileInfo> fileList = (ArrayList<FileInfo>) msg.obj;
		mTransferController = new TransferController();
		mTransferController.offer(fileList, mManager.getUserList());
	}

	private void requestAllUserToTransfer() {
		Set<User> users = mTransferController.getUserList();
		for (User user : users) {
			TransferTask task = mTransferController.pollTask(user);
			if (task == null) {
				return;
			}
			requestToTransfer(task);
		}
	}

	/**
	 * request to share a file
	 * 
	 * @param msg
	 */
	private void requestToTransfer(TransferTask task) {
		Log.e(TAG, "request to transfer a file");
		mController.requestFileTransfer(task);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mManager = UserManager.getInstance(this);
		mController = new SocketController(this);
		mController.setHandler(mHandler);

		MyInfo.getInstance().register(new OnInfoUpdateListener() {
			@Override
			public void onInfoUpdate(UserInfo info) {
				User user = mManager.getMyInfo();
				user.name = info.name;
				user.photo = info.avatar;

				mController.notifyUpdate(user, USER_STATUS_UPDATE);
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int cmd = intent.getIntExtra(CMD_CODE, 0);

		switch (cmd) {
		case CMD_QUIT: {
			if (mController.isWaiting()) {
				mController.dissolve(CMD_DISSOLVE);
			} else {
				mController.leave(CMD_LEAVE);
			}
			mController.stopWaiting();
			break;
		}
		case CMD_LEAVE: {
			mController.leave(cmd);
			break;
		}
		case CMD_DISSOLVE: {
			mController.dissolve(cmd);
			break;
		}
		case CMD_STOP_DISCOVER: {
			mHandler.sendEmptyMessage(cmd);
			break;
		}
		case CMD_ACCEPT: {
		}
		case CMD_REFUSE: {
		}
		case CMD_REQUEST_JOIN: {
			mHandler.obtainMessage(cmd, intent.getSerializableExtra(DATA))
					.sendToTarget();
			break;
		}
		case CMD_DISCOVER: {
			mHandler.sendEmptyMessage(cmd);
			break;
		}
		case CMD_TRANSFER: {
			Message msg = mHandler.obtainMessage();
			msg.obj = intent.getSerializableExtra(DATA);
			msg.what = cmd;
			msg.sendToTarget();
			break;
		}
		case TRANSFER_RESPONSE_ACCEPT: {
			Message msg = mHandler.obtainMessage();
			msg.obj = intent.getSerializableExtra(SOCKET_TRANSFER_MSG);
			msg.what = TRANSFER_RESPONSE_ACCEPT;
			msg.sendToTarget();
			break;
		}
		case CMD_UPDATE: {
			break;
		}
		case CMD_STOP_WAITING: {
			mHandler.sendEmptyMessage(CMD_STOP_WAITING);
			break;
		}
		case CMD_WAIT: {
			mHandler.sendEmptyMessage(CMD_WAIT);
			break;
		}
		}

		// return to ensure the service is always running.
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		mHandler.removeMessages(CMD_DISCOVER);
		mHandler.removeMessages(CMD_HEART_BEAT);

		super.onDestroy();
	}

}
