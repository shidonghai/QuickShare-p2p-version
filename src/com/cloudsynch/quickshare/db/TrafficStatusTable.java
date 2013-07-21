
package com.cloudsynch.quickshare.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.cloudsynch.quickshare.entity.TrafficInfo;

public class TrafficStatusTable extends Provider {
    public static final String TABLE_NAME = "traffic";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    public static final String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + Columns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.DATE
            + " INTEGER, " + Columns.TYPE + " VARCHAR, " + Columns.DATA_TYPE
            + " VARCHAR, " + Columns.TOTAL + " INTEGER " + ")";

    public static final String DEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static class Columns implements BaseColumns {
        public static final String DATE = "date";
        public static final String TYPE = "type";
        public static final String DATA_TYPE = "datatype";
        public static final String TOTAL = "totalSize";

        public static final String[] CLOMNS_PRJECTION = {
                _ID, TYPE, DATE,
                DATA_TYPE, TOTAL
        };

        public static final Map<String, String> CLOMN_MAP = new HashMap<String, String>();
        static {
            CLOMN_MAP.put(_ID, _ID);
            CLOMN_MAP.put(TYPE, TYPE);
            CLOMN_MAP.put(DATE, DATE);
            CLOMN_MAP.put(DATA_TYPE, DATA_TYPE);
            CLOMN_MAP.put(TOTAL, TOTAL);
        }
    }

    public static ContentValues convertFromTrafficInfo(TrafficInfo info) {
        ContentValues cv = new ContentValues();
        if (info.id != 0) {
            cv.put(Columns._ID, info.id);
        }
        cv.put(Columns.DATE, info.date);
        cv.put(Columns.TYPE, info.type);
        cv.put(Columns.DATA_TYPE, info.dataType);
        cv.put(Columns.TOTAL, info.totalSize);
        return cv;
    }
}
