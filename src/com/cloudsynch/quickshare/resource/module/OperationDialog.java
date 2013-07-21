package com.cloudsynch.quickshare.resource.module;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.cloudsynch.quickshare.R;

/**
 * Created by Xiaohu on 13-6-14.
 */
public class OperationDialog extends Dialog {
    public OperationDialog(Context context) {
        super(context);
    }

    public OperationDialog(Context context, int theme) {
        super(context, theme);
    }

    protected OperationDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
    }
}
