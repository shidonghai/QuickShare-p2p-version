package com.cloudsynch.quickshare.ui;

import java.io.File;
import java.util.List;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.history.HistoryDataLoader;
import com.cloudsynch.quickshare.history.HistoryDataLoader.IDataLoaderListener;
import com.cloudsynch.quickshare.history.HistoryFileOpeartor;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;
import com.umeng.socialize.bean.SocializeUser;

public class HistoryFragment extends Fragment implements OnClickListener,
		OnItemClickListener {

	private ImageView mBackButton;
	private ImageView mDeleteButton;
	private TextView mEmptyList;
	private ListView mList;
	private HistoryAdapter mAdapter;
	private List<HistoryInfo> mHistoryLists;
	private HistoryDataLoader mDataLoader;
	public static final int DATA_CHANGE = 0;
	public static final int DATA_LOADED = 1;
	public static final int DATA_DELETED = 2;
	public static final int DELETE_FILE_START = 3;
	public static final int DELETE_FILE = 4;
	public static final int DELETE_FILE_FINISH = 5;
	private OperationDialog mOperationMenu;
	private ViewGroup mDelPanel;
	private TextView mHistoryDelButton;
	private TextView mHistoryDelCancelButton;
	private DeleteDialog mDelDialog;
	private SocializeUser mUser;
	private static final String LOAD_TYPE_ALL = "all";
	// 加载数据的类型，全部加载或正在发送的文件
	private String mLoadDataType = LOAD_TYPE_ALL;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == DATA_CHANGE) {
				// startDataLoad();
			} else if (msg.what == DATA_LOADED) {
				if (mOperationMenu != null && !mOperationMenu.isShowing()) {
					List<HistoryInfo> list = (List<HistoryInfo>) msg.obj;
					mAdapter.setData(list);
					mAdapter.setTrafficInfo(mDataLoader.getSentSize(),
							mDataLoader.getRecvSize());
				}
			} else if (msg.what == DELETE_FILE_FINISH) {
				onDeleteFinish();
			}
		};
	};

	private Handler mProgressHandler = new Handler();

	private Runnable mProgressRunnable = new Runnable() {

		@Override
		public void run() {
			if (mOperationMenu != null && !mOperationMenu.isShowing()) {
				startDataLoad();
			}
			mProgressHandler.postDelayed(this, 2000);
		}
	};

	private ContentResolver mResolver;
	private ContentObserver mObservable = new ContentObserver(mHandler) {
		public void onChange(boolean selfChange) {
			mHandler.sendEmptyMessage(DATA_CHANGE);
		};
	};

	/**
	 * History data load listener
	 */
	private IDataLoaderListener mListener = new IDataLoaderListener() {
		@Override
		public void onStart() {
		}

		@Override
		public void onLoading() {
		}

		@Override
		public void onFinish(List<HistoryInfo> list) {
			mHandler.sendMessage(mHandler.obtainMessage(DATA_LOADED, list));
		}
	};

	public HistoryFragment() {
	}

	public HistoryFragment(String loadType) {
		mLoadDataType = loadType;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResolver = getActivity().getContentResolver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.history_layout, null);
		initView(view);
		return view;
	}

	private void initView(View view) {
		// mBackButton = (ImageView)
		// view.findViewById(R.id.history_back_button);
		// mDeleteButton = (ImageView)
		// view.findViewById(R.id.history_del_button);
		mEmptyList = (TextView) view.findViewById(R.id.list_item_empty_info);
		mList = (ListView) view.findViewById(R.id.history_list);
		mDelPanel = (ViewGroup) view.findViewById(R.id.history_del_panel);
		mHistoryDelButton = (TextView) view
				.findViewById(R.id.history_del_comfirm);
		mHistoryDelCancelButton = (TextView) view
				.findViewById(R.id.history_del_cancel);
		if (LOAD_TYPE_ALL.equals(mLoadDataType)) {
			mAdapter = new HistoryAdapter(getActivity(), true);
		} else {
			mAdapter = new HistoryAdapter(getActivity(), false);
		}
		mList.setAdapter(mAdapter);
		mDataLoader = new HistoryDataLoader(getActivity(), mListener);
		// view.findViewById(R.id.top).setVisibility(View.GONE);
		showTitle(view);

		// Add listeners to views
		// mBackButton.setOnClickListener(this);
		// mDeleteButton.setOnClickListener(this);
		mList.setOnItemClickListener(this);
		mHistoryDelButton.setOnClickListener(this);
		mHistoryDelCancelButton.setOnClickListener(this);
	}

	private void showTitle(View view) {
		Titlebar titlebar = (Titlebar) view.findViewById(R.id.history_top);
		if (LOAD_TYPE_ALL.equals(mLoadDataType)) {
			titlebar.setTitle(R.string.history_title);
			titlebar.setLeftImage(R.drawable.return_button);
			titlebar.setRightImage(R.drawable.press_delete_icon);
			titlebar.setTitlebarClickListener(new TitlebarClickListener() {
				@Override
				public void onRightClick() {
					setDeleteMode();
					updateDelNum();
				}

				@Override
				public void onLeftClick() {
					getActivity().finish();
				}
			});
		} else {
			titlebar.setVisibility(View.GONE);
		}
	}

	private void registerContentObserver() {
		mResolver.registerContentObserver(HistoryTable.CONTENT_URI, false,
				mObservable);
	}

	private void unregisterContentObserver() {
		mResolver.unregisterContentObserver(mObservable);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStart() {
		registerContentObserver();
		// startDataLoad();
		mProgressHandler.post(mProgressRunnable);
		super.onStart();
	}

	@Override
	public void onStop() {
		unregisterContentObserver();
		mProgressHandler.removeCallbacks(mProgressRunnable);
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		mOperationMenu = new OperationDialog(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Load history data from db
	 */
	private void startDataLoad() {
		if (LOAD_TYPE_ALL.equals(mLoadDataType)) {
			mDataLoader.load();
		} else {
			mDataLoader.load(mLoadDataType,
					HistoryInfo.HistoryType.HISTORY_TYPE_RECV, "date desc");
		}
	}

	private void onDeleteFinish() {
		mAdapter.clearSelectList();
		updateDelNum();
		startDataLoad();
		mDelDialog.dismiss();
		if (mOperationMenu.isShowing()) {
			mOperationMenu.dismiss();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position == 0) {
			return;
		}
		if (mAdapter.isCheckedMode()) {
			mAdapter.setChecked(view, position);
			updateDelNum();
		} else {
			final HistoryInfo info = mAdapter.getItem(position);
            if (null == info) {
                return;
            }

			mOperationMenu.setContent(new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doHistoryClick(which, info);
				}
			});
			mOperationMenu.showAsDropDown(view);
		}
	}

	private void doHistoryClick(int which, HistoryInfo info) {
		if (R.id.history_item_open == which) {
			openFile(info);
		} else if (R.id.history_item_del == which) {
			mAdapter.getSelectedList().add(info);
			mDelDialog = new DeleteDialog();
			mDelDialog.show(getActivity().getSupportFragmentManager(),
					mDelDialog, "deldialog");
		} else if (R.id.history_item_share == which) {
			ShareDialog dialog = ShareDialog.getInstance();
			dialog.show(getActivity().getSupportFragmentManager(), dialog,
					"sharedialog");
		} else if (R.id.history_item_attribute == which) {
			AttributeDialog dialog = AttributeDialog.newInstance(info);
			dialog.show(getActivity().getSupportFragmentManager(), dialog,
					"attrdialog");
		}
	}

	private void openFile(HistoryInfo info) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(info.filePath));

		intent.setDataAndType(uri, FileUtil.getFileMimeType(info.filePath));
		startActivity(Intent.createChooser(intent, ""));
	}

	/**
	 * Set the checked list items number
	 */
	private void updateDelNum() {
		int num = mAdapter.getSelectedList().size();
		mHistoryDelButton.setText(getString(R.string.history_record_del, num));
	}

	@Override
	public void onClick(View v) {
		if (v == mBackButton) {

		} else if (v == mDeleteButton) {
			setDeleteMode();
			updateDelNum();
		} else if (v == mHistoryDelButton) {
            if ( 0 == mAdapter.getSelectedList().size()) {
                Toast.makeText(getActivity(),R.string.histor_no_item_to_del,Toast.LENGTH_SHORT).show();
            } else {
			    mDelDialog = new DeleteDialog();
			    mDelDialog.show(getActivity().getSupportFragmentManager(),
					    mDelDialog, "deldialog");
            }
		} else if (v == mHistoryDelCancelButton) {
			setDeleteMode();
		}
	}

	/**
	 * Set delete mode to listview
	 */
	private void setDeleteMode() {
		mAdapter.setCheckMod();
		if (mDelPanel.getVisibility() == View.GONE) {
			mDelPanel.setVisibility(View.VISIBLE);
		} else {
			mDelPanel.setVisibility(View.GONE);
		}
	}

	public void show(DialogFragment dialog, String tagName) {
		FragmentTransaction ft = getActivity().getSupportFragmentManager()
				.beginTransaction();
		Fragment prev = getActivity().getSupportFragmentManager()
				.findFragmentByTag(tagName);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		dialog.show(ft, tagName);
	}

	/**
	 * @author wangyouguo History record delete dialog
	 */
	private class DeleteDialog extends BaseDialog {
		private TextView mDelRecord;
		private TextView mDelRecordFile;
		private ViewGroup mProgressBar;

		@Override
		public View getBody(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.history_del_dialog, null);
			mDelRecord = (TextView) view.findViewById(R.id.del_record);
			mDelRecord.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HistoryFileOpeartor opeartor = new HistoryFileOpeartor(
							mResolver, mHandler);
					opeartor.delRecord(mAdapter.getSelectedList(), false);
					showProgressBar();
					// dismiss();
				}
			});
			mDelRecordFile = (TextView) view.findViewById(R.id.del_record_file);
			mDelRecordFile.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					HistoryFileOpeartor opeartor = new HistoryFileOpeartor(
							mResolver, mHandler);
					opeartor.delRecord(mAdapter.getSelectedList(), false);
					showProgressBar();
					// dismiss();
				}
			});
			mProgressBar = (ViewGroup) view.findViewById(R.id.del_bar);
			return view;
		}

		/**
		 * show the delete progress bar
		 */
		private void showProgressBar() {
			mDelRecord.setVisibility(View.GONE);
			mDelRecordFile.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			setCancelable(false);
		}
	}
}
