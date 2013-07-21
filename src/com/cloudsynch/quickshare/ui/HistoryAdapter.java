package com.cloudsynch.quickshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.user.UserAvatarChooser;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.StorageManager;
import com.cloudsynch.quickshare.utils.TextUtil;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker.LoadMethod;

public class HistoryAdapter extends BaseAdapter {
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_ITEM = 1;
	public static final int HISTORY_SEND = 1;
	public static final int HISTORY_RECV = 2;
	private List<HistoryInfo> mHistoryList = new ArrayList<HistoryInfo>();
	private List<HistoryInfo> mSelectList = new ArrayList<HistoryInfo>();
	private LayoutInflater mInflater;
	private Context mContext;
	private boolean isCheckMode;
	private ImageWorker mWorker;
	private Bitmap mDefaultIcon;
	private long mTrafficInfo;
	private ResourceManager mResourceManager;
	private boolean mSendType;
	private long mRecvSize = 0;
	private long mSendSize = 0;

	private LoadMethod mFileIconMethod = new LoadMethod() {
		@Override
		public Bitmap processBitmap(Object obj, Context context) {

			HistoryInfo info = (HistoryInfo) obj;
			return BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.head_01);
		}
	};

	public interface onDataLoadChange {
		void onChange();
	}

	public HistoryAdapter(Context c, boolean sendType) {
		mSendType = sendType;
		mInflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;
		mResourceManager = ResourceManager.getInstance(c);
	}

	public HistoryAdapter(Context c) {
		this(c, false);
	}

	@Override
	public int getCount() {
		return mHistoryList == null ? 1 : mHistoryList.size() + 1;
	}

	public void setData(List<HistoryInfo> list) {
		mHistoryList.clear();
		mHistoryList.addAll(list);
		notifyDataSetChanged();
	}

	public void setTrafficInfo(long sendSize, long recvSize) {
		mSendSize = sendSize;
		mRecvSize = recvSize;
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_HEADER;
		} else {
			return TYPE_ITEM;
		}
	}

	private int getItemViewType(HistoryInfo info) {
		return info.historyType;

	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public HistoryInfo getItem(int position) {
		if (position == 0) {
			return null;
		} else {
			return mHistoryList.get(position - 1);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setCheckMod() {
		setCheckMode(!isCheckMode);
	}

	private void setCheckMode(boolean flag) {
		isCheckMode = flag;
		if (!isCheckMode) {
			mSelectList.clear();
		}
		notifyDataSetChanged();
	}

	public boolean isCheckedMode() {
		return isCheckMode;
	}

	public void setAllChecked(boolean flag) {
		if (flag) {
			mSelectList.clear();
			mSelectList.addAll(mHistoryList);
		} else {
			mSelectList.clear();
		}
		notifyDataSetChanged();
	}

	public List<HistoryInfo> getSelectedList() {
		return mSelectList;
	}

	public void clearSelectList() {
		mSelectList.clear();
	}

	/**
	 * When we click the item, we set the item checked or not.
	 * 
	 * @param item
	 * @param position
	 */
	public void setChecked(View item, int position) {
		HistoryInfo info = getItem(position);
		if (mSelectList.contains(info)) {
			mSelectList.remove(info);
		} else {
			mSelectList.add(info);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			return getHeaderView(convertView);
		} else {
			return getHistoryItems(convertView, position);
		}
	}

	private View getHistoryItems(View convertView, int position) {
		ViewHolder holder = null;
		int type = getItemViewType(getItem(position));
		int typeLayout = HISTORY_RECV == type ? R.layout.history_item_recv
				: R.layout.history_item_send;
		if (convertView == null) {
			convertView = mInflater.inflate(typeLayout, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(typeLayout, holder);
		} else {
			holder = (ViewHolder) convertView.getTag(typeLayout);
			// If we don't do this the sender and receiver maybe incorrect.
			if (holder == null) {
				convertView = mInflater.inflate(typeLayout, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(typeLayout, holder);
			}
		}
		holder.setData(getItem(position));
		mResourceManager.getThumbnail(holder.fileIcon,
				getItem(position).filePath);
		return convertView;
	}

	private View getHeaderView(View convertView) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.history_list_header, null);

		}
		String totalSpace = StorageManager.getTotalSpace();
		String freeSpace = StorageManager.getTotalFreeSpace();
		TextView total = (TextView) convertView
				.findViewById(R.id.storage_total_space);
		TextView free = (TextView) convertView
				.findViewById(R.id.storage_free_space);
		total.setText(getStoraeSpannabl(R.string.history_storage_total_space,
				totalSpace));
		free.setText(getStoraeSpannabl(R.string.history_storage_free_space,
				freeSpace));
		TextView traffic = (TextView) convertView
				.findViewById(R.id.history_transfer_prompt);
		// long totalSize = HistoryInfo.getTotalSendSize(mHistoryList);
		if (mSendType) {
			traffic.setText(getTrafficSpannabl());
		} else {
			traffic.setText(getSendingFrameSpannabl());
		}
		return convertView;
	}

	/**
	 * @param size
	 * @return
	 */
	private SpannableStringBuilder getSendingFrameSpannabl() {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(mContext.getString(R.string.histor_file_send_size));
		builder.append(TextUtil.fromString(FileUtil.formatFromByte(mSendSize),
				Color.parseColor("#cc8910")));
		builder.append(mContext.getString(R.string.histor_file_sendrecv_text));
		builder.append(mContext.getString(R.string.histor_file_recv_size));
		builder.append(TextUtil.fromString(FileUtil.formatFromByte(mRecvSize),
				Color.parseColor("#cc8910")));
		builder.append(mContext.getString(R.string.histor_file_sendrecv_text));
		return builder;
	}

	/**
	 * @param size
	 * @return
	 */
	private SpannableStringBuilder getTrafficSpannabl() {
		String size = FileUtil.formatFromByte(mSendSize + mRecvSize);
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(mContext.getString(R.string.history_transfer_prompt_1));
		builder.append(TextUtil.fromString(
				mContext.getString(R.string.history_transfer_prompt_2),
				Color.parseColor("#cc8910")));
		builder.append(mContext.getString(R.string.history_transfer_prompt_3));
		builder.append(TextUtil.fromString(size, Color.parseColor("#cc8910")));
		builder.append(mContext.getString(R.string.history_transfer_prompt_4));
		builder.append(TextUtil.fromString(size, Color.parseColor("#cc8910")));
		return builder;
	}

	private SpannableStringBuilder getStoraeSpannabl(int resId, String size) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(mContext.getString(resId));
		builder.append(TextUtil.fromString(size, Color.parseColor("#cc8910")));
		return builder;
	}

	private SpannableStringBuilder getSenderAndReceiverSpannable(
			HistoryInfo info) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (HistoryInfo.HistoryType.HISTORY_TYPE_RECV == info.historyType) {
			builder.append(mContext
					.getString(R.string.history_file_receive_from));
			builder.append(TextUtil.fromString(info.sender,
					Color.parseColor("#cc8910")));
		} else {
			builder.append(mContext.getString(R.string.history_file_send_to));
			builder.append(TextUtil.fromString(info.reciver,
					Color.parseColor("#cc8910")));
		}
		return builder;
	}

	private String getSenderOrReceiver(HistoryInfo info) {
	    if (HistoryInfo.HistoryType.HISTORY_TYPE_RECV == info.historyType) {
	        return info.sender;
	    }
		return mContext.getString(R.string.history_file_sender_me);
	}

	private Bitmap getUserAvatar(HistoryInfo info) {
		if (HistoryInfo.HistoryType.HISTORY_TYPE_RECV == info.historyType) {

		} else {
			String path = MyInfo.getInstance(mContext).getInfo().avatar;
			return UserAvatarChooser.getAvatar(path, mContext);
		}
		return null;
	}

	public final class ViewHolder {
		public ImageView avater;
		public TextView name;
		public ImageView fileType;
		public ImageView fileIcon;
		public TextView fileName;
		public TextView fileSize;
		public TextView fileOpeate;
		public TextView fileSender;
		public ImageView fileChecked;
		public ImageView fileUncheck;
		public ProgressBar progressBar;

		public ViewHolder(View view) {
			avater = (ImageView) view.findViewById(R.id.avatar);
			name = (TextView) view.findViewById(R.id.name);
			// fileType = (ImageView) view.findViewById(R.id.file_type);
			fileIcon = (ImageView) view.findViewById(R.id.file_icon);
			fileName = (TextView) view.findViewById(R.id.file_name);
			fileSize = (TextView) view.findViewById(R.id.file_size);
			fileSender = (TextView) view.findViewById(R.id.file_from);
			fileChecked = (ImageView) view.findViewById(R.id.history_check);
			fileUncheck = (ImageView) view.findViewById(R.id.history_uncheck);
			progressBar = (ProgressBar) view
					.findViewById(R.id.file_transfer_progress);
		}

		private void setViewVal(HistoryInfo info) {
			fileName.setText(info.filePath);
			fileSize.setText(FileUtil.formatFromByte(info.fileSize));
			name.setText(info.sender);
			fileSender.setText(getSenderAndReceiverSpannable(info));
			name.setText(getSenderOrReceiver(info));
			Bitmap bitmap = getUserAvatar(info);
			if (bitmap != null) {
				avater.setImageBitmap(bitmap);
			}
		}

		public void setData(HistoryInfo info) {
			setViewVal(info);
			setCheckMode();
			// If is check mode then we show the checkbox or not.
			if (isCheckMode) {
				if (mSelectList.contains(info)) {
					setChecked(true);
				} else {
					setChecked(false);
				}
			}
			if (HistoryInfo.Status.STATUS_TRANSFERING.equals(info.status)) {
				updateProgress(info);
			} else {
				if (progressBar == null) {
					return;
				}
				if (View.VISIBLE == progressBar.getVisibility()) {
					progressBar.setVisibility(View.GONE);
				}
			}
		}

		private void updateProgress(HistoryInfo info) {
			if (progressBar != null) {
				if (View.VISIBLE != progressBar.getVisibility()) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(info.getProgress());
				if (info.transferProgress == progressBar.getMax()) {
					progressBar.setVisibility(View.GONE);
					info.status = HistoryInfo.Status.STATUS_TRANSFERING_FINISH;
				}
			}
		}

		/**
		 * If is check mode we should hide the file type icon.
		 */
		public void setCheckMode() {
			if (isCheckMode) {
				fileUncheck.setVisibility(View.INVISIBLE);
				fileChecked.setVisibility(View.VISIBLE);
			} else {
				fileChecked.setVisibility(View.INVISIBLE);
				fileUncheck.setVisibility(View.INVISIBLE);
			}
		}

		private void setChecked(boolean falg) {
			if (falg) {
				fileChecked.setVisibility(View.VISIBLE);
				fileUncheck.setVisibility(View.INVISIBLE);
			} else {
				fileChecked.setVisibility(View.INVISIBLE);
				fileUncheck.setVisibility(View.VISIBLE);
			}
		}

	}

}
