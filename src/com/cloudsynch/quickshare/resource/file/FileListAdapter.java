package com.cloudsynch.quickshare.resource.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;

/**
 * Created by Xiaohu on 13-6-14.
 */
public class FileListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private List<File> mFolders;

    private File mCurrentFolder;

    public FileListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mFolders = new ArrayList<File>();
        mCurrentFolder = Environment.getExternalStorageDirectory();
        mFolders.add(mCurrentFolder);
    }

    @Override
    public int getCount() {
        return mFolders.size();
    }

    @Override
    public File getItem(int i) {
        return mFolders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (null == view) {
            view = mInflater.inflate(R.layout.file_operation_item, viewGroup, false);
            holder = new ViewHolder();
            holder.folder = (TextView) view.findViewById(R.id.folder);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.folder.setText(mFolders.get(i).getName());
        return view;
    }

    public void scanFolder(File path) {
        mFolders.clear();
        mCurrentFolder = path;
        File[] files = path.listFiles();
        if (null != files) {
            for (File file : files) {
                if (file.isDirectory()) {
                    mFolders.add(file);
                }
            }
        }

        notifyDataSetChanged();
    }

    public File getCurrentFolder() {
        return mCurrentFolder;
    }

    public void setCurrentFolder(File folder) {
        if (null != folder) {
            mCurrentFolder = folder;
            scanFolder(mCurrentFolder);
        }

    }

    private class ViewHolder {
        TextView folder;
    }
}
