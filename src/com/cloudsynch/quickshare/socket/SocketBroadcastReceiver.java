package com.cloudsynch.quickshare.socket;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.socket.model.FileTransferMessage;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.utils.LocalNotificationManager;

public class SocketBroadcastReceiver extends BroadcastReceiver {

	private static final int NEVER_COULD_BE = -1;
	protected static final int DISMISS_DIALOG = 0;
	private IntentFilter mIntentFilter;
	private Activity mActivity;
	private ArrayList<Listener> mListeners = new ArrayList<Listener>();

	private static SocketBroadcastReceiver instance;

	public static SocketBroadcastReceiver getInstance(Activity act) {
		if (instance == null) {
			instance = new SocketBroadcastReceiver(act);
		}
		return instance;
	}

	private SocketBroadcastReceiver(Activity act) {
		mActivity = act;
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(SocketService.SOCKET_RESULT_ACTION);
		mIntentFilter.addAction(SocketService.SOCKET_TRANSFER_ACTION);
		mIntentFilter.addAction(SocketService.SOCKET_TRANSFER_REQUEST);
		mIntentFilter.addAction(SocketService.SOCKET_DISSOLVE_ACTION);
		mIntentFilter.addAction(SocketService.SOCKET_LEAVE_ACTION);
		mIntentFilter.addAction(SocketService.SOCKET_SDCARD_AVAILABLE_ACTION);
	}

	public void register() {
		mActivity.registerReceiver(this, mIntentFilter);
	}

	public void unregister() {
		try {
			mActivity.unregisterReceiver(this);
		} catch (Exception e) {
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SocketService.SOCKET_TRANSFER_REQUEST)) {
			FileTransferMessage transferMessage = (FileTransferMessage) intent
					.getSerializableExtra(SocketService.SOCKET_TRANSFER_MSG);
			// TODO ...
		} else if (intent.getAction()
				.equals(SocketService.SOCKET_RESULT_ACTION)) {
			int result = intent.getIntExtra(SocketService.RESULT_CODE, -1);

			if (result == NEVER_COULD_BE) {
				Toast.makeText(mActivity, "never could be", Toast.LENGTH_SHORT)
						.show();
				return;
			} else {
				switch (result) {
				case SocketService.RESPONSE_GROUP_DISCOVER: {
					User stranger = (User) intent
							.getSerializableExtra(SocketService.RESULT_DATA);
					for (Listener callback : mListeners) {
						UserGroup group = new UserGroup();
						group.host = stranger;
						callback.onNewGroupFound(group);
					}
					break;
				}
				case SocketService.REQEUST_TRANSFER: {
					for (Listener callback : mListeners) {
						callback.onReceiveFile();
					}
					break;
				}
				case SocketService.REQEUST_FRIEND: {
					for (Listener callback : mListeners) {
						callback.onRequestJoin((User) intent
								.getSerializableExtra(SocketService.RESULT_DATA));
					}
					break;
				}
				case SocketService.REQUEST_ACCEPTED: {
					for (Listener callback : mListeners) {
						callback.onAccept((User) intent
								.getSerializableExtra(SocketService.RESULT_DATA));
					}
					break;
				}
				case SocketService.REQUEST_REFUSED: {
					for (Listener callback : mListeners) {
						callback.onRefuse((User) intent
								.getSerializableExtra(SocketService.RESULT_DATA));
					}
					break;
				}
				case SocketService.RESPONSE_STOP_WAITING: {
					for (Listener callback : mListeners) {
						callback.onHostStop((UserGroup) intent
								.getSerializableExtra(SocketService.RESULT_DATA));
					}
					break;
				}
				}
			}
		} else if (intent.getAction().equals(
				SocketService.SOCKET_TRANSFER_ACTION)) {
			int status = intent.getIntExtra(SocketService.TRANSFER_STATUS, -1);
			switch (status) {
			case SocketService.TRANSFER_STARTED: {
				TransferInfo info = (TransferInfo) intent
						.getSerializableExtra(SocketService.DATA);
				for (Listener callback : mListeners) {
					callback.onTransferStart(info);
				}
				break;
			}
			case SocketService.TRANSFER_FINISHED: {
				TransferInfo info = (TransferInfo) intent
						.getSerializableExtra(SocketService.DATA);
				for (Listener callback : mListeners) {
					callback.onTransferFinish(info);
				}
				break;
			}
			case SocketService.TRANSFER_PROGRESS: {
				TransferInfo info = (TransferInfo) intent
						.getSerializableExtra(SocketService.DATA);
				for (Listener callback : mListeners) {
					callback.onTransferProgress(info);
				}
				break;
			}
			}
		} else if (intent.getAction().equals(
				SocketService.SOCKET_DISSOLVE_ACTION)) {
			for (Listener callback : mListeners) {
				User user = (User) intent
						.getSerializableExtra(SocketService.RESULT_DATA);
				callback.onDissolve(user);
			}
		} else if (intent.getAction().equals(SocketService.SOCKET_LEAVE_ACTION)) {
			for (Listener callback : mListeners) {
				User user = (User) intent
						.getSerializableExtra(SocketService.RESULT_DATA);
				callback.onLeave(user);
			}
		} else if (intent.getAction().equals(
				SocketService.SOCKET_SDCARD_AVAILABLE_ACTION)) {
			for (Listener callback : mListeners) {
				callback.onSDCardNotAvailable();
			}
		}
	}

	public void registerListener(Listener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public void unregisterListener(Listener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public static interface Listener {
		public void onNewGroupFound(UserGroup group);

		public void onTransferProgress(TransferInfo info);

		public void onTransferFinish(TransferInfo info);

		public void onTransferStart(TransferInfo info);

		public void onSDCardNotAvailable();

		public void onLeave(User user);

		public void onHostStop(UserGroup group);

		public void onDissolve(User user);

		public void onRequestJoin(User user);

		public void onRefuse(User user);

		public void onAccept(User user);

		public void onReceiveFile();
	}
}
