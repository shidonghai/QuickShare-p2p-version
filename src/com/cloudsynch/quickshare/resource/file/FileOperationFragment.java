package com.cloudsynch.quickshare.resource.file;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-14.
 */
public class FileOperationFragment extends BaseFragment implements AdapterView.OnItemClickListener, Titlebar.TitlebarClickListener, View.OnClickListener {

    public static final int TYPE_COPY = 0;

    public static final int TYPE_CUT = 1;

    private FileListAdapter mFileAdapter;

    private IFileOperationComplete mFileListener;

    private int mType;

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.file_operation_layout, container, false);
//    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_operation_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (null != bundle) {
            mType = bundle.getInt("type");
        }

        Titlebar titlebar = (Titlebar) view.findViewById(R.id.title_bar);
        titlebar.setLeftImage(R.drawable.return_button);
        titlebar.setTitlebarClickListener(this);
        TextView done = (TextView) view.findViewById(R.id.done);
        done.setOnClickListener(this);

        if (mType == TYPE_COPY) {
            titlebar.setTitle(R.string.resource_manager_copy_file);
            done.setText(R.string.resource_manager_copy_there);
        } else if (mType == TYPE_CUT) {
            titlebar.setTitle(R.string.resource_manager_cut_file);
            done.setText(R.string.resource_manager_cut_there);
        }


        ListView listView = (ListView) view.findViewById(R.id.folder_list);
        mFileAdapter = new FileListAdapter(getActivity());
        listView.setAdapter(mFileAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File file = mFileAdapter.getItem(i);
        mFileAdapter.scanFolder(file);
    }

    @Override
    public boolean onBackEvent() {
        File file = mFileAdapter.getCurrentFolder();
        if (null == file) {
            return false;
        }

        if (file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            getFragmentManager().popBackStack();
        } else {
            mFileAdapter.setCurrentFolder(file.getParentFile());
        }
        return true;
    }

    @Override
    public void onLeftClick() {
        onBackEvent();
    }

    @Override
    public void onRightClick() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.done:
                getFragmentManager().popBackStack();
                if (null != mFileListener) {
                    switch (mType) {
                        case TYPE_COPY:
                            mFileListener.onCopyComplete(mFileAdapter.getCurrentFolder());
                            break;
                        case TYPE_CUT:
                            mFileListener.onCutComplete(mFileAdapter.getCurrentFolder());
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setFileOperationCompleteListener(IFileOperationComplete listener) {
        mFileListener = listener;
    }


    public interface IFileOperationComplete {
        void onCopyComplete(File path);

        void onCutComplete(File path);
    }

}
