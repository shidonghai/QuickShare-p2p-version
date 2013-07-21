
package com.cloudsynch.quickshare.entity;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.db.HistoryTable;
import com.cloudsynch.quickshare.utils.LogUtil;

public class HistoryInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public int historyType;
    public int fileType;
    public Bitmap peopleIcon;
    public Bitmap fileIcon;
    public boolean checked;
    public String filePath;
    public String fileSize;
    public String sender;
    public String reciver;
    public String date;
    public String receSize;
    public String costTime;
    public String status;
    public int transferProgress;
    public String fileName;

    public static class HistoryType {
        public static final int HISTORY_TYPE_SEND = 1;
        public static final int HISTORY_TYPE_RECV = 2;
    }

    public static class Status {
        public static final String STATUS_TRANSFERING = "transfering";
        public static final String STATUS_TRANSFERING_FINISH = "finish";
        public static final String STATUS_TRANSFERING_FAILED = "failed";
    }

    public HistoryInfo() {

    }

    /**
     * convert status to 
     * @param c
     * @param status
     * @return
     */
    public static String convertStatus(Context c, String status) {
        if (Status.STATUS_TRANSFERING.equals(status)) {
            return c.getString(R.string.histor_file_status_sending);
        } else if (Status.STATUS_TRANSFERING_FINISH.equals(status)) {
            return c.getString(R.string.histor_file_status_finish);
        } else {
            return c.getString(R.string.histor_file_status_failed);
        }
    }

    public static long getTotalSendSize(List<HistoryInfo> list) {
        long size = 0;
        for (int i = 0; i < list.size(); i++) {
            size += Long.parseLong(list.get(i).fileSize);
        }
        return size;
    }
    
    public static HistoryInfo convertToHistoryInfo(Cursor cursor) {
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

    public String getFileName() {
        File f = new File(filePath);
        return f.getName();
    }

    public int getProgress() {
        try {
            transferProgress = (int) (Long.valueOf(receSize) * 100 / Long
                    .valueOf(fileSize));
            LogUtil.e("historyInfo", "" + transferProgress);
        } catch (Exception e) {
            transferProgress = 0;
            status = Status.STATUS_TRANSFERING_FAILED;
        }
        return transferProgress;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HistoryInfo)) {
            return false;
        }
        HistoryInfo anther = (HistoryInfo) obj;
        return id == anther.id;
    }

    @Override
    public String toString() {
        return "HistoryInfo [id=" + id + ", historyType=" + historyType
                + ", fileType=" + fileType + ", peopleIcon=" + peopleIcon
                + ", fileIcon=" + fileIcon + ", checked=" + checked
                + ", filePath=" + filePath + ", fileSize=" + fileSize
                + ", sender=" + sender + ", reciver=" + reciver + ", date="
                + date + "]";
    }
}
