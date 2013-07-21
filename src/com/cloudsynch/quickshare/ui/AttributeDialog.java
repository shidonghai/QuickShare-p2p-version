
package com.cloudsynch.quickshare.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.TimeUtils;

public class AttributeDialog extends DialogFragment {
    private HistoryInfo mInfo;

    public AttributeDialog() {
    }

    public static AttributeDialog newInstance(HistoryInfo info) {
        AttributeDialog dialog = new AttributeDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInfo = (HistoryInfo) getArguments().getSerializable("info");
        setStyle(STYLE_NO_TITLE, getTheme());
        setStyle(STYLE_NO_FRAME, getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.file_attribute_dialog, null);
        initView(view);
        return view;
    }

    private void initView(View v) {

        ((TextView) v.findViewById(R.id.file_name)).setText(getString(R.string.history_file_name,
                mInfo.getFileName()));
        String fileType = getString(FileUtil.getFileType(mInfo.filePath));
        ((TextView) v.findViewById(R.id.file_type)).setText(getString(R.string.history_file_type,
                fileType));
        ((TextView) v.findViewById(R.id.file_dir)).setText(getString(R.string.history_file_dir,
                mInfo.filePath));
        ((TextView) v.findViewById(R.id.file_received_size)).setText(getString(
                R.string.history_file_received,
                FileUtil.formatFromByte(mInfo.receSize)));
        ((TextView) v.findViewById(R.id.file_total_size)).setText(getString(
                R.string.history_file_total_size,
                FileUtil.formatFromByte(mInfo.fileSize)));
        ((TextView) v.findViewById(R.id.file_receiver)).setText(getString(
                R.string.history_file_receiver, mInfo.reciver));
        ((TextView) v.findViewById(R.id.file_status)).setText(getString(
                R.string.history_file_status,
                HistoryInfo.convertStatus(getActivity(), mInfo.status)));
        ((TextView) v.findViewById(R.id.file_date)).setText(getString(R.string.history_file_date,
                TimeUtils.format(mInfo.date)));

        v.findViewById(R.id.file_comfirm).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(FragmentManager manager, DialogFragment dialog, String tagName) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag(tagName);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialog.show(ft, tagName);
    }

}
