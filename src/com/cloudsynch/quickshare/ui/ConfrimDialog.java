package com.cloudsynch.quickshare.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cloudsynch.quickshare.R;

/**
 * Created by Xiaohu on 13-7-20.
 */
public class ConfrimDialog extends DialogFragment {

    public static ConfrimDialog newInstance() {
        ConfrimDialog dialog = new ConfrimDialog();
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME,getTheme());
        setStyle(STYLE_NO_TITLE,getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.confrim_dialog_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        view.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                ((IButtonOnclickListener) getActivity()).onConfrim();
            }
        });

        view.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                ((IButtonOnclickListener) getActivity()).onCancel();
            }
        });
    }

    public interface IButtonOnclickListener {
        void onConfrim();

        void onCancel();
    }
}
