
package com.cloudsynch.quickshare.entity;

import java.io.Serializable;
import java.util.List;

import android.database.Cursor;

import com.cloudsynch.quickshare.db.TrafficStatusTable;

public class TrafficInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String type;
    public String date;
    public String dataType;
    public int totalSize;
    public long totalSentSize;
    public long totalRecvSize;
    public long allSize;

    public TrafficInfo() {
    }

    public static class Type {
        public static final String TYPE_RECEIVED = "recv";
        public static final String TYPE_SENT = "sent";
    }

    public static class DataType {
        public static final String DATA_WIFI = "wifi";
        public static final String DATA_GPRS = "gprs";
        public static final String DATA_WIFI_AP = "wifiAp";
    }

    public void convertFromCursor(Cursor c) {
        id = c.getInt(c.getColumnIndex(TrafficStatusTable.Columns._ID));
        type = c.getString(c.getColumnIndex(TrafficStatusTable.Columns.TYPE));
        date = c.getString(c.getColumnIndex(TrafficStatusTable.Columns.DATE));
        dataType = c.getString(c.getColumnIndex(TrafficStatusTable.Columns.DATA_TYPE));
        totalSize = c.getInt(c.getColumnIndex(TrafficStatusTable.Columns.TOTAL));
    }

    public void calculateRecvSendtSize(List<TrafficInfo> list) {
        for (TrafficInfo trafficInfo : list) {
            if (Type.TYPE_RECEIVED.equals(trafficInfo.type)) {
                totalRecvSize += trafficInfo.totalSize;
            } else {
                totalSentSize += trafficInfo.totalSize;
            }
            allSize += trafficInfo.totalSize;
        }
    }

}
