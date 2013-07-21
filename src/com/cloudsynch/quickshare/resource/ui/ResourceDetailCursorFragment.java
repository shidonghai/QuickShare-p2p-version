package com.cloudsynch.quickshare.resource.ui;

import java.io.File;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.logic.IMultipleSelection;
import com.cloudsynch.quickshare.resource.module.FileItem;
import com.cloudsynch.quickshare.utils.FileUtil;

/**
 * Created by Xiaohu on 13-6-2.
 */
public class ResourceDetailCursorFragment extends ResourceDetailBaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ResourceCursorAdapter mCursorAdapter;

    @Override
    public void initView(View view) {
        mCursorAdapter = new ResourceCursorAdapter(getActivity(), null, mCategory);
        mCursorAdapter.setHandler(mHandler);


        switch (mCategory.category) {
            case AUDIO:
            case DOC:
            case ZIP:
            case APK:
                mGridView.setNumColumns(1);
                mGridView.setVerticalSpacing(10);
                break;
            default:
                mGridView.setNumColumns(3);
                break;
        }

        mGridView.setAdapter(mCursorAdapter);
        getLoaderManager().initLoader(mCategory.category.getStringId(), null, this);
    }

    @Override
    public IMultipleSelection setMultipleSelection() {
        return mCursorAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri uri = null;
        String selection = null;
        switch (i) {
            case R.string.resource_manager_video:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case R.string.resource_manager_audio:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case R.string.resource_manager_photo:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.Audio.Media.SIZE + " >0";
                break;
            case R.string.resource_manager_apk:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = MediaStore.Files.FileColumns.DATA + " LIKE '%.apk' and " +
                            MediaStore.Files.FileColumns.SIZE + " > 0";
                }
                break;
            case R.string.resource_manager_doc:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = ResourceManager.buildDocSelection();
                }
                break;
            case R.string.resource_manager_zip:
                if (Build.VERSION.SDK_INT > 11) {
                    uri = MediaStore.Files.getContentUri("external");
                    selection = "(" + MediaStore.Files.FileColumns.MIME_TYPE + " == '"
                            + ResourceManager.ZipFileMimeType + "') and " +
                            MediaStore.Files.FileColumns.SIZE + " > 0";
                }
                break;
            default:
                break;
        }
        if (null != uri) {
            return new CursorLoader(getActivity(), uri, null, selection, null,
                    MediaStore.Video.Media.DISPLAY_NAME + " COLLATE LOCALIZED");
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursorAdapter.changeCursor(cursor);
        if (null != mOnItemClick) {
            mOnItemClick.onClick();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        FileItem item = (FileItem) view.getTag();
        if (mMultipleSelection.isMultipleSelectMode()) {
            mCursorAdapter.toggleFile(item);
            ImageView selected = (ImageView) view.findViewById(R.id.selected);
            selected.setImageResource(mCursorAdapter.mSelectFiles.containsKey(item.id) ?
                    R.drawable.selected_icon : R.drawable.selected_icon_no);
            if (null != mOnItemClick) {
                mOnItemClick.onClick();
            }
        } else {
            File file = item.file;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);

            intent.setDataAndType(uri, FileUtil.getFileMimeType(file.getAbsolutePath()));
            startActivity(Intent.createChooser(intent, ""));
        }
    }
}
