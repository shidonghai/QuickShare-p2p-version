package com.cloudsynch.quickshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.entity.TrafficInfo;
import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.history.HistoryDataLoader;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.socket.model.UserManager.UserChangedListener;
import com.cloudsynch.quickshare.socket.promp.PromptDialog;
import com.cloudsynch.quickshare.transport.TransportActivity;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.user.UserAvatarChooser;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.ViewSettings;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;

public class HomeFragment extends BaseFragment implements OnClickListener {
	private ImageView mListButton;
	private ImageView mHistoryButton;
	private ImageView mUserAvatar;
	private TextView mUserName;
	private TextView mSignture;
	private TextView mTransferInfo;
	private ImageView mUserEditButton;
	private TextView mCreateShare;
	private TextView mJoinShare;
	private TextView mJoinTip;
	private TextView mCreateTip;
	private TextView mShare;
	private UserInfo mUserInfo;
	private View mShareTip;
	private TrafficInfo mTrafficInfo;
	private HistoryDataLoader mLoader;
	public static final int ACTION_FINISH = 0;
	private long mRecvSize = 0;
	private long mSendSIze = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (ACTION_FINISH == msg.what) {
				List<HistoryInfo> list = (List<HistoryInfo>) msg.obj;
				setReceiveAndSentSize(list);
				initViewValues();
			}
		};
	};

	private UserChangedListener mUserChangedListener = new UserChangedListener() {

		@Override
		public void notifyUserStatusChanged(ArrayList<User> list, User user) {
		}

		@Override
		public void notifyUserChanged(ArrayList<User> list, int status) {
			int size = list.size();
			if (size > 0) {
				mJoinShare.setVisibility(View.GONE);
				mJoinTip.setVisibility(View.GONE);
				mCreateTip.setVisibility(View.GONE);
				mCreateShare.setVisibility(View.GONE);
				mShare.setVisibility(View.VISIBLE);
				mShareTip.setVisibility(View.VISIBLE);
			} else {
				mJoinShare.setVisibility(View.VISIBLE);
				mJoinTip.setVisibility(View.VISIBLE);
				mCreateTip.setVisibility(View.VISIBLE);
				mCreateShare.setVisibility(View.VISIBLE);
				mShare.setVisibility(View.GONE);
				mShareTip.setVisibility(View.GONE);
			}
		}
	};

	public void setReceiveAndSentSize(List<HistoryInfo> list) {
		mRecvSize = mLoader.getRecvSize();
		mSendSIze = mLoader.getSentSize();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserInfo = MyInfo.getInstance(getActivity()).getInfo();
		mTrafficInfo = new TrafficInfo();
		mLoader = new HistoryDataLoader(getActivity(),
				new HistoryDataLoader.IDataLoaderListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onLoading() {
					}

					@Override
					public void onFinish(List<HistoryInfo> list) {
						mHandler.sendMessage(mHandler.obtainMessage(
								ACTION_FINISH, list));
					}
				});
	}

	@Override
	public View createView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.home_layout, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		Titlebar titlebar = (Titlebar) view.findViewById(R.id.top);
		titlebar.setTitle(R.string.app_name);
		titlebar.setLeftImage(R.drawable.list_icon);
		titlebar.setRightImage(R.drawable.history_button);
		titlebar.setTitlebarClickListener(new TitlebarClickListener() {
			@Override
			public void onRightClick() {
				startHistoryActivity();
			}

			@Override
			public void onLeftClick() {
				showMenu();
			}
		});

		mUserAvatar = (ImageView) view.findViewById(R.id.user_avatar);
		mUserName = (TextView) view.findViewById(R.id.user_name);
		mSignture = (TextView) view.findViewById(R.id.user_signture);
		mUserEditButton = (ImageView) view.findViewById(R.id.edit_user_info);
		mTransferInfo = (TextView) view.findViewById(R.id.transfer_info);
		mCreateShare = (TextView) view
				.findViewById(R.id.home_create_share_button);
		mJoinShare = (TextView) view.findViewById(R.id.home_join_share_button);
		mJoinTip = (TextView) view.findViewById(R.id.home_join_share_note);
		mCreateTip = (TextView) view.findViewById(R.id.home_create_share_note);
		mShare = (TextView) view.findViewById(R.id.begin_share);
		mShareTip = view.findViewById(R.id.begin_share_note);

		mUserEditButton.setOnClickListener(this);
		mCreateShare.setOnClickListener(this);
		mJoinShare.setOnClickListener(this);
		mShare.setOnClickListener(this);
	}

	private void initViewValues() {
		if (mUserInfo != null) {
			mUserName.setText(mUserInfo.name);
			mSignture.setText(mUserInfo.signture);
			Bitmap bitmap = MyInfo.getInstance(getActivity()).getInfo().avatarBitmap;
			if (bitmap != null) {
				mUserAvatar.setImageBitmap(bitmap);
			} else {
				bitmap = UserAvatarChooser.getAvatar(mUserInfo.avatar,
						getActivity().getBaseContext());
				if (bitmap != null) {
					MyInfo.getInstance().getInfo().avatarBitmap = bitmap;
					mUserAvatar.setImageBitmap(bitmap);
				}
			}
		}
		if (mTrafficInfo != null) {
			mTransferInfo.setText(getString(R.string.home_transfer_info,
					FileUtil.formatFromByte(mSendSIze),
					FileUtil.formatFromByte(mRecvSize)));
		} else {
			mTransferInfo.setText(getString(R.string.home_transfer_info, 0, 0));
		}
	}

	/**
	 * Load history data from db
	 */
	private void startDataLoad() {
		mSendSIze = 0;
		mRecvSize = 0;
		mLoader.load();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		UserManager.getInstance(getActivity()).unregisterCallback(
				mUserChangedListener);
	}

	@Override
	public void onStart() {
		super.onStart();
		startDataLoad();
		initViewValues();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mShare) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), TransportActivity.class);
			startActivity(intent);
		} else if (v == mListButton) {

		} else if (v == mHistoryButton) {
			startHistoryActivity();
		} else if (v == mUserEditButton) {
			startUserEditActivity();
		} else if (v == mCreateShare) {
			EventManager.getInstance().onEvent(EventConstant.USER_SHARE);

			Intent intent = new Intent();
			intent.setClass(getActivity(), TransportActivity.class);

			Bundle bundle = new Bundle();
			bundle.putInt(PromptDialog.KEY_TYPE, PromptDialog.TYPE_WAIT);

			intent.putExtra("extra", bundle);

			startActivity(intent);
		} else if (v == mJoinShare) {
			EventManager.getInstance().onEvent(EventConstant.USER_JOIN);

			Intent intent = new Intent();
			intent.setClass(getActivity(), TransportActivity.class);

			Bundle bundle = new Bundle();
			bundle.putInt(PromptDialog.KEY_TYPE, PromptDialog.TYPE_SCAN);

			intent.putExtra("extra", bundle);

			startActivity(intent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		UserManager.getInstance(getActivity()).registerCallback(
				mUserChangedListener);
	}

	/**
	 * Start the user info edit activity
	 */
	private void startUserEditActivity() {
		EventManager.getInstance().onEvent(EventConstant.USER_EDIT);

		Intent intent = new Intent();
		intent.setClass(getActivity(), PersonalInformationActivity.class);
		getActivity().startActivity(intent);
	}

	private void startHistoryActivity() {
		EventManager.getInstance().onEvent(EventConstant.USER_HISTORY);

		Intent intent = new Intent();
		intent.setClass(getActivity(), HistoryActivity.class);
		getActivity().startActivity(intent);
	}

	public void loadTransferInfo() {
		mLoader.load();
	}
}
