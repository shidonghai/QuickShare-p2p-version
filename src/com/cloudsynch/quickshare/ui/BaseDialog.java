
package com.cloudsynch.quickshare.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;

public class BaseDialog extends DialogFragment {

    private TextView mTitle;
    private ViewGroup mBody;

    public static BaseDialog getInstance() {
        BaseDialog dialog = new BaseDialog();
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
        View view = inflater.inflate(R.layout.base_dialog, null);
        initView(view);
        addBodyView(inflater);
        return view;
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setTitle(int titleRes) {
        mTitle.setText(titleRes);
    }

    public void addBodyView(LayoutInflater inflater) {
        View v = getBody(inflater);
        if (v != null) {
            mBody.addView(v);
        }
    }

    public View getBody(LayoutInflater inflater) {
        return null;
    }

    private void initView(View view) {
        mTitle = (TextView) view.findViewById(R.id.prompt_title);
        mBody = (ViewGroup) view.findViewById(R.id.body);
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
}
