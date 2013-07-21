package com.cloudsynch.quickshare.socket.promp;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.socket.SocketRemoteController;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.utils.ViewSettings;

public class PromptDialog extends Fragment {
	private static final String TAG = PromptDialog.class.getName();

	public static final String ACTION_DISMISS_DIALOG = "action_dismiss_dialog";

	public static final String KEY_TYPE = "type";
	public static final String KEY_OBJ = "obj";

	private static final int COUNT_FROM = 60;
	private static final int COUNT_DELAY = 1000;

	public static final int TYPE_WAIT = 1;
	public static final int TYPE_SCAN = 2;
	public static final int TYPE_CREATE = 3;
	public static final int TYPE_SCAN_TIMEOUT = 4;
	public static final int TYPE_WAIT_TIMEOUT = 5;
	public static final int TYPE_SHOW_REQUEST = 6;
	public static final int TYPE_SHOW_GROUP = 7;
	public static final int TYPE_SHOW_REFUSE = 8;
	public static final int TYPE_SHOW_HOST_STOP = 9;
	public static final int TYPE_SHOW_TRANSFER = 10;
	public static final int TYPE_SHOW_DISSOLVE = 11;
	public static final int TYPE_DECIDE_DISSOLVE = 12;
	public static final int TYPE_SHOW_SDCARD_NOT_AVAILABLE = 13;
	public static final int TYPE_ALL_LEAVE = 14;

	public static final int EVENT_COUNT_DOWN = 201;
	public static final int EVENT_CREATE = 202;
	public static final int EVENT_CREATED = 203;
	public static final int EVENT_SCAN = 204;
	public static final int EVENT_WAIT_TIMEOUT = 205;
	public static final int EVENT_SCAN_TIMEOUT = 206;
	public static final int EVENT_SHOW_WAIT_TIMEOUT = 207;
	public static final int EVENT_SHOW_SCAN_TIMEOUT = 208;
	public static final int EVENT_QUIT = 209;
	private static final int EVENT_SHOW_REQUEST = 210;
	private static final int EVENT_SHOW_GROUP = 211;
	private static final int EVENT_SHOW_REFUSE = 212;
	private static final int EVENT_SHOW_HOST_STOP = 213;
	private static final int EVENT_SHOW_TRANSFER = 214;
	private static final int EVENT_DISMISS_DIALOG = 215;
	private static final int EVENT_ANIMATION = 216;
	private static final int EVENT_SHOW_DISSOLVE = 217;
	private static final int EVENT_DECIDE_DISSOLVE = 218;
	private static final int EVENT_SHOW_SDCARD_NOT_AVAILABLE = 219;
	public static final int EVENT_ALL_LEAVE = 14;

	public static final int NET_AP = 101;
	public static final int NET_WIFI = 102;
	public static final int NET_NONE = 103;

	private int mCurrentEvent;

	private ArrayList<User> mUsers = new ArrayList<User>();
	private ArrayList<UserGroup> mGroups = new ArrayList<UserGroup>();

	private View mView;

	private SocketRemoteController mController = SocketRemoteController
			.getInstance(getActivity());

	private BroadcastReceiver mDismissReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			PromptDialog.this.getActivity().finish();
		}

	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mCurrentEvent = msg.what;

			if (msg.what == EVENT_QUIT) {
				LogUtil.e("event_quit", "finish the activity");
				mController.sendQuitBroadcast();
				return;
			}
			if (getActivity() == null) {
				return;
			}
			switch (msg.what) {
			case EVENT_ALL_LEAVE: {
				showAllLeave();
				break;
			}
			case EVENT_SHOW_SDCARD_NOT_AVAILABLE: {
				showSDCardNotAvailable();
				break;
			}
			case EVENT_DECIDE_DISSOLVE: {
				showDecideDissolve();
				break;
			}
			case EVENT_SHOW_DISSOLVE: {
				showDissolve((User) msg.obj);
				break;
			}
			case EVENT_DISMISS_DIALOG: {
				removeMessages(EVENT_ANIMATION);
				cancel();
				break;
			}
			case EVENT_ANIMATION: {
				doAnimation();
				sendEmptyMessageDelayed(EVENT_ANIMATION, 20);
				break;
			}
			case EVENT_SHOW_TRANSFER: {
				LogUtil.e("handle", "show animation");
				showTransfer();
				break;
			}
			case EVENT_SHOW_REFUSE: {
				showRefuse((User) msg.obj);
				break;
			}
			case EVENT_SHOW_GROUP: {
				showGroup();
				break;
			}
			case EVENT_SHOW_REQUEST: {
				showRequest();
				break;
			}
			case EVENT_COUNT_DOWN: {
				int type = msg.arg2;
				if (msg.arg1 == 0) {
					if (type == TYPE_WAIT) {
						mHandler.sendEmptyMessage(EVENT_WAIT_TIMEOUT);
					} else if (type == TYPE_SCAN) {
						mHandler.sendEmptyMessage(EVENT_SCAN_TIMEOUT);
					}
				} else {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(
							EVENT_COUNT_DOWN, msg.arg1 - 1, msg.arg2),
							COUNT_DELAY);
				}
				break;
			}
			case EVENT_WAIT_TIMEOUT: {
				mController.stopWaiting();
				cancel();
				mController.showDialog(TYPE_WAIT_TIMEOUT);
				break;
			}
			case EVENT_SCAN_TIMEOUT: {
				mController.stopScanning();
				cancel();
				mController.showDialog(TYPE_SCAN_TIMEOUT);
				break;
			}
			case EVENT_SHOW_WAIT_TIMEOUT: {
				onTimeout(TYPE_WAIT_TIMEOUT);
				break;
			}
			case EVENT_SHOW_SCAN_TIMEOUT: {
				onTimeout(TYPE_SCAN_TIMEOUT);
				break;
			}
			case EVENT_CREATED: {
				mController.startWaiting();
				onWait(EVENT_CREATED, msg.arg1, (String) msg.obj);
				break;
			}
			case EVENT_CREATE: {
				mController.create(mHandler);
				onWait(EVENT_CREATE, NET_NONE, getString(R.string.creating));
				break;
			}
			case EVENT_SCAN: {
				onScan();
				mController.startScanning();
				break;
			}
			case EVENT_SHOW_HOST_STOP: {
				mController.stopScanning();
				showHostStop();
				break;
			}
			}
		}

	};

	public PromptDialog(Bundle bundleExtra) {
		setArguments(bundleExtra);
	}

	protected void showAllLeave() {
		ViewSettings.setText(mView, R.id.refuse_text, R.string.user_all_leave);
		ViewSettings.setVisibility(mView, R.id.cancel, View.VISIBLE);
		ViewSettings.setText(mView, R.id.cancel,
				R.string.resource_manager_leave);
		ViewSettings.setText(mView, R.id.accept, R.string.wait);

		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
					}
				});

		ViewSettings.setOnClickListener(mView, R.id.cancel,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mController.stopWaiting();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
	}

	protected void showSDCardNotAvailable() {
		ViewSettings.setText(mView, R.id.refuse_text,
				R.string.sdcard_not_available);
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
					}
				});
	}

	protected void showDecideDissolve() {
		ViewSettings.setText(mView, R.id.refuse_text, R.string.decide_dissolve);
		ViewSettings.setVisibility(mView, R.id.cancel, View.VISIBLE);
		ViewSettings.setOnClickListener(mView, R.id.cancel,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
					}
				});
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mController.dissolve();
						mController.stopWaiting();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
	}

	protected void showTransfer() {
		mHandler.sendEmptyMessageDelayed(EVENT_ANIMATION, 0);
		mHandler.sendEmptyMessageDelayed(EVENT_DISMISS_DIALOG, 1500);
	}

	public void doAnimation() {
		View fileView = mView.findViewById(R.id.file);

		RelativeLayout.LayoutParams params = (LayoutParams) fileView
				.getLayoutParams();
		params.leftMargin += 2;
		fileView.setLayoutParams(params);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter filter = new IntentFilter(ACTION_DISMISS_DIALOG);
		getActivity().registerReceiver(mDismissReceiver, filter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setupView(inflater);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void setupView(LayoutInflater inflater) {
		int type = getType();

		mController.setDialogType(type);
		switch (type) {
		case TYPE_ALL_LEAVE: {
			mView = inflater.inflate(R.layout.refuse_view, null);

			mHandler.sendEmptyMessage(EVENT_ALL_LEAVE);
			break;
		}
		case TYPE_SHOW_SDCARD_NOT_AVAILABLE: {
			mView = inflater.inflate(R.layout.refuse_view, null);

			mHandler.sendEmptyMessage(EVENT_SHOW_SDCARD_NOT_AVAILABLE);
			break;
		}
		case TYPE_DECIDE_DISSOLVE: {
			mView = inflater.inflate(R.layout.refuse_view, null);

			mHandler.sendEmptyMessage(EVENT_DECIDE_DISSOLVE);
			break;
		}
		case TYPE_SHOW_DISSOLVE: {
			mView = inflater.inflate(R.layout.refuse_view, null);

			User user = (User) getArguments().getSerializable(KEY_OBJ);
			mHandler.obtainMessage(EVENT_SHOW_DISSOLVE, user).sendToTarget();
			break;
		}
		case TYPE_SHOW_TRANSFER:
			mView = inflater.inflate(R.layout.transfer_view, null);
			mHandler.sendEmptyMessage(EVENT_SHOW_TRANSFER);
			break;
		case TYPE_SHOW_HOST_STOP:
			mView = inflater.inflate(R.layout.refuse_view, null);
			mHandler.sendEmptyMessage(EVENT_SHOW_HOST_STOP);
			break;
		case TYPE_SCAN:
			mView = inflater.inflate(R.layout.waiting_prompt_view, null);
			mHandler.sendEmptyMessage(EVENT_SCAN);
			break;
		case TYPE_WAIT:
			mView = inflater.inflate(R.layout.waiting_prompt_view, null);
			mHandler.sendEmptyMessage(EVENT_CREATE);
			break;
		case TYPE_SCAN_TIMEOUT:
			mView = inflater.inflate(R.layout.timeout_prompt_view, null);
			mHandler.sendEmptyMessage(EVENT_SHOW_SCAN_TIMEOUT);
			break;
		case TYPE_WAIT_TIMEOUT:
			mView = inflater.inflate(R.layout.timeout_prompt_view, null);
			mHandler.sendEmptyMessage(EVENT_SHOW_WAIT_TIMEOUT);
			break;
		case TYPE_SHOW_REQUEST:
			mView = inflater.inflate(R.layout.request_view, null);
			User user = (User) getArguments().getSerializable(KEY_OBJ);
			if (!mUsers.contains(user)) {
				mUsers.add(user);
			}
			mHandler.sendEmptyMessage(EVENT_SHOW_REQUEST);
			break;
		case TYPE_SHOW_GROUP:
			mView = inflater.inflate(R.layout.group_view, null);

			UserGroup group = (UserGroup) getArguments().getSerializable(
					KEY_OBJ);
			if (!mGroups.contains(group)) {
				mGroups.add(group);
			}
			mHandler.sendEmptyMessage(EVENT_SHOW_GROUP);
			break;
		case TYPE_SHOW_REFUSE:
			mView = inflater.inflate(R.layout.refuse_view, null);
			mHandler.obtainMessage(EVENT_SHOW_REFUSE,
					getArguments().getSerializable(KEY_OBJ)).sendToTarget();
			break;
		}
	}

	private void onWait(int event, int type, String name) {
		ImageView icon = (ImageView) mView.findViewById(R.id.icon);

		switch (type) {
		case NET_WIFI: {
			icon.setImageResource(R.drawable.wifi_icon);
			icon.setVisibility(View.VISIBLE);
			if (name != null) {
				ViewSettings.setText(mView, R.id.text,
						getString(R.string.wifi_title, name));
			}
			ViewSettings.setText(mView, R.id.waiting_text, R.string.waiting);

			// start count down
			Message msg = mHandler.obtainMessage(EVENT_COUNT_DOWN, COUNT_FROM,
					TYPE_WAIT);
			mHandler.sendMessage(msg);
			break;
		}
		case NET_AP: {
			icon.setImageResource(R.drawable.traffic_icon);
			icon.setVisibility(View.VISIBLE);
			if (name != null) {
				ViewSettings.setText(mView, R.id.text,
						getString(R.string.traffic_title, name));
			}
			ViewSettings.setText(mView, R.id.waiting_text, R.string.waiting);

			// start count down
			Message msg = mHandler.obtainMessage(EVENT_COUNT_DOWN, COUNT_FROM,
					TYPE_WAIT);
			mHandler.sendMessage(msg);
			break;
		}
		case NET_NONE: {
			icon.setVisibility(View.GONE);
			ViewSettings.setText(mView, R.id.text, R.string.creating);
			ViewSettings.setRelativeGravity(mView, R.id.text,
					RelativeLayout.CENTER_IN_PARENT);
			ViewSettings.setText(mView, R.id.waiting_text,
					R.string.creating_net);
			break;
		}
		}
	}

	private void onScan() {
		ImageView icon = (ImageView) mView.findViewById(R.id.icon);
		icon.setVisibility(View.GONE);

		ViewSettings.setText(mView, R.id.text, R.string.scan);
		ViewSettings.setText(mView, R.id.waiting_text, R.string.scanning);
		ViewSettings.setRelativeGravity(mView, R.id.text,
				RelativeLayout.CENTER_IN_PARENT);

		Message msg = mHandler.obtainMessage(EVENT_COUNT_DOWN, COUNT_FROM,
				TYPE_SCAN);
		mHandler.sendMessage(msg);

	}

	private void onTimeout(final int type) {
		ViewSettings.setOnClickListener(mView, R.id.cancel,
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						cancel();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
		ViewSettings.setOnClickListener(mView, R.id.submit,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mController.clearUsersAndGroups();
						if (type == TYPE_WAIT_TIMEOUT) {
							mController.startWaiting();
							mController.showDialog(TYPE_WAIT);
						} else {
							mController.startScanning();
							mController.showDialog(TYPE_SCAN);
						}
					}
				});

		if (type == TYPE_WAIT_TIMEOUT) {
			ViewSettings.setText(mView, R.id.timeout_text, R.string.no_user);
			ViewSettings.setText(mView, R.id.text, R.string.time_out);
		} else if (type == TYPE_SCAN_TIMEOUT) {
			ViewSettings.setText(mView, R.id.timeout_text, R.string.no_result);
			ViewSettings.setText(mView, R.id.text, R.string.no_result);
		}
	}

	private OnClickListener mJoinListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			UserGroup group = (UserGroup) v.getTag();
			mController.requestToAdd(group);
		}
	};

	private void showGroup() {
		ViewSettings.setText(mView, R.id.text, R.string.scan_result);

		LinearLayout groupView = (LinearLayout) mView
				.findViewById(R.id.group_view);
		LayoutInflater inflater = getLayoutInflater(null);
		// TODO
		for (UserGroup group : mGroups) {
			View hotspotView = inflater.inflate(R.layout.hotspot_view, null);
			Button button = (Button) hotspotView.findViewById(R.id.button);
			button.setOnClickListener(mJoinListener);
			button.setTag(group);
			ViewSettings.setText(hotspotView, R.id.name, group.host.name);
			groupView.addView(hotspotView);
		}
	}

	private void showRequest() {
		ViewSettings.setText(mView, R.id.text, R.string.request);
		final CheckBox checkbox = (CheckBox) mView.findViewById(R.id.checkbox);
		ViewSettings.setOnClickListener(mView, R.id.refuse,
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						// refuse
						mController.refuse(mUsers);
						cancel();
						if (UserManager.getInstance(getActivity())
								.getUserList().size() == 0) {
							mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
						}
					}
				});
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View view) {
						// TODO accept
						if (checkbox.isChecked()) {
							UserManager.getInstance(getActivity())
									.addWhiteList(mUsers);
						} else {

						}

						mController.accept(mUsers);
						cancel();
					}
				});

		LinearLayout group = (LinearLayout) mView
				.findViewById(R.id.friends_view);
		LayoutInflater inflater = getLayoutInflater(null);
		int count = mUsers.size();
		group.removeAllViews();
		LogUtil.e(TAG, "user count:" + count);
		if (count == 1) {
			View friendViewHorizontal = inflater.inflate(
					R.layout.friend_view_horizontal, null);
			ViewSettings.setText(friendViewHorizontal, R.id.name,
					mUsers.get(0).name);
			group.addView(friendViewHorizontal);
		} else if (count > 1) {
			for (int i = 0; i < count; i++) {
				View friendView = inflater.inflate(R.layout.friend_view, null);
				ViewSettings.setText(friendView, R.id.name, mUsers.get(i).name);
				group.addView(friendView);
			}
		}
	}

	public void showRefuse(User user) {
		ViewSettings.setText(mView, R.id.refuse_text,
				getString(R.string.refuse_hint, user.name));
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
	}

	private void showDissolve(User host) {
		ViewSettings.setText(mView, R.id.refuse_text,
				getString(R.string.host_dissolve, host.name));
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
	}

	private void showHostStop() {
		ViewSettings.setText(mView, R.id.refuse_text, R.string.host_stop);
		ViewSettings.setVisibility(mView, R.id.cancel, View.VISIBLE);
		ViewSettings.setOnClickListener(mView, R.id.cancel,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
					}
				});
		ViewSettings.setText(mView, R.id.accept, R.string.retry);
		ViewSettings.setOnClickListener(mView, R.id.accept,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						cancel();
						mController.startScanning();
						mController.showDialog(TYPE_SCAN);
					}
				});
	};

	public int getType() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			return bundle.getInt(KEY_TYPE);
		}
		return 0;
	}

	public void cancel() {
		mHandler.removeMessages(EVENT_COUNT_DOWN);
		if (getActivity() != null) {
			mController.stopScanning();
			getActivity().finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(mDismissReceiver);
		mController.setDialogType(-1);
	}

	public void onNewIntent(Intent intent) {
		Bundle bundle = intent.getBundleExtra("extra");
		int type = bundle.getInt(KEY_TYPE);
		Object object = bundle.getSerializable(KEY_OBJ);
		if (type == TYPE_SHOW_REQUEST) {
			User user = (User) object;
			if (!mUsers.contains(user)) {
				mUsers.add(user);
				mHandler.sendEmptyMessage(EVENT_SHOW_REQUEST);
			}
		} else if (type == TYPE_SHOW_GROUP) {
			UserGroup group = (UserGroup) object;
			if (!mGroups.contains(group)) {
				mGroups.add(group);
				mHandler.sendEmptyMessage(EVENT_SHOW_GROUP);
			}
		}
	}

	public void onBackPressed() {
		if (mCurrentEvent == EVENT_SCAN || mCurrentEvent == EVENT_CREATED
				|| mCurrentEvent == EVENT_CREATE
				|| mCurrentEvent == EVENT_COUNT_DOWN
				|| mCurrentEvent == EVENT_SHOW_DISSOLVE) {
			cancel();
			mController.stopWaiting();
			mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
		} else if (mCurrentEvent == EVENT_SHOW_REFUSE
				|| mCurrentEvent == EVENT_SHOW_SCAN_TIMEOUT
				|| mCurrentEvent == EVENT_SHOW_WAIT_TIMEOUT
				|| mCurrentEvent == EVENT_DECIDE_DISSOLVE) {
			cancel();
		} else if (mCurrentEvent == EVENT_SHOW_TRANSFER) {
			cancel();
			mHandler.removeMessages(EVENT_DISMISS_DIALOG);
			mHandler.removeMessages(EVENT_ANIMATION);
		} else if (mCurrentEvent == EVENT_SHOW_REQUEST
				|| mCurrentEvent == EVENT_SHOW_GROUP) {
			cancel();
			if (UserManager.getInstance(getActivity()).getUserList().size() == 0) {
				mHandler.sendEmptyMessageDelayed(EVENT_QUIT, 150);
				mController.stopWaiting();
			}
		}
	}
}
