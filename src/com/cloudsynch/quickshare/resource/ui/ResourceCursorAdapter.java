package com.cloudsynch.quickshare.resource.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudsynch.quickshare.QuickShareApplication;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.resource.ResourceManager;
import com.cloudsynch.quickshare.resource.logic.IMultipleSelection;
import com.cloudsynch.quickshare.resource.module.FileItem;
import com.cloudsynch.quickshare.resource.module.ResourceCategory;
import com.cloudsynch.quickshare.utils.FileUtil;
import com.cloudsynch.quickshare.utils.thumbnail.ImageWorker;

/**
 * Created by Xiaohu on 13-6-2.
 */
public class ResourceCursorAdapter extends CursorAdapter implements
        IMultipleSelection {

    private final int VIEW_TYPE_COUNT = 2;
    private final int SELECT_ALL = 0;
    private final int CHANGED = 1;
    public Map<Long, File> mSelectFiles = new HashMap<Long, File>();
    private ResourceCategory mCategory;
    private ResourceManager mResourceManager;
    private boolean isMultipleMode;
    private ImageWorker mWorker;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SELECT_ALL:
                    notifyDataSetChanged();
                    if (null != mFragmentHandler) {
                        mFragmentHandler.sendEmptyMessage(SELECT_ALL);
                    }
                    break;
                case CHANGED:
                    mResourceManager.notifyContentChanged();
                    break;
                default:
                    break;
            }
        }
    };
    private Handler mFragmentHandler;

    public ResourceCursorAdapter(Context context, Cursor c,
                                 ResourceCategory category) {
        super(context, c, false);
        mCategory = category;
        mResourceManager = ResourceManager.getInstance(context);

        mWorker = ((QuickShareApplication) context.getApplicationContext())
                .getImageWorker();
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view;
        switch (mCategory.category) {
            case AUDIO:
            case DOC:
            case ZIP:
            case APK:
                view = LayoutInflater.from(context).inflate(R.layout.category_item,
                        null);
                break;
            default:
                view = LayoutInflater.from(context).inflate(
                        R.layout.resource_manager_detail_item, null);
                break;
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        switch (mCategory.category) {
            case AUDIO:
            case DOC:
            case APK:
            case ZIP:
                bindListView(view, cursor);
                break;
            default:
                mResourceManager.bindView(view, cursor, mCategory);
                break;
        }

        ImageView selected = (ImageView) view.findViewById(R.id.selected);
        selected.setVisibility(isMultipleMode ? View.VISIBLE : View.INVISIBLE);

        String data = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.DATA));
        long id = cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media._ID));
        selected.setImageResource(mSelectFiles.containsKey(id) ? R.drawable.selected_icon
                : R.drawable.selected_icon_no);
        view.setTag(new FileItem(id, false, new File(data)));
    }

    public void toggleFile(FileItem item) {
        if (mSelectFiles.containsKey(item.id)) {
            mSelectFiles.remove(item.id);
        } else {
            mSelectFiles.put(item.id, item.file);
        }
    }

    private void bindListView(View view, Cursor cursor) {
        ImageView type = (ImageView) view.findViewById(R.id.type);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView size = (TextView) view.findViewById(R.id.size);
        TextView date = (TextView) view.findViewById(R.id.date);
        String data = cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.DATA));

        switch (mCategory.category) {
            case AUDIO:
                type.setImageResource(R.drawable.category_music_icon);
                break;
            case DOC:
                type.setImageResource(R.drawable.category_file_icon);
                break;
            case ZIP:
                type.setImageResource(R.drawable.category_compress_icon);
                break;
            case APK:
                // type.setImageResource(R.drawable.category_install_icon);
                mResourceManager.loadAppIcon(data, type);

                break;
            default:
                break;
        }

        File file = new File(data);
        name.setText(file.exists() ? file.getName() : cursor.getString(cursor
                .getColumnIndex(MediaStore.Audio.Media.TITLE)));
        size.setText(FileUtil.formatFromByte(cursor.getLong(cursor
                .getColumnIndex(MediaStore.Audio.Media.SIZE))));
        date.setText(FileUtil.getLastModifyDateString(file));
    }

    @Override
    public void startMultipleSelect() {
        isMultipleMode = true;
        notifyDataSetChanged();
    }

    @Override
    public void cancelMultipleSelect() {
        isMultipleMode = false;
        mSelectFiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public List<File> getSelectItems() {
        return new ArrayList<File>(mSelectFiles.values());
    }

    @Override
    public boolean isMultipleSelectMode() {
        return isMultipleMode;
    }

    @Override
    public void setMultipleSelectMode(boolean isMultipleSelectMode) {
        isMultipleMode = isMultipleSelectMode;
    }

    @Override
    public void selectAll(boolean isAll) {
        final Cursor cursor = getCursor();
        if (null == cursor) {
            return;
        }

        mSelectFiles.clear();
        if (isAll) {
            new Thread() {
                @Override
                public void run() {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                            .moveToNext()) {
                        String data = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));
                        long id = cursor.getLong(cursor
                                .getColumnIndex(MediaStore.Audio.Media._ID));
                        mSelectFiles.put(id, new File(data));
                    }
                    mHandler.sendEmptyMessage(SELECT_ALL);
                }
            }.start();
        } else {
            mHandler.sendEmptyMessage(SELECT_ALL);
        }
    }

    @Override
    public void onChange() {

    }

    public void setHandler(Handler handler) {
        mFragmentHandler = handler;
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        notifyDataSetChanged();
        mHandler.removeMessages(CHANGED);
        mHandler.sendEmptyMessageDelayed(CHANGED, 500);
    }

    @Override
    public int getAllCount() {
        Cursor cursor = getCursor();
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }
}
