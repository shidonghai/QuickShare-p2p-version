package com.cloudsynch.quickshare.resource.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.logic.IMultipleSelection;
import com.cloudsynch.quickshare.resource.module.FileItem;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;

/**
 * Created by Xiaohu on 13-6-5.
 */
public class ResourceFileAdapter extends BaseAdapter implements
		IMultipleSelection {

	private ResourceCategory mCategory;

	private LayoutInflater mInflater;

	private ResourceManager mResourceManager;

	private boolean isMultipleMode;

	public Map<String, File> mSelectFiles = new HashMap<String, File>();

	private InitResourceTask mInitResourceTask;

	public ResourceFileAdapter(Context context, ResourceCategory category) {
		mCategory = category;
		mInflater = LayoutInflater.from(context);
		mResourceManager = ResourceManager.getInstance(context);
	}

	@Override
	public int getCount() {
		return mCategory.files.size();
	}

	@Override
	public FileItem getItem(int i) {
		return mCategory.files.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder holder;
		if (null == view) {
			view = mInflater.inflate(R.layout.category_item, viewGroup, false);
			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.type);
			holder.selected = (ImageView) view.findViewById(R.id.selected);
			holder.name = (TextView) view.findViewById(R.id.name);
			holder.size = (TextView) view.findViewById(R.id.size);
			holder.date = (TextView) view.findViewById(R.id.date);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		FileItem fileItem = mCategory.files.get(i);
		mResourceManager.bindView(holder, fileItem.file);
		holder.selected.setVisibility(isMultipleMode ? View.VISIBLE
				: View.INVISIBLE);
		holder.selected.setImageResource(mSelectFiles.containsKey(fileItem.file
				.getAbsolutePath()) ? R.drawable.selected_icon
				: R.drawable.selected_icon_no);

		return view;
	}

	public void setSelectedItem(File file) {
		mSelectFiles.put(file.getAbsolutePath(), file);
	}

	public void removeSelectedItem(File file) {
		mSelectFiles.remove(file.getAbsolutePath());
	}

	public void toggleFile(File file) {
		if (mSelectFiles.containsKey(file.getAbsolutePath())) {
			mSelectFiles.remove(file.getAbsolutePath());
		} else {
			mSelectFiles.put(file.getAbsolutePath(), file);
		}
	}

	@Override
	public void startMultipleSelect() {
		isMultipleMode = true;
		notifyDataSetChanged();
	}

	@Override
	public void cancelMultipleSelect() {
		isMultipleMode = false;
		mSelectFiles.clear();
		notifyDataSetChanged();
	}

	@Override
	public List<File> getSelectItems() {
		return new ArrayList<File>(mSelectFiles.values());
	}

	@Override
	public boolean isMultipleSelectMode() {
		return isMultipleMode;
	}

	@Override
	public void setMultipleSelectMode(boolean isSelectMode) {
		isMultipleMode = isSelectMode;
	}

	@Override
	public void selectAll(boolean isAll) {

	}

	@Override
	public void onChange() {
		if (null == mInitResourceTask) {
			mInitResourceTask = new InitResourceTask();
			mInitResourceTask.execute();
		}
	}

	public class ViewHolder {
		public ImageView icon;
		public ImageView selected;
		public TextView name;
		public TextView date;
		public TextView size;
	}

	private class InitResourceTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... objects) {
			mResourceManager.initResource(mInflater.getContext(), mCategory);
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			notifyDataSetChanged();
			mInitResourceTask = null;
		}
	}

	@Override
	public int getAllCount() {
		// TODO Auto-generated method stub
		return getCount();
	}

}
