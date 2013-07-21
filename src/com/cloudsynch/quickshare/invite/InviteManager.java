package com.cloudsynch.quickshare.invite;

import java.io.File;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.invite.zeroflow.ZeroFlowActivity;
import com.cloudsynch.quickshare.ui.ShareActivity;
import com.cloudsynch.quickshare.utils.EventConstant;
import com.cloudsynch.quickshare.utils.EventManager;

/**
 * Created by Xiaohu on 13-5-26.
 */
public class InviteManager implements View.OnClickListener {

    public static final int TYPE_SINA = 0;

    public static final int TYPE_TENCENT = 1;

    public static final int TYPE_RENREN = 3;

    private View mInviteView;

    public InviteManager() {
    }

    public void registerManager(View inviteView) {
        if (null == inviteView) {
            return;
        }

        mInviteView = inviteView;

        mInviteView.findViewById(R.id.sms).setOnClickListener(this);
        mInviteView.findViewById(R.id.bluetooth).setOnClickListener(this);
        mInviteView.findViewById(R.id.zero).setOnClickListener(this);
        mInviteView.findViewById(R.id.sina).setOnClickListener(this);
        mInviteView.findViewById(R.id.tencent).setOnClickListener(this);
        mInviteView.findViewById(R.id.renren).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sms:
                EventManager.getInstance().onEvent(EventConstant.INVITE_SMS);
                smsInvite();
                break;
            case R.id.bluetooth:
                EventManager.getInstance().onEvent(EventConstant.INVITE_BLUETOOTH);
                bluetoothInvite();
                break;
            case R.id.zero:
                EventManager.getInstance().onEvent(EventConstant.INVITE_ZERO_FLOW);
                startZeroFlow();
                break;
            case R.id.sina:
                EventManager.getInstance().onEvent(EventConstant.INVITE_SINA);
                startSNS(TYPE_SINA);
                break;
            case R.id.tencent:
                EventManager.getInstance().onEvent(EventConstant.INVITE_TENCENT);
                startSNS(TYPE_TENCENT);
                break;
            case R.id.renren:
                EventManager.getInstance().onEvent(EventConstant.INVITE_RENREN);
                startSNS(TYPE_RENREN);
                break;
            default:
                break;
        }
    }

    private void smsInvite() {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", mInviteView.getContext().getString(R.string.share_msg_default));
        mInviteView.getContext().startActivity(intent);
    }

    private void bluetoothInvite() {

        PackageManager manager = mInviteView.getContext().getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(mInviteView.getContext().getPackageName(), 0);
            File file = new File(info.sourceDir);
            Log.d("zxh", info.sourceDir + "  " + file.exists());
            if (file.exists()) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.setClassName("com.android.bluetooth"
                        , "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
                        file));
                mInviteView.getContext().startActivity(intent);
            } else {
                Toast.makeText(mInviteView.getContext(), R.string.invite_bluetooth_error,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mInviteView.getContext(), R.string.invite_bluetooth_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startZeroFlow() {
        Intent intent = new Intent();
        intent.setClass(mInviteView.getContext(), ZeroFlowActivity.class);
        mInviteView.getContext().startActivity(intent);
    }

    private void startSNS(int type) {
        Intent intent = new Intent(mInviteView.getContext(), ShareActivity.class);
        intent.putExtra("type", type);
        mInviteView.getContext().startActivity(intent);
    }


}
