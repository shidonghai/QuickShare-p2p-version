
package com.cloudsynch.quickshare.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.user.UserAvatarChooser;
import com.cloudsynch.quickshare.user.MyInfo;
import com.cloudsynch.quickshare.utils.FileUtil;

public class UserHeaderSetting extends FragmentActivity implements OnClickListener,
        OnItemClickListener {
    private TextView mGalleryPick;
    private ImageView mBackButton;
    private GridView mDefaultHeaderPick;
    private HeaderAdapter mAdapter;
    private List<HeaderInfo> mList = new ArrayList<UserHeaderSetting.HeaderInfo>();
    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.user_header_setting_layout);
        mBackButton = (ImageView) findViewById(R.id.user_back_button);
        mGalleryPick = (TextView) findViewById(R.id.choose_from_gallery);
        mDefaultHeaderPick = (GridView) findViewById(R.id.default_header);
        mBackButton.setOnClickListener(this);
        mGalleryPick.setOnClickListener(this);
        mDefaultHeaderPick.setOnItemClickListener(this);
        mAdapter = new HeaderAdapter();
        mDefaultHeaderPick.setAdapter(mAdapter);
        mInflater = LayoutInflater.from(this);
        getDefaultHeaderList();
    }

    private List<HeaderInfo> getDefaultHeaderList() {
        Map<String, Integer> headerMap = UserAvatarChooser.mUserDefaultAvatars;
        for (String key : headerMap.keySet()) {
            HeaderInfo info = new HeaderInfo();
            info.headerId = headerMap.get(key);
            info.headerPath = key;
            mList.add(info);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HeaderInfo info = mAdapter.getItem(position);
        Intent intent = new Intent();
        MyInfo.getInstance(getApplicationContext()).getInfo().avatarBitmap = null;
        MyInfo.getInstance(getApplicationContext()).getInfo().avatar = info.headerPath;
        MyInfo.getInstance(getApplicationContext()).updateUserInfo(UserHeaderSetting.this);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mBackButton) {
            finish();
        } else if (v == mGalleryPick) {
            doPickPhotoFromGallery();
        }
    }

    //
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = getPhotoPickIntent();
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
        }
    }

    @Override
    protected void onActivityResult(int req, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            final Bitmap photo = data.getParcelableExtra("data");
            MyInfo.getInstance(getApplicationContext()).getInfo().avatarBitmap = photo;
            savePhotoAndUpdate(photo);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Save the photo picked from gallery
     * 
     * @param bitmap
     */
    private void savePhotoAndUpdate(final Bitmap bitmap) {
        final File destFile = FileUtil.getOrCreateFile(FileUtil.INBOX_FILE,
                System.currentTimeMillis()
                        + "");
        Runnable run = new Runnable() {
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(destFile);
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    MyInfo.getInstance(getApplicationContext()).getInfo().avatar = destFile.getAbsolutePath();
                    MyInfo.getInstance(getApplicationContext()).updateUserInfo(UserHeaderSetting.this);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(run).start();
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

    static class HeaderInfo {
        String headerPath;
        int headerId;
    }

    private class HeaderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public HeaderInfo getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.user_header_pick_item, null);
            }
            ImageView iv = (ImageView) convertView.findViewById(R.id.gallery_item);
            HeaderInfo info = getItem(position);
            if (info.headerId > 0) {
                iv.setImageResource(info.headerId);
            }
            return convertView;
        }
    }

}
