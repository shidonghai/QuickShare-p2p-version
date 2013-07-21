package com.cloudsynch.quickshare.resource.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.file.FileOperationFragment;
import com.cloudsynch.quickshare.resource.module.OperationDialogFragment;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.resource.module.ResourceDetailFactory;
import com.cloudsynch.quickshare.socket.SocketService;
import com.cloudsynch.quickshare.socket.promp.PromptActivity;
import com.cloudsynch.quickshare.socket.promp.PromptDialog;
import com.cloudsynch.quickshare.socket.transfer.FileInfo;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-13.
 */
public class ResourceDetailContainerFragment extends BaseFragment implements View.OnClickListener,
        AdapterView.OnItemLongClickListener, OperationDialogFragment.OnCompleteListener, FileOperationFragment.IFileOperationComplete, Titlebar.TitlebarClickListener {
    private LinearLayout mOperationLayout;

    protected ResourceCategory mCategory;

    private ResourceDetailBaseFragment mDetailFragment;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.resource_manager_detail, container, false);

        mOperationLayout = (LinearLayout) view.findViewById(R.id.operation_layout);
        mCategory = (ResourceCategory) getArguments().getSerializable(ResourceManager.RESOURCE_CATEGORY);
        if (null != mCategory) {
            Titlebar titlebar = (Titlebar) view.findViewById(R.id.title_bar);
            titlebar.setTitle(mCategory.category.getStringId());
            titlebar.setLeftImage(R.drawable.return_button);
            titlebar.setTitlebarClickListener(this);

            View send = view.findViewById(R.id.send);
            View copy = view.findViewById(R.id.copy);
            View cut = view.findViewById(R.id.cut);
            View rename = view.findViewById(R.id.rename);
            View delete = view.findViewById(R.id.delete);
            send.setOnClickListener(this);
            copy.setOnClickListener(this);
            cut.setOnClickListener(this);
            rename.setOnClickListener(this);
            delete.setOnClickListener(this);

            showCategoryFragment(mCategory);

        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showCategoryFragment(ResourceCategory category) {
        mDetailFragment =
                ResourceDetailFactory.
                        getDetailFragment(category.category);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ResourceManager.RESOURCE_CATEGORY,
                category);
        mDetailFragment.setArguments(bundle);
        mDetailFragment.setOnItemLongClickListener(this);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.resource_detail,
                        mDetailFragment).commit();
    }

    @Override
    public void onClick(View view) {
        List<File> files = mDetailFragment.mMultipleSelection.getSelectItems();
        if (null == files || files.size() == 0) {
            Toast.makeText(getActivity(), R.string.resource_manager_select_file,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (view.getId()) {
            case R.id.send:
                sendFile(files);
                break;
            case R.id.copy:
                if (!checkSDCard()) {
                    return;
                }
                showDestFolderFragment(FileOperationFragment.TYPE_COPY);
                break;
            case R.id.cut:
                if (!checkSDCard()) {
                    return;
                }
                showDestFolderFragment(FileOperationFragment.TYPE_CUT);
                break;
            case R.id.rename:
                showRenameDialog(files);
                break;
            case R.id.delete:
                showDialogFragment(OperationDialogFragment.DELETE, files);
                break;
            default:
                break;
        }
    }

    private void sendFile(List<File> files) {
        String event = EventConstant.TRANSPORT_TYPES.get(mDetailFragment.getCategory());
        EventManager.getInstance().onEvent(event);

        ArrayList<FileInfo> fileList = createFileList(files);

        Intent intent = new Intent();
        intent.setClass(getActivity(), SocketService.class);
        intent.putExtra(SocketService.CMD_CODE, SocketService.CMD_TRANSFER);
        intent.putExtra(SocketService.DATA, fileList);
        getActivity().startService(intent);

        mDetailFragment.mMultipleSelection.cancelMultipleSelect();
        mOperationLayout.setVisibility(View.GONE);
        
        showAnimation();
    }

	private void showAnimation() {
		Bundle bundle = new Bundle();
		bundle.putInt(PromptDialog.KEY_TYPE, PromptDialog.TYPE_SHOW_TRANSFER);
		Intent intent = new Intent();
		intent.putExtra("extra", bundle);
		intent.setClass(getActivity(), PromptActivity.class);
		startActivity(intent);
	}

	private ArrayList<FileInfo> createFileList(List<File> list) {
        ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
        for (File file : list) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.name = file.getName();
            fileInfo.path = file.getAbsolutePath();
            fileInfo.length = file.length();
            fileInfo.type = 0;
            fileList.add(fileInfo);
        }
        return fileList;
    }

    private boolean checkSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }

        Toast.makeText(getActivity(), R.string.resource_manager_sdcard_unavailable,
                Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean onBackEvent() {
        mOperationLayout.setVisibility(View.GONE);
        return mDetailFragment.onBackEvent();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!mDetailFragment.mMultipleSelection.isMultipleSelectMode()) {
            mDetailFragment.mMultipleSelection.startMultipleSelect();
            mOperationLayout.setVisibility(View.VISIBLE);
        }

        return false;
    }

    public void showRenameDialog(List<File> files) {
        if (files.size() > 1) {
            Toast.makeText(getActivity(), R.string.resource_manager_rename_one,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showDialogFragment(OperationDialogFragment.RENAME, files);

    }

    private void showDialogFragment(int type, List<File> files) {
        OperationDialogFragment fragment = new OperationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(OperationDialogFragment.OPERATION_TYPE, type);
        fragment.setArguments(bundle);
        fragment.setFiles(files);
        fragment.setOnCompleteListener(this);
        fragment.show(getFragmentManager(), "");
    }

    private void showFileDialogFragment(int type, File dest, List<File> files) {
        OperationDialogFragment fragment = new OperationDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(OperationDialogFragment.OPERATION_TYPE, type);
        fragment.setArguments(bundle);
        fragment.setFiles(files);
        fragment.setDestFolder(dest);
        fragment.setOnCompleteListener(this);
        fragment.show(getFragmentManager(), "");
    }

    private void showDestFolderFragment(int type) {
        FileOperationFragment fileOperationFragment = new FileOperationFragment();
        fileOperationFragment.setFileOperationCompleteListener(this);
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fileOperationFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.content,
                fileOperationFragment, ResourceManager.RESOURCE_CATEGORY).addToBackStack(null).commit();
    }

    @Override
    public void onComplete() {
        mDetailFragment.mMultipleSelection.cancelMultipleSelect();
        mDetailFragment.mMultipleSelection.onChange();
        mOperationLayout.setVisibility(View.GONE);
        ResourceManager.getInstance(getActivity()).notifyContentChanged();
    }

    @Override
    public void onCopyComplete(File path) {
        List<File> files = mDetailFragment.mMultipleSelection.getSelectItems();
        if (null == path || null == files || files.size() == 0) {
            return;
        }
        showFileDialogFragment(OperationDialogFragment.COPY, path, files);
    }

    @Override
    public void onCutComplete(File path) {
        List<File> files = mDetailFragment.mMultipleSelection.getSelectItems();
        if (null == path || null == files || files.size() == 0) {
            return;
        }
        showFileDialogFragment(OperationDialogFragment.CUT, path, files);
    }


    @Override
    public void onLeftClick() {
        if (mDetailFragment.mMultipleSelection.isMultipleSelectMode()) {
            mDetailFragment.mMultipleSelection.cancelMultipleSelect();
            mOperationLayout.setVisibility(View.GONE);
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onRightClick() {

    }
}
