package com.cloudsynch.quickshare.resource.ui;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.logic.IMultipleSelection;
import com.cloudsynch.quickshare.resource.module.FileItem;
import com.cloudsynch.quickshare.utils.FileUtil;

/**
 * Created by Xiaohu on 13-6-5.
 */
public class ResourceDetailFileFragment extends ResourceDetailBaseFragment {

    private ResourceFileAdapter mAdapter;

    @Override
    public void initView(View view) {
        mAdapter = new ResourceFileAdapter(getActivity(), mCategory);
        mGridView.setNumColumns(1);
        mGridView.setVerticalSpacing(10);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public IMultipleSelection setMultipleSelection() {
        return mAdapter;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mMultipleSelection.isMultipleSelectMode()) {
            FileItem item = mAdapter.getItem(i);
            mAdapter.toggleFile(item.file);

            ImageView selected = (ImageView) view.findViewById(R.id.selected);
            selected.setImageResource(mAdapter.mSelectFiles.containsKey(item.file.getAbsolutePath()) ?
                    R.drawable.selected_icon : R.drawable.selected_icon_no);
        } else {
            File file = mAdapter.getItem(i).file;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);

            intent.setDataAndType(uri, FileUtil.getFileMimeType(file.getAbsolutePath()));
            startActivity(Intent.createChooser(intent, ""));
        }
    }
}
