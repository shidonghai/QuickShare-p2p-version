
package com.cloudsynch.quickshare.history;

import java.io.File;
import java.util.List;

import android.content.ContentResolver;
import android.os.Handler;

import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.entity.HistoryInfo;
import com.cloudsynch.quickshare.ui.HistoryActivity;
import com.cloudsynch.quickshare.ui.HistoryFragment;

public class HistoryFileOpeartor implements Runnable {

    private ContentResolver mResolver;
    private Handler mHandler;
    private List<HistoryInfo> mList;
    private boolean mIsDeleteFile;

    public HistoryFileOpeartor(ContentResolver resolver, Handler handler) {
        mResolver = resolver;
        mHandler = handler;
    }

    public void delRecord(List<HistoryInfo> list, boolean delFile) {
        mList = list;
        mIsDeleteFile = delFile;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            mHandler.sendEmptyMessage(HistoryFragment.DELETE_FILE_START);
            for (int i = 0; i < mList.size(); i++) {
                HistoryInfo info = mList.get(i);
                mResolver.delete(HistoryTable.CONTENT_URI,
                        HistoryTable.Columns._ID + "=" + info.id,
                        null);
                if (mIsDeleteFile) {
                    File f = new File(info.filePath);
                    if (f.exists()) {
                        f.delete();
                    }
                }

            }
        } catch (Exception e) {
        } finally {
            mHandler.sendEmptyMessage(HistoryFragment.DELETE_FILE_FINISH);
        }
    }
}
