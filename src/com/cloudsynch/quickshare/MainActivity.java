package com.cloudsynch.quickshare;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.cloudsynch.quickshare.MenuFactory.MenuItem;
import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.netresources.NetworkResourcesFragment;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.ui.ResourceManagerFragment;
import com.cloudsynch.quickshare.settings.SettingManager;
import com.cloudsynch.quickshare.settings.SettingsFragment;
import com.cloudsynch.quickshare.socket.SocketBroadcastReceiver;
import com.cloudsynch.quickshare.socket.SocketRemoteController;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.socket.model.User;
import com.cloudsynch.quickshare.socket.model.UserGroup;
import com.cloudsynch.quickshare.socket.transfer.TransferInfo;
import com.cloudsynch.quickshare.ui.HomeFragment;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.utils.LocalNotificationManager;
import com.cloudsynch.quickshare.utils.LogUtil;
import com.cloudsynch.quickshare.utils.ViewSettings;
import com.cloudsynch.quickshare.utils.WifiManagerUtils;
import com.cloudsynch.quickshare.utils.thumbnail.ImageCache;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends SlidingActivity implements
		OnItemClickListener {

	protected static final String TAG = MainActivity.class.getName();

	private SlidingMenu mSlidingMenu;

	private MenuAdapter mMenuAdapter;

	private HashMap<String, Fragment> mFragments = new HashMap<String, Fragment>();

	LocalNotificationManager mNotifyManager;
	private SocketBroadcastReceiver mReceiver;
	private SocketBroadcastReceiver.Listener mListener = new SocketBroadcastReceiver.Listener() {

		@Override
		public void onTransferStart(TransferInfo info) {
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
		public void onTransferProgress(TransferInfo info) {
			if (info.direction == SocketService.TRANSFER_IN) {

			} else {

			}
		}

		@Override
		public void onTransferFinish(TransferInfo info) {
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
		public void onSDCardNotAvailable() {
		}

		@Override
		public void onRequestJoin(User user) {
		}

		@Override
		public void onRefuse(User user) {
		}

		@Override
		public void onReceiveFile() {
		}

		@Override
		public void onNewGroupFound(UserGroup group) {
		}

		@Override
		public void onLeave(User user) {
		}

		@Override
		public void onHostStop(UserGroup group) {
		}

		@Override
		public void onDissolve(User user) {
		}

		@Override
		public void onAccept(User user) {
		}
	};

	private MyInfo.OnInfoUpdateListener mInfoUpdateListener = new MyInfo.OnInfoUpdateListener() {

		@Override
		public void onInfoUpdate(UserInfo info) {
			mMenuAdapter.notifyDataSetChanged();
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initSlidingView();
		initMenu();

		init();
		checkVersion();

		MyInfo.getInstance().register(mInfoUpdateListener);

		((QuickShareApplication) getApplication()).resume();

		mReceiver = SocketBroadcastReceiver.getInstance(this);
		mReceiver.register();

		mNotifyManager = LocalNotificationManager.getInstance(this);

		SocketRemoteController.getInstance(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_QUIT);
		intent.setClass(this, SocketService.class);
		startService(intent);

		QuickShareApplication.destroy();
		// android.os.Process.killProcess(android.os.Process.myPid());
		mReceiver.unregister();
	}

	private void init() {
		showFragment(HomeFragment.class.getName());

		ImageWorker worker = ((QuickShareApplication) getApplicationContext())
				.getImageWorker();
		worker.addImageCache(getSupportFragmentManager(),
				new ImageCache.ImageCacheParams(getApplicationContext(),
						"diskcache"));
	}

	private void initMenu() {
		ListView menuView = new ListView(this);
		menuView.setDivider(null);
		menuView.setBackgroundColor(getResources().getColor(R.color.home_bg));
		menuView.setOnItemClickListener(this);

		mMenuAdapter = new MenuAdapter(
				MenuFactory.getMenu(getApplicationContext()));
		menuView.setAdapter(mMenuAdapter);
		setBehindContentView(menuView);
	}

	private void initSlidingView() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int offset = dm.widthPixels / 5;

		mSlidingMenu = getSlidingMenu();
		mSlidingMenu.setBehindOffset(offset);
		mSlidingMenu.setShadowWidth(10);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		mSlidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		mSlidingMenu.setBehindScrollScale(0);
		mSlidingMenu.setFadeEnabled(false);
	}

	private void checkVersion() {
		if (SettingManager.isAutoCheckVersion(this)) {
			UmengUpdateAgent.setUpdateOnlyWifi(false);
			UmengUpdateAgent.setOnDownloadListener(null);
			UmengUpdateAgent.update(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// startSocketService();
		MobclickAgent.onResume(this);

		WifiManagerUtils.getWifiInfo(this);

		mReceiver.registerListener(mListener);

	}

	@Override
	protected void onStop() {
		super.onStop();
		mReceiver.unregisterListener(mListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {
		if (position != 0) {
			mMenuAdapter.setSelected(position);
		}
		MenuItem item = (MenuItem) view.getTag();

		showFragment(item.fragmentName);

	}

	private void showFragment(String name) {
		if (TextUtils.isEmpty(name)) {
			return;
		}

		Fragment fragment = null;

		if (HomeFragment.class.getName().equals(name)) {
			EventManager.getInstance().onEvent(EventConstant.MENU_USER_INFO);
		} else if (NetworkResourcesFragment.class.getName().equals(name)) {
			EventManager.getInstance().onEvent(EventConstant.MENU_NET_RESOURCE);
		} else if (ResourceManagerFragment.class.getName().equals(name)) {
			EventManager.getInstance().onEvent(
					EventConstant.MENU_RESOURCE_MANAGER);
		} else if (SettingsFragment.class.getName().equals(name)) {
			EventManager.getInstance().onEvent(EventConstant.MENU_SETTING);
		}

		if (mFragments.containsKey(name)) {
			fragment = mFragments.get(name);
			LogUtil.e("get fragment from map", "get fragment from map");
		} else {
			try {
				fragment = (Fragment) Class.forName(name).newInstance();
				mFragments.put(name, fragment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null != fragment && !fragment.isAdded()) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, fragment).commit();
		}
		getSlidingMenu().showContent();
	}

	class MenuAdapter extends BaseAdapter {
		private ArrayList<MenuItem> menu;

		private int mSelect = 1;

		public MenuAdapter(ArrayList<MenuItem> menu) {
			this.menu = menu;
		}

		public void setSelected(int position) {
			mSelect = position;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return menu == null ? 0 : menu.size();
		}

		@Override
		public MenuItem getItem(int position) {
			return menu == null ? null : menu.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItem item = getItem(position);
			View view = null;
			if (position == 0) {
				view = getLayoutInflater().inflate(R.layout.home_title_view,
						null);
				UserInfo info = MyInfo.getInstance().getInfo();
				ImageView avatar = (ImageView) view.findViewById(R.id.portrait);
				avatar.setImageBitmap(info.avatarBitmap);
				ViewSettings.setText(view, R.id.title, info.name);
			} else {
				view = getLayoutInflater().inflate(R.layout.main_menu_item,
						null);
				ViewSettings.setImage(view, R.id.icon, item.drawable);
				ViewSettings.setText(view, R.id.title, item.title);
				ViewSettings.setText(view, R.id.description, item.description);
				if (position == mSelect) {
					ViewSettings.setTextColor(view, R.id.description,
							getResources().getColor(R.color.golden));
					ViewSettings.setVisibility(view, R.id.now_icon,
							View.VISIBLE);
				}
			}
			view.setTag(item);
			return view;
		}
	}

	public void addIgnoreView(View view) {
		mSlidingMenu.addIgnoredView(view);
	}

	public void removeIgnoreView(View view) {
		mSlidingMenu.removeIgnoredView(view);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(
					ResourceManager.RESOURCE_CATEGORY);
			if (null != fragment) {
				getSupportFragmentManager().popBackStack();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

}
