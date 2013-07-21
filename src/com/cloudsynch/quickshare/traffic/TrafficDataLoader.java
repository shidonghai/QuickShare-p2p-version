
package com.cloudsynch.quickshare.traffic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.db.TrafficStatusTable;
import com.cloudsynch.quickshare.entity.TrafficInfo;

public class TrafficDataLoader implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private boolean isRunning;
    private String mSqlWhere;
    private IDataLoaderListener mListener;

    public TrafficDataLoader(Context c, Handler h) {
        mContext = c;
        mHandler = h;
    }

    public TrafficDataLoader(Context c, IDataLoaderListener l) {
        mContext = c;
        mListener = l;
    }

    public void setListener(IDataLoaderListener l) {
        mListener = l;
    }

    public void load() {
        if (!isRunning) {
            new Thread(this).start();
        }
        // for (int i = 0; i < 20; i++) {
        // ContentValues cv = new ContentValues();
        // cv.put(HistoryTable.Columns.PATH, "aa");
        // cv.put(HistoryTable.Columns.DATE, System.currentTimeMillis());
        // cv.put(HistoryTable.Columns.SENDER, "me");
        // cv.put(HistoryTable.Columns.RECIVER, "me");
        // cv.put(HistoryTable.Columns.TYPE, i % 2 + 1);
        // mContext.getContentResolver().insert(HistoryTable.CONTENT_URI, cv);
        // }
    }

    public void load(String status) {
        if (!isRunning) {
            mSqlWhere = HistoryTable.Columns.STATUS + "='" + status + "'";
            new Thread(this).start();
        }
        // for (int i = 0; i < 20; i++) {
        // ContentValues cv = new ContentValues();
        // cv.put(HistoryTable.Columns.PATH, "aa");
        // cv.put(HistoryTable.Columns.DATE, System.currentTimeMillis());
        // cv.put(HistoryTable.Columns.SENDER, "me");
        // cv.put(HistoryTable.Columns.RECIVER, "me");
        // cv.put(HistoryTable.Columns.TYPE, i % 2 + 1);
        // mContext.getContentResolver().insert(HistoryTable.CONTENT_URI, cv);
        // }
    }

    @Override
    public void run() {
        isRunning = true;
        List<TrafficInfo> list = new ArrayList<TrafficInfo>();
        if (mListener != null) {
            mListener.onStart();
        }
        Cursor cursor = null;
        try {
            if (mListener != null) {
                mListener.onLoading();
            }
            cursor = mContext.getContentResolver().query(
                    TrafficStatusTable.CONTENT_URI, null, mSqlWhere, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    TrafficInfo hi = new TrafficInfo();
                    hi.convertFromCursor(cursor);
                    if (hi != null) {
                        list.add(hi);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mListener != null) {
                mListener.onFinish(list);
            }
            if (cursor != null) {
                cursor.close();
            }
            isRunning = false;
        }
    }

    public interface IDataLoaderListener {
        void onStart();

        void onLoading();

        void onFinish(List<TrafficInfo> list);
    }

}
