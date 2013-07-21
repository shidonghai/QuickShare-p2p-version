package com.cloudsynch.quickshare.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;

/**
 * Created by Xiaohu on 13-6-12.
 */
public class FAQFragment extends BaseFragment implements View.OnClickListener {

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
//        layout.setBackgroundResource(R.color.pi_bg);
        layout.setOrientation(LinearLayout.VERTICAL);

//        Titlebar titlebar = new Titlebar(getActivity());
//        titlebar.setTitle(R.string.settings_faq);
//        titlebar.setLeftImage(R.drawable.return_button);
//        layout.addView(titlebar);

        EditText editText = new EditText(getActivity());
        editText.setText(R.string.faq);
        editText.setBackgroundResource(R.color.pi_bg);
        editText.setCursorVisible(false);
        editText.setFocusable(false);
        layout.addView(editText);

        return layout;
    }

    @Override
    public void onClick(View view) {

    }
}
