package com.cloudsynch.quickshare.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-12.
 */
public class AboutFragment extends BaseFragment {
    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Titlebar titlebar = (Titlebar) view.findViewById(R.id.title_bar);
        titlebar.setTitle(R.string.settings_about_title);
        titlebar.setLeftImage(R.drawable.return_button);

        TextView versionView = (TextView) view.findViewById(R.id.version);
        versionView.setText(String.format(getString(R.string.settings_version),
                getVersion()));
        view.findViewById(R.id.weibo).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://weibo.com/xiaoakuaichuan"));
                startActivity(intent);
            }
        });
        
       view.findViewById(R.id.tencent).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://t.qq.com/xiaoakuaichuan"));
                startActivity(intent);
            }
        });
    }

    private String getVersion() {
        String versionName = "";
        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            versionName = info.versionName;
            if (!TextUtils.isEmpty(versionName)) {
                return versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }
}
