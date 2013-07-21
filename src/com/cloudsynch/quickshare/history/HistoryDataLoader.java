
package com.cloudsynch.quickshare.history;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;

import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.entity.HistoryInfo;

public class HistoryDataLoader implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private boolean isRunning;
    private String mSqlWhere;
    private String mSortParm;
    private IDataLoaderListener mListener;
    private long mRecvSize = 0;
    private long mSendSize = 0;
    private int mLoadType;
    private String mLoadStatus;

    public HistoryDataLoader(Context c, Handler h) {
        mContext = c;
        mHandler = h;
    }

    public HistoryDataLoader(Context c, IDataLoaderListener l) {
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
    }

    public void load(String status) {
        if (!isRunning) {
            mLoadStatus = status;
            // mSqlWhere = HistoryTable.Columns.STATUS + "='" + status + "'";
            new Thread(this).start();
        }
    }
    public void load(String status,int type,String sort) {
        if (!isRunning) {
            mLoadStatus = status;
            mLoadType = type;
            mSqlWhere = HistoryTable.Columns.TYPE + "=" + type;
            mSortParm = sort;
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        isRunning = true;
        List<HistoryInfo> list = new ArrayList<HistoryInfo>();
        if (mListener != null) {
            mListener.onStart();
        }
        Cursor cursor = null;
        try {
            if (mListener != null) {
                mListener.onLoading();
            }
            cursor = mContext.getContentResolver().query(
                    HistoryTable.CONTENT_URI, null, mSqlWhere, null, mSortParm);
            if (cursor != null && cursor.moveToFirst()) {
                clearSecvAndSentSize();
                do {
                    HistoryInfo hi = HistoryInfo.convertToHistoryInfo(cursor);
                    if (hi != null) {
                        if (mLoadType != 0) {
                            if (hi.historyType==mLoadType) {
                                list.add(hi);
                            }
                        } else {
                            list.add(hi);
                        }
                        setReceiveAndSentSize(hi);
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

    private HistoryInfo convertToHistoryInfo(Cursor cursor) {
        try {
            HistoryInfo info = new HistoryInfo();
            info.id = cursor.getInt(cursor
                    .getColumnIndex(HistoryTable.Columns._ID));
            info.filePath = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.PATH));
            info.date = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.DATE));
            info.sender = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.SENDER));
            info.reciver = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.RECIVER));
            info.historyType = cursor.getInt(cursor
                    .getColumnIndex(HistoryTable.Columns.TYPE));
            info.fileSize = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.TOTAL_SIZE));
            info.receSize = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.RECV_SIZE));
            info.status = cursor.getString(cursor
                    .getColumnIndex(HistoryTable.Columns.STATUS));
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setReceiveAndSentSize(HistoryInfo info) {
        if (info.historyType == HistoryInfo.HistoryType.HISTORY_TYPE_RECV) {
            mRecvSize += Long.parseLong(info.fileSize);
        } else {
            mSendSize += Long.parseLong(info.fileSize);
        }
    }

    public void clearSecvAndSentSize() {
        mRecvSize = 0;
        mSendSize = 0;
    }

    public long getRecvSize() {
        return mRecvSize;
    }

    public long getSentSize() {
        return mSendSize;
    }

    public long getTotalSize() {
        return mRecvSize + mSendSize;
    }

    public interface IDataLoaderListener {
        void onStart();

        void onLoading();

        void onFinish(List<HistoryInfo> list);
    }

}
