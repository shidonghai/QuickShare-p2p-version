
package com.cloudsynch.quickshare.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;
import com.umeng.common.Log;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.UMShareMsg;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMInfoAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.exception.SocializeException;

public class ShareActivity extends Activity implements OnClickListener {
    public static final int MSG_LENGTH = 155;
    private Titlebar mTitle;
    private EditText mShareMsg;
    private TextView mMsgLength;
    private TextView mShareButton;
    private int mShareType;
    private UMSocialService mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
        initView();
        handleIntent(getIntent());
        initData();
    }

    private void initData() {
        mController = UMServiceFactory.getUMSocialService("", RequestType.SOCIAL);
        SocializeConfig confg = mController.getConfig();
        confg.setPlatforms(SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT, SHARE_MEDIA.RENREN);
    }

    private void handleIntent(Intent intent) {
        mShareType = intent.getIntExtra("type", 0);
        setTitle();
    }

    private void setTitle() {
        switch (mShareType) {
            case ShareDialog.TYPE_SINA:
                mTitle.setTitle(R.string.history_file_share_to_sina);
                break;
            case ShareDialog.TYPE_TENCENT:
                mTitle.setTitle(R.string.history_file_share_to_tencent);
                break;
            case ShareDialog.TYPE_WEIXIN:
                mTitle.setTitle(R.string.history_file_share_to_weixin);
                break;
            case ShareDialog.TYPE_RENREN:
                mTitle.setTitle(R.string.history_file_share_to_renren);
                break;

            default:
                break;
        }
    }

    private void initView() {
        mTitle = (Titlebar) findViewById(R.id.top);
        mTitle.setLeftImage(R.drawable.return_button);
        mTitle.setTitlebarClickListener(new TitlebarClickListener() {
            @Override
            public void onRightClick() {
            }

            @Override
            public void onLeftClick() {
                finish();
            }
        });
        mShareMsg = (EditText) findViewById(R.id.share_msg);
        mShareMsg.setSelection(mShareMsg.getText().length());
        mShareMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setMsgLength();
            }
        });

        mMsgLength = (TextView) findViewById(R.id.left_text_num);
        setMsgLength();
        mShareButton = (TextView) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(this);
    }

    public void setMsgLength() {
        mMsgLength.setText(getString(R.string.share_msg_left_num, MSG_LENGTH
                - mShareMsg.getText().length()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 使用SSO必须添加，指定获取授权信息的回调页面，并传给SDK进行处理
         */
        UMSsoHandler sinaSsoHandler = mController.getConfig().getSinaSsoHandler();
        if (sinaSsoHandler != null && requestCode == UMSsoHandler.DEFAULT_AUTH_ACTIVITY_CODE) {
            // sinaSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    private SHARE_MEDIA getShareMedia() {
        if (mShareType == ShareDialog.TYPE_SINA) {
            return SHARE_MEDIA.SINA;
        } else if (mShareType == ShareDialog.TYPE_TENCENT) {
            return SHARE_MEDIA.TENCENT;
        } else {
            return SHARE_MEDIA.RENREN;
        }
    }

    private void shareToWeibo() {
        if (mShareType == ShareDialog.TYPE_WEIXIN) {
        } else {
            SHARE_MEDIA type = getShareMedia();
            boolean isOauthed = UMInfoAgent.isOauthed(this, type);
            if (isOauthed) {
                doShareMsg(type);
            } else {
                // mController.getConfig().setSinaSsoHandler(new
                // SinaSsoHandler());
                mController.doOauthVerify(this, type, new UMAuthListener() {

                    @Override
                    public void onStart(SHARE_MEDIA arg0) {
                    }

                    @Override
                    public void onError(SocializeException arg0, SHARE_MEDIA arg1) {

                    }

                    @Override
                    public void onComplete(Bundle arg0, SHARE_MEDIA arg1) {
                        Toast.makeText(ShareActivity.this,
                                R.string.pi_user_weibo_bind_successful, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA arg0) {

                    }
                });
            }
        }
    }

    private void doShareMsg(SHARE_MEDIA media) {
        UMShareMsg msg = new UMShareMsg();
        msg.text = mShareMsg.getText().toString();
        mController.postShare(this, media, msg, new SnsPostListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(SHARE_MEDIA arg0, int eCode, SocializeEntity entity) {
                if (eCode == 200) {
                    Toast.makeText(ShareActivity.this, R.string.share_successful, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else {
                    Toast.makeText(ShareActivity.this, R.string.share_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mShareMsg.getText())) {
            Toast.makeText(this, R.string.share_msg_empty, Toast.LENGTH_SHORT).show();
        } else {
            shareToWeibo();
        }
    }
}
