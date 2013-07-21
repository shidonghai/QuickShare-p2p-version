package com.cloudsynch.quickshare.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.socket.model.UserManager;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.user.UserAvatarChooser;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.widgets.Titlebar;
import com.cloudsynch.quickshare.widgets.Titlebar.TitlebarClickListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMInfoAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.exception.SocializeException;

public class PersonalInformationActivity extends FragmentActivity implements
		OnClickListener,ConfrimDialog.IButtonOnclickListener {
	public static final int ACT_PICK_HEADER = 0;
	private ImageView mAvatar;
	private ViewGroup mAvatarSetting;
	private EditText mNickName;
	private ImageView mMaleCheck;
	private ImageView mMaleUncheck;
	private ImageView mFemaleCheck;
	private ImageView mFemaleUncheck;
	private EditText mSignture;
	private TextView mSinaWeibo;
	private TextView mTencentWeibo;
	private TextView mRenrenWeibo;

	private ContentResolver mResolver;
	private UserInfo mUserInfo;

	private String mAvatarPath;
	private Bitmap mAvatarBitmap;

    private SHARE_MEDIA mShareType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_information);
		initView();

		mResolver = getContentResolver();

		initData();
	}

	private void initView() {
		Titlebar titlebar = (Titlebar) findViewById(R.id.top);
		titlebar.setTitle(R.string.pi_title);
		titlebar.setLeftImage(R.drawable.return_button);
		titlebar.setRightImage(R.drawable.save_button);
		titlebar.setTitlebarClickListener(new TitlebarClickListener() {
			@Override
			public void onRightClick() {
				save();
				finish();
			}

			@Override
			public void onLeftClick() {
				finish();
			}
		});

		// Init views
		mAvatar = (ImageView) findViewById(R.id.avater);
		mAvatarSetting = (ViewGroup) findViewById(R.id.avater_panel);
		mNickName = (EditText) findViewById(R.id.user_name);
		mMaleCheck = (ImageView) findViewById(R.id.man_checked);
		mMaleUncheck = (ImageView) findViewById(R.id.man_unchecked);
		mFemaleCheck = (ImageView) findViewById(R.id.woman_checked);
		mFemaleUncheck = (ImageView) findViewById(R.id.woman_unchecked);
		mSignture = (EditText) findViewById(R.id.user_signture);
		mSinaWeibo = (TextView) findViewById(R.id.bind_sina_weibo);
		mTencentWeibo = (TextView) findViewById(R.id.bind_tencent_weibo);
		mRenrenWeibo = (TextView) findViewById(R.id.bind_renren_weibo);
		updateWeiboInfo();
		// Set listeners
		mAvatarSetting.setOnClickListener(this);
		mMaleCheck.setOnClickListener(this);
		mMaleUncheck.setOnClickListener(this);
		mFemaleCheck.setOnClickListener(this);
		mFemaleUncheck.setOnClickListener(this);
		mSinaWeibo.setOnClickListener(this);
		mTencentWeibo.setOnClickListener(this);
		mRenrenWeibo.setOnClickListener(this);

	}

	/**
	 * Get user info
	 */
	private void initData() {
		mUserInfo = MyInfo.getInstance().getInfo();
		mNickName.setText(mUserInfo.nickname);
		mSignture.setText(mUserInfo.signture);
		if (UserInfo.MaleType.TYPE_MALE.equals(mUserInfo.male)) {
			setMaleButonStatus(true);
		} else {
			setMaleButonStatus(false);
		}
		Bitmap bitmap = MyInfo.getInstance().getInfo().avatarBitmap;
		if (bitmap != null) {
			mAvatar.setImageBitmap(bitmap);
		} else {
			bitmap = UserAvatarChooser.getAvatar(mUserInfo.avatar,
					getBaseContext());
			if (bitmap != null) {
				mAvatar.setImageBitmap(bitmap);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mAvatarSetting) {
			doPickPhotoFromGallery();
		} else if (v == mFemaleCheck) {
			showMaleCheckButton(false);
		} else if (v == mFemaleUncheck) {
			showMaleCheckButton(false);
		} else if (v == mMaleCheck) {
			showMaleCheckButton(true);
		} else if (v == mMaleUncheck) {
			showMaleCheckButton(true);
		} else if (v == mSinaWeibo) {
			OauthedOrNot(SHARE_MEDIA.SINA);
		} else if (v == mTencentWeibo) {
			OauthedOrNot(SHARE_MEDIA.TENCENT);
		} else if (v == mRenrenWeibo) {
			OauthedOrNot(SHARE_MEDIA.RENREN);
		}
	}

	/**
	 * 绑定或解绑定账号
	 * 
	 * @param type
	 */
	private void OauthedOrNot(SHARE_MEDIA type) {
        mShareType = type;
		if (isOauthed(type)) {
            showConfrimDialog();
		} else {
			doOauthed(type);
		}
	}

	/**
	 * 是否已经授权
	 * 
	 * @param type
	 * @return
	 */
	private boolean isOauthed(SHARE_MEDIA type) {
		return UMInfoAgent.isOauthed(this, type);
	}

	/**
	 * 更新微博账号绑定情况
	 */
	private void updateWeiboInfo() {
		if (isOauthed(SHARE_MEDIA.SINA)) {
			mSinaWeibo.setText(R.string.weibo_binded_text);
			mSinaWeibo.setSelected(true);
		} else {
			mSinaWeibo.setText(R.string.weibo_bind_text);
			mSinaWeibo.setSelected(false);
		}
		if (isOauthed(SHARE_MEDIA.TENCENT)) {
			mTencentWeibo.setText(R.string.weibo_binded_text);
			mTencentWeibo.setSelected(true);
		} else {
			mTencentWeibo.setText(R.string.weibo_bind_text);
			mTencentWeibo.setSelected(false);
		}
		if (isOauthed(SHARE_MEDIA.RENREN)) {
			mRenrenWeibo.setText(R.string.weibo_binded_text);
			mRenrenWeibo.setSelected(true);
		} else {
			mRenrenWeibo.setText(R.string.weibo_bind_text);
			mRenrenWeibo.setSelected(false);
		}
	}

	/**
	 * 解除账号授权
	 * 
	 * @param type
	 */
	private void unOauthed(SHARE_MEDIA type) {
		UMServiceFactory.getUMSocialService("", RequestType.SOCIAL)
				.deleteOauth(this, type, new SocializeClientListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int arg0, SocializeEntity arg1) {
						Toast.makeText(PersonalInformationActivity.this,
								R.string.pi_user_weibo_unbind_successful,
								Toast.LENGTH_SHORT).show();
						updateWeiboInfo();
					}
				});
	}

	/**
	 * 授权微博账号
	 * 
	 * @param type
	 */
	private void doOauthed(SHARE_MEDIA type) {
		UMServiceFactory.getUMSocialService("", RequestType.SOCIAL)
				.doOauthVerify(this, type, new UMAuthListener() {

					@Override
					public void onStart(SHARE_MEDIA arg0) {
					}

					@Override
					public void onError(SocializeException arg0,
							SHARE_MEDIA arg1) {

					}

					@Override
					public void onComplete(Bundle arg0, SHARE_MEDIA arg1) {
						Toast.makeText(PersonalInformationActivity.this,
								R.string.pi_user_weibo_bind_successful,
								Toast.LENGTH_SHORT).show();
						updateWeiboInfo();
					}

					@Override
					public void onCancel(SHARE_MEDIA arg0) {

					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACT_PICK_HEADER && resultCode == RESULT_OK) {
			try {
				final Bitmap photo = data.getParcelableExtra("data");
				savePhotoAndUpdate(photo);
				mAvatar.setImageBitmap(photo);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, R.string.pi_user_set_header_failed,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * User Male info check button set
	 * 
	 * @param isMale
	 */
	private void showMaleCheckButton(boolean isMale) {
		if (isMale) {
			if (mMaleCheck.getVisibility() == View.VISIBLE) {
				setMaleButonStatus(false);
			} else {
				setMaleButonStatus(true);
			}
		} else {
			if (mFemaleCheck.getVisibility() == View.VISIBLE) {
				setMaleButonStatus(true);
			} else {
				setMaleButonStatus(false);
			}
		}
	}

	private void setMaleButonStatus(boolean isMale) {
		if (isMale) {
			mMaleCheck.setVisibility(View.VISIBLE);
			mMaleUncheck.setVisibility(View.GONE);
			mFemaleCheck.setVisibility(View.GONE);
			mFemaleUncheck.setVisibility(View.VISIBLE);
		} else {
			mMaleCheck.setVisibility(View.GONE);
			mMaleUncheck.setVisibility(View.VISIBLE);
			mFemaleCheck.setVisibility(View.VISIBLE);
			mFemaleUncheck.setVisibility(View.GONE);
		}
	}

	/**
	 * Save the user data
	 */
	private void save() {
		try {
			if (mAvatarPath != null) {
				File dir = new File(FileUtil.AVATAR_PATH);
				for (File f : dir.listFiles()) {
					f.delete();
				}
				File file = new File(mAvatarPath);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(file);
				mAvatarBitmap.compress(CompressFormat.PNG, 100, fos);
				mUserInfo.avatarBitmap = mAvatarBitmap;
				mUserInfo.avatar = mAvatarPath;
			}
			updateUserInfo();
			MyInfo.getInstance().updateUserInfo(getBaseContext(), mUserInfo);
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), R.string.pi_user_info_saved_error,
					Toast.LENGTH_LONG).show();
		}
	}

	private void updateUserInfo() {
		mUserInfo.name = mNickName.getText().toString();
		mUserInfo.nickname = mNickName.getText().toString();
		mUserInfo.signture = mSignture.getText().toString();
		mUserInfo.male = getMale();
	}

	private String getMale() {
		if (mMaleCheck.getVisibility() == View.VISIBLE) {
			return UserInfo.MaleType.TYPE_MALE;
		} else {
			return UserInfo.MaleType.TYPE_FEMALE;
		}
	}

	protected void doPickPhotoFromGallery() {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = getPhotoPickIntent();
			startActivityForResult(intent, ACT_PICK_HEADER);
		} catch (ActivityNotFoundException e) {
		}
	}

	/**
	 * get the photo with 80dip width and 80dip height
	 * 
	 * @return
	 */
	public static Intent getPhotoPickIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", true);
		return intent;

	}

	/**
	 * Save the photo picked from gallery
	 * 
	 * @param bitmap
	 */
	private void savePhotoAndUpdate(final Bitmap bitmap) {
		if (bitmap == null) {
			Toast.makeText(getApplicationContext(),
					R.string.pi_user_set_header_failed, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		File dir = new File(FileUtil.AVATAR_PATH);
		File file = new File(dir, System.currentTimeMillis() + "");
		mAvatarPath = file.getAbsolutePath();
		mAvatarBitmap = bitmap;
	}

    private void showConfrimDialog(){
        ConfrimDialog dialog=new ConfrimDialog();
        dialog.show(getSupportFragmentManager(),"confrimDialog");
    }

    @Override
    public void onConfrim() {
        unOauthed(mShareType);
    }

    @Override
    public void onCancel() {

    }
}
