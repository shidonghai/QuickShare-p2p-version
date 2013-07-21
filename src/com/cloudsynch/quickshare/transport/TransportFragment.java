package com.cloudsynch.quickshare.transport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.resource.module.ResourceDetailFactory;
import com.cloudsynch.quickshare.resource.ui.ResourceDetailBaseFragment;
import com.cloudsynch.quickshare.socket.SocketBroadcastReceiver;
import com.cloudsynch.quickshare.socket.SocketRemoteController;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.socket.model.UserManager.UserChangedListener;
import com.cloudsynch.quickshare.socket.promp.PromptDialog;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.ui.HistoryActivity;
import com.cloudsynch.quickshare.ui.HistoryFragment;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.LocalNotificationManager;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.utils.NetworkStateManager;
import com.cloudsynch.quickshare.utils.ViewSettings;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;

/**
 * Created by Xiaohu on 13-6-13.
 */
public class TransportFragment extends BaseFragment implements
		View.OnClickListener, ResourceDetailBaseFragment.IOnItemClick {
	protected static final String TAG = TransportFragment.class.getName();
	private Button mVideo;
	private Button mApk;
	private Button mPhoto;
	private Button mAudio;
	private Button mSendRecord;
	private Button mSend;
	private Button mDissolve;

	private View mToolbar;

	private TextView mCount;

	private ResourceCategory mCurrentCategory;

	private ResourceDetailBaseFragment mDetailFragment;

	private ResourceCategory mVideoCategory = new ResourceCategory(
			ResourceCategory.Category.VIDEO);

	private ResourceCategory mPhotoCategory = new ResourceCategory(
			ResourceCategory.Category.PHOTO);

	private ResourceCategory mAPKCategory = new ResourceCategory(
			ResourceCategory.Category.APK);

	private ResourceCategory mAudioCategory = new ResourceCategory(
			ResourceCategory.Category.AUDIO);

	private UserManager mUserManager;

	private HistoryFragment mHistoryFragment;

	private SocketRemoteController mController;
	private SocketBroadcastReceiver mSocketReceiver;

	private NetworkStateManager mNetworkStateManager;

	private LocalNotificationManager mNotifyManager;

	private ArrayList<User> mUsers = new ArrayList<User>();
	private ArrayList<UserGroup> mGroups = new ArrayList<UserGroup>();
	private SocketBroadcastReceiver.Listener mCallback = new SocketBroadcastReceiver.Listener() {

		@Override
		public void onRequestJoin(final User user) {
			LogUtil.e(TAG, "client found a request");
			if (!mUsers.contains(user)) {
				mUsers.add(user);

				if (UserManager.getInstance(getActivity()).isInList(user)) {
					ArrayList<User> list = new ArrayList<User>();
					list.add(user);
					mController.accept(list);
					mController.dismissDialog();
					return;
				}
				mController.dismissDialog();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mController.showDialog(PromptDialog.TYPE_SHOW_REQUEST,
								user);
					}
				}, 500);
			}
		}

		@Override
		public void onNewGroupFound(final UserGroup group) {
			LogUtil.e(TAG, "client found a group");
			if (!mGroups.contains(group)) {
				mGroups.add(group);
				if (mController.getDialogType() != PromptDialog.TYPE_SHOW_GROUP) {
					mController.dismissDialog();
				}

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mController.showDialog(PromptDialog.TYPE_SHOW_GROUP,
								group);
					}
				}, 500);
			}
		}

		@Override
		public void onRefuse(final User user) {
			mController.dismissDialog();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mController.showDialog(PromptDialog.TYPE_SHOW_REFUSE, user);
				}
			}, 500);
		}

		@Override
		public void onAccept(User user) {
			mController.dismissDialog();
		}

		@Override
		public void onReceiveFile() {
			if (mFragmentShown) {
				mSendRecord.performClick();
			}
		}

		public void onLeave(User uesr) {
			if (UserManager.getInstance(getActivity()).getUserList().size() == 0) {
				mController.showDialog(PromptDialog.TYPE_ALL_LEAVE);
			}
		}

		@Override
		public void onDissolve(User user) {
			mController.showDialog(PromptDialog.TYPE_SHOW_DISSOLVE, user);
		}

		@Override
		public void onHostStop(UserGroup group) {
			if (mGroups.contains(group)) {
				mController.dismissDialog();

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mController
								.showDialog(PromptDialog.TYPE_SHOW_HOST_STOP);
					}
				}, 500);
			}
		}

		@Override
		public void onSDCardNotAvailable() {
			mController.showDialog(PromptDialog.TYPE_SHOW_SDCARD_NOT_AVAILABLE);
		}

		@Override
		public void onTransferStart(TransferInfo info) {
			if (mFragmentShown) {
				return;
			}
			LogUtil.e(TAG, "transfer start notification");
			if (info.direction == SocketService.TRANSFER_IN) {
				String msg = getString(R.string.get_file_from,
						info.task.from.name, info.task.info.name);
				mNotifyManager.showNotification(info.task.from.identifier, msg);
			} else {
				String msg = getString(R.string.send_file_to,
						info.task.from.name, info.task.info.name);
				mNotifyManager.showNotification(info.task.from.identifier, msg);
			}
		}

		@Override
		public void onTransferFinish(TransferInfo info) {
			if (mFragmentShown) {
				return;
			}
			LogUtil.e(TAG, "transfer finish notification");
			if (info.direction == SocketService.TRANSFER_IN) {
				String msg = getString(R.string.done_get_file_from,
						info.task.from.name, info.task.info.name);
				mNotifyManager.showNotification(info.task.from.identifier, msg);
			} else {
				String msg = getString(R.string.done_send_file_to,
						info.task.to.name, info.task.info.name);
				mNotifyManager.showNotification(info.task.from.identifier, msg);
			}
		}

		@Override
		public void onTransferProgress(TransferInfo info) {
			if (mFragmentShown) {
				return;
			}
			if (info.direction == SocketService.TRANSFER_IN) {

			} else {

			}
		}

	};

	private boolean mFragmentShown;
	private boolean mPendingToQuit;
	private QuitBroadcastReceiver mQuitReceiver;

	private QuitBroadcastReceiver.OnPendingQuitListener mQuitListener = new QuitBroadcastReceiver.OnPendingQuitListener() {
		@Override
		public void onPendingQuit() {
			if (!mPendingToQuit) {
				mPendingToQuit = true;
			} else {
				if (getActivity() != null) {
					getActivity().finish();
				}
			}
		}
	};

	private UserChangedListener mUserChangedListener = new UserChangedListener() {

		@Override
		public void notifyUserStatusChanged(ArrayList<User> list, User user) {
			createUserList(list);
		}

		@Override
		public void notifyUserChanged(ArrayList<User> list, int status) {
			createUserList(list);
		}
	};

	private void createUserList(ArrayList<User> list) {

		mUsers.clear();
		mUsers.addAll(list);
		if (getView() == null) {
			return;
		}
		View friendsView = getView().findViewById(R.id.friends_container);
		if (friendsView == null) {
			return;
		}
		LinearLayout group = (LinearLayout) friendsView;
		group.removeAllViews();
		int count = list.size();
		for (int i = 0; i < count; i++) {
			ImageView image = new ImageView(getActivity());
			image.setImageResource(R.drawable.home_separate_line);
			LinearLayout.LayoutParams imageParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			imageParams.gravity = Gravity.CENTER_VERTICAL;
			imageParams.leftMargin = 2;
			imageParams.rightMargin = 2;
			group.addView(image, imageParams);

			View view = null;
			view = getLayoutInflater(null).inflate(R.layout.friend_view, null);
			ViewSettings.setText(view, R.id.name, list.get(i).name);
			LinearLayout.LayoutParams friendParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			friendParams.gravity = Gravity.CENTER_VERTICAL;

			group.addView(view, friendParams);
		}

	}

	@Override
	public View createView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.transport_layout, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mPendingToQuit) {
			if (getActivity() != null) {
				getActivity().finish();
			}
		} else {
			mPendingToQuit = true;
		}

		mFragmentShown = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		mPendingToQuit = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		mPendingToQuit = false;
		mFragmentShown = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserManager = UserManager.getInstance(getActivity());
		mController = SocketRemoteController.getInstance(getActivity());
		mQuitReceiver = new QuitBroadcastReceiver(getActivity());
		mQuitReceiver.register(mQuitListener);

		mNetworkStateManager = NetworkStateManager.getIntance(getActivity());

		// save network state
		mNetworkStateManager.saveState();
		mNotifyManager = LocalNotificationManager.getInstance(getActivity());

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Titlebar titlebar = (Titlebar) view.findViewById(R.id.title_bar);
		titlebar.setTitle(R.string.app_name);
		titlebar.setLeftImage(R.drawable.list_icon);
		titlebar.setRightImage(R.drawable.history_button);
		titlebar.setTitlebarClickListener(new TitlebarClickListener() {
			@Override
			public void onRightClick() {
				EventManager.getInstance().onEvent(EventConstant.USER_HISTORY);

				Intent intent = new Intent();
				intent.setClass(getActivity(), HistoryActivity.class);
				getActivity().startActivity(intent);
			}

			@Override
			public void onLeftClick() {
				getActivity().onBackPressed();
			}
		});

		mCount = (TextView) view.findViewById(R.id.selected_count);
		mVideo = (Button) view.findViewById(R.id.video);
		mApk = (Button) view.findViewById(R.id.apk);
		mPhoto = (Button) view.findViewById(R.id.photo);
		mAudio = (Button) view.findViewById(R.id.audio);
		mSendRecord = (Button) view.findViewById(R.id.ts_receive);
		mSend = (Button) view.findViewById(R.id.send);
		mDissolve = (Button) view.findViewById(R.id.dissolve);
		mToolbar = (View) view.findViewById(R.id.toolbar);

		Button selectAll = (Button) view.findViewById(R.id.select_all);

		mVideo.setOnClickListener(this);
		mApk.setOnClickListener(this);
		mPhoto.setOnClickListener(this);
		mAudio.setOnClickListener(this);
		mSendRecord.setOnClickListener(this);
		mSend.setOnClickListener(this);
		selectAll.setOnClickListener(this);
		mDissolve.setOnClickListener(this);

		new InitResourceTask().execute();

		mSocketReceiver = SocketBroadcastReceiver.getInstance(getActivity());
		mSocketReceiver.registerListener(mCallback);

		if (getArguments() != null) {
			if (getArguments().getInt(PromptDialog.KEY_TYPE) == PromptDialog.TYPE_SCAN) {
				mDissolve.setText(R.string.resource_manager_leave);
			}
			mController
					.showDialog(getArguments().getInt(PromptDialog.KEY_TYPE));
		} else {
			if (getActivity().getSharedPreferences("mode", 0).getInt(
					PromptDialog.KEY_TYPE, 0) == PromptDialog.TYPE_SCAN) {
				mDissolve.setText(R.string.resource_manager_leave);
			}
		}
		mUserManager.registerCallback(mUserChangedListener);

		mHistoryFragment = new HistoryFragment(
				HistoryInfo.Status.STATUS_TRANSFERING);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		int type = 0;
		if (getArguments() != null) {
			type = getArguments().getInt(PromptDialog.KEY_TYPE);
		}
		if (type != 0) {
			getActivity().getSharedPreferences("mode", 0).edit()
					.putInt(PromptDialog.KEY_TYPE, type).commit();
		}

		// If necessary, stop waiting, or this device will be discovered as a
		// host
		if (UserManager.getInstance(getActivity()).getUserList().size() == 0) {
			mController.stopWaiting();
		} else {
			mNetworkStateManager.restoreState();
		}

		mQuitReceiver.unregister();
		mUserManager.unregisterCallback(mUserChangedListener);

		// when finish, restore network state
		mSocketReceiver.unregisterListener(mCallback);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.dissolve:
			Button btn = (Button) view;
			if (btn.getText().equals(
					getString(R.string.resource_manager_dissolve))) {
				mController.showDialog(PromptDialog.TYPE_DECIDE_DISSOLVE);
			} else {
				mController.leave();
				getActivity().finish();
			}
			break;
		case R.id.video:
			setCategory(view);
			showCategoryFragment(mVideoCategory);
			mToolbar.setVisibility(View.VISIBLE);
			break;
		case R.id.apk:
			setCategory(view);
			showCategoryFragment(mAPKCategory);
			mToolbar.setVisibility(View.VISIBLE);
			break;
		case R.id.photo:
			showCategoryFragment(mPhotoCategory);
			mToolbar.setVisibility(View.VISIBLE);
			setCategory(view);
			break;
		case R.id.audio:
			setCategory(view);
			showCategoryFragment(mAudioCategory);
			mToolbar.setVisibility(View.VISIBLE);
			break;
		case R.id.ts_receive:
			mToolbar.setVisibility(View.GONE);
			setCategory(view);
			if (!mHistoryFragment.isAdded()) {
				getActivity().getSupportFragmentManager().beginTransaction()
						.replace(R.id.category_container, mHistoryFragment)
						.commit();
			}
			break;
		case R.id.send:
			sendSelectedFile();
			mController.showDialog(PromptDialog.TYPE_SHOW_TRANSFER);
			break;
		case R.id.select_all:
			int count = mDetailFragment.mMultipleSelection.getAllCount();
			int selected = mDetailFragment.mMultipleSelection.getSelectItems()
					.size();
			mDetailFragment.mMultipleSelection.selectAll(!(count == selected));
			break;
		default:
			break;
		}
	}

    public void showReceive() {
        mToolbar.setVisibility(View.GONE);
        setCategory(mSendRecord);
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.category_container,
                        new HistoryFragment(
                                HistoryInfo.Status.STATUS_TRANSFERING))
                .commit();
    }

	private void sendSelectedFile() {
		String event = EventConstant.TRANSPORT_TYPES.get(mDetailFragment
				.getCategory());
		EventManager.getInstance().onEvent(event);

		List<File> list = mDetailFragment.mMultipleSelection.getSelectItems();

		if (list.size() == 0 || mUserManager.getUserList().size() == 0) {
			return;
		}
		mController.sendFiles(list);
	}

	private void setCategory(View view) {
		mVideo.setSelected(false);
		mApk.setSelected(false);
		mPhoto.setSelected(false);
		mAudio.setSelected(false);
		mSendRecord.setSelected(false);
		mSend.setVisibility(View.GONE);

		view.setSelected(true);
	}

	public void showCategoryFragment(ResourceCategory category) {
		mCurrentCategory = category;
		mCount.setVisibility(View.GONE);
		mDetailFragment = ResourceDetailFactory
				.getDetailFragment(category.category);
		Bundle bundle = new Bundle();
		bundle.putSerializable(ResourceManager.RESOURCE_CATEGORY, category);
		mDetailFragment.setIOnItemClick(this);
		mDetailFragment.setArguments(bundle);
		mDetailFragment.setMultipleSelectMode(true);
		getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.category_container, mDetailFragment)
				.commitAllowingStateLoss();
	}

	private void setCount() {
		int count = mDetailFragment.mMultipleSelection.getAllCount();
		int selected = mDetailFragment.mMultipleSelection.getSelectItems()
				.size();
		if (selected > 0) {
			mCount.setText(String.format(
					getString(R.string.resource_manager_selected_count), count,
					getString(mCurrentCategory.category.getStringId())));
			mCount.setVisibility(View.GONE);
			mSend.setVisibility(View.VISIBLE);
		} else {
			mCount.setText(String.format(
					getString(R.string.resource_manager_selected_count), count,
					getString(mCurrentCategory.category.getStringId())));
			mCount.setVisibility(View.VISIBLE);
			mSend.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick() {
		setCount();
	}

	@Override
	public void onSelectAll() {
		setCount();
	}

	private class InitResourceTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... objects) {
			ResourceManager resourceManager = ResourceManager
					.getInstance(getActivity());
			resourceManager.initResource(getActivity(), mPhotoCategory);
			resourceManager.initResource(getActivity(), mVideoCategory);
			resourceManager.initResource(getActivity(), mAPKCategory);
			resourceManager.initResource(getActivity(), mAudioCategory);
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			if (getActivity() != null) {
                showCategoryFragment(mVideoCategory);

                String action = getActivity().getIntent() == null ? null :
                        getActivity().getIntent().getAction();
                if (SocketService.ACTION_RECEIVE.equals(action)) {
                   showReceive();
                } else {
                    setCategory(mVideo);
                }
            }
        }
	}

	public static class QuitBroadcastReceiver extends BroadcastReceiver {

		private Activity mAct;
		private IntentFilter mFilter;
		private OnPendingQuitListener mListener;

		public QuitBroadcastReceiver(Activity act) {
			mAct = act;
			mFilter = new IntentFilter();
			mFilter.addAction("quit");
		}

		public void register(OnPendingQuitListener listener) {
			mAct.registerReceiver(this, mFilter);
			mListener = listener;
		}

		public void unregister() {
			mAct.unregisterReceiver(this);
			mListener = null;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mListener != null) {
				mListener.onPendingQuit();
			}
		}

		public static interface OnPendingQuitListener {
			public void onPendingQuit();
		}
	}

}
