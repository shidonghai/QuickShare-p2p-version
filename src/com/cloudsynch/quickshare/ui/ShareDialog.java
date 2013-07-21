
package com.cloudsynch.quickshare.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cloudsynch.quickshare.R;

public class ShareDialog extends DialogFragment implements OnClickListener {
    public static final int TYPE_SINA = 0;
    public static final int TYPE_TENCENT = 1;
    public static final int TYPE_WEIXIN = 2;
    public static final int TYPE_RENREN = 3;

    private ViewGroup mSina;
    private ViewGroup mTencent;
    private ViewGroup mWeixin;
    private ViewGroup mRenren;

    public static ShareDialog getInstance() {
        ShareDialog dialog = new ShareDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.share_dialog, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mSina = (ViewGroup) view.findViewById(R.id.sina_panel);
        mTencent = (ViewGroup) view.findViewById(R.id.tencent_panel);
        //mWeixin = (ViewGroup) view.findViewById(R.id.weixin_panel);
        mRenren = (ViewGroup) view.findViewById(R.id.renren_panel);
        mSina.setOnClickListener(this);
        mTencent.setOnClickListener(this);
        //mWeixin.setOnClickListener(this);
        mRenren.setOnClickListener(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
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

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), ShareActivity.class);
        if (v == mSina) {
            intent.putExtra("type", TYPE_SINA);
        } else if (v == mTencent) {
            intent.putExtra("type", TYPE_TENCENT);
        } else if (v == mWeixin) {
            intent.putExtra("type", TYPE_WEIXIN);
        } else if (v == mRenren) {
            intent.putExtra("type", TYPE_RENREN);
        }
        getActivity().startActivity(intent);
        dismiss();
    }

}
