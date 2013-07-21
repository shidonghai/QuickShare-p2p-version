
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
import com.cloudsynch.quickshare.utils.TimeUtils;

public class DownloadAlertDialog extends DialogFragment {
    TextView mShowMsg;
    TextView mConfirmButton;
    TextView mCancelButton;
    OnClickListener mListener;
    int msgID;

    public DownloadAlertDialog() {
    }

    public static DownloadAlertDialog newInstance(int showMsgID) {
        DownloadAlertDialog dialog = new DownloadAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("msg", showMsgID);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        setStyle(STYLE_NO_FRAME, getTheme());
        msgID=getArguments().getInt("msg");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.xunlei_install_dialog, null);
        initView(view);
        return view;
    }

    private void initView(View v) {

        mShowMsg = (TextView) v.findViewById(R.id.msg);
        mShowMsg.setText(msgID);
        mConfirmButton = (TextView) v.findViewById(R.id.confirm_button);
        mCancelButton = (TextView) v.findViewById(R.id.cancel_button);
        mConfirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(v);
                dismiss();
            }
        });
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    };

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
