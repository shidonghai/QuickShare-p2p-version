package com.cloudsynch.quickshare.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-20.
 */
public class SettingDetailActivity extends FragmentActivity implements Titlebar.TitlebarClickListener {

    public static final String DETAIL_TYPE = "type";

    public static final int FAQ = 0;

    public static final int ABOUT = 1;

    public static final int WHITE_LIST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_detail);

        Titlebar titlebar = (Titlebar) findViewById(R.id.title_bar);
        titlebar.setLeftImage(R.drawable.return_button);
        titlebar.setTitlebarClickListener(this);

        int type = getIntent().getIntExtra(DETAIL_TYPE, 0);

        Fragment fragment = null;
        switch (type) {
            case FAQ:
                titlebar.setTitle(R.string.settings_faq);
                fragment = new FAQFragment();
                break;
            case ABOUT:
                titlebar.setTitle(R.string.settings_about);
                fragment = new AboutFragment();
                break;
            case WHITE_LIST:
                titlebar.setTitle(R.string.settings_white_list);
                fragment = new WhiteListFragment();
            default:
                break;
        }

        if (null != fragment) {
            getSupportFragmentManager().beginTransaction().add(R.id.detail_container,
                    fragment).commit();
        }

    }

    @Override
    public void onLeftClick() {
        finish();
    }

    @Override
    public void onRightClick() {

    }
}
