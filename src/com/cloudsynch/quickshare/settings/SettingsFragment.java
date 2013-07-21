package com.cloudsynch.quickshare.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * Created by Xiaohu on 13-5-27.
 */
public class SettingsFragment extends BaseFragment implements
        View.OnClickListener, Titlebar.TitlebarClickListener {

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Titlebar titlebar = (Titlebar) view.findViewById(R.id.title_bar);
        titlebar.setTitle(R.string.settings_title);
        titlebar.setLeftImage(R.drawable.list_icon);
        titlebar.setTitlebarClickListener(this);

        View score = view.findViewById(R.id.score);
        score.setOnClickListener(this);

        View feedback = view.findViewById(R.id.feedback);
        feedback.setOnClickListener(this);

        View checkVersion = view.findViewById(R.id.check_version);
        checkVersion.setOnClickListener(this);

        View faq = view.findViewById(R.id.faq);
        faq.setOnClickListener(this);

        View about = view.findViewById(R.id.about);
        about.setOnClickListener(this);

        View whiteList = view.findViewById(R.id.white_list);
        whiteList.setOnClickListener(this);

        ImageView voice = (ImageView) view.findViewById(R.id.voice_switch);
        voice.setTag(SettingManager.isVoiceNotify(getActivity()));
        voice.setOnClickListener(this);
        voice.setImageResource(SettingManager.isVoiceNotify(getActivity()) ? R.drawable.set_open_button
                : R.drawable.set_cloesd_button);

        ImageView auto_dis = (ImageView) view.findViewById(R.id.auto_switch);
        auto_dis.setTag(SettingManager.isAutoDisconnect(getActivity()));
        auto_dis.setOnClickListener(this);
        auto_dis.setImageResource(SettingManager
                .isAutoDisconnect(getActivity()) ? R.drawable.set_open_button
                : R.drawable.set_cloesd_button);

        ImageView check_version_switch = (ImageView) view
                .findViewById(R.id.check_version_switch);
        check_version_switch.setTag(SettingManager
                .isAutoCheckVersion(getActivity()));
        check_version_switch.setOnClickListener(this);
        check_version_switch.setImageResource(SettingManager
                .isAutoCheckVersion(getActivity()) ? R.drawable.set_open_button
                : R.drawable.set_cloesd_button);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.score:
                score();
                break;
            case R.id.feedback:
                startFeedback();
                break;
            case R.id.check_version:
                checkUpdate();
                break;
            case R.id.faq:
                statrDetailActivity(SettingDetailActivity.FAQ);
                break;
            case R.id.about:
                statrDetailActivity(SettingDetailActivity.ABOUT);
                break;
            case R.id.white_list:
                statrDetailActivity(SettingDetailActivity.WHITE_LIST);
                break;
            case R.id.voice_switch:
                toggleView(!(Boolean) view.getTag(), (ImageView) view,
                        SettingManager.VOICE_NOTIFY);
                break;
            case R.id.auto_switch:
                toggleView(!(Boolean) view.getTag(), (ImageView) view,
                        SettingManager.AUTO_DISCONNECT);
                break;
            case R.id.check_version_switch:
                toggleView(!(Boolean) view.getTag(), (ImageView) view,
                        SettingManager.CHECK_VERSION);
                break;
            default:
                break;
        }
    }

    UmengUpdateListener updateListener = new UmengUpdateListener() {
        @Override
        public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
            switch (updateStatus) {
                case 0: // has update
                    Log.i("--->", "callback result");
                    UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                    break;
                case 1: // has no update
                    Toast.makeText(getActivity(),
                            R.string.settings_check_version_no_update,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2: // none wifi
                    Toast.makeText(getActivity(),
                            R.string.settings_check_version_error_wifi,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 3: // time out
                    Toast.makeText(getActivity(),
                            R.string.settings_check_version_error,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 4: // is updating
                /*
                 * Toast.makeText(mContext, "updating...", Toast.LENGTH_SHORT)
				 * .show();
				 */
                    break;
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        UmengUpdateAgent.setUpdateListener(null);
    }

    private void score() {
        Intent intent = new Intent();
        Uri marketUri = Uri.parse("market://details?id="
                + getActivity().getPackageName());
        intent.setData(marketUri);
        startActivity(Intent.createChooser(intent, ""));
    }

    private void startFeedback() {
        FeedbackAgent agent = new FeedbackAgent(getActivity());
        agent.startFeedbackActivity();
    }

    private void checkUpdate() {
        com.umeng.common.Log.LOG = true;
        UmengUpdateAgent.setUpdateOnlyWifi(false); //
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(updateListener);

        UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {

            @Override
            public void OnDownloadEnd(int result) {
                Log.i("zxh", "download result : " + result);
                // Toast.makeText(getActivity(), "download result : " + result,
                // Toast.LENGTH_SHORT)
                // .show();
            }

        });

        UmengUpdateAgent.update(getActivity());

    }

    private void toggleView(boolean isChecked, ImageView view, String tag) {
        view.setImageResource(isChecked ? R.drawable.set_open_button
                : R.drawable.set_cloesd_button);
        view.setTag(isChecked);
        SettingManager.setPreferences(getActivity(), isChecked, tag);
    }

    private void statrDetailActivity(int type) {
        Intent detail = new Intent();
        detail.putExtra(SettingDetailActivity.DETAIL_TYPE, type);
        detail.setClass(getActivity(), SettingDetailActivity.class);
        startActivity(detail);
    }


    @Override
    public void onLeftClick() {
        showMenu();
    }

    @Override
    public void onRightClick() {

    }
}
