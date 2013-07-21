
package com.cloudsynch.quickshare.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.provider.BaseColumns;

public class DownloadTable extends Provider {
    public static final String TABLE_NAME = "download";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_NAME);

    public static final String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + Columns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.URL
            + " VARCHAR, " + Columns.DATE + " INTEGER, " + Columns.FILE_NAME
            + " VARCHAR, " + Columns.SAVE_PATH + " VARCHAR,  "
            + Columns.STATUS + " VARCHAR, "
            + Columns.RECEIVE_SIZE + " INTEGER, "
            + Columns.TOTAL_SIZE + " INTEGER " + ")";

    public static final String DEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static class Columns implements BaseColumns {
        public static final String URL = "url";
        public static final String FILE_NAME = "filename";
        public static final String SAVE_PATH = "path";
        public static final String DATE = "date";
        public static final String RECEIVE_SIZE = "receiveSize";
        public static final String TOTAL_SIZE = "totalSize";
        public static final String STATUS = "status";

        public static final String[] CLOMNS_PRJECTION = {
                _ID, URL, FILE_NAME,
                SAVE_PATH, DATE, RECEIVE_SIZE, TOTAL_SIZE, STATUS
        };

        public static final Map<String, String> CLOMN_MAP = new HashMap<String, String>();
        static {
            CLOMN_MAP.put(_ID, _ID);
            CLOMN_MAP.put(URL, URL);
            CLOMN_MAP.put(FILE_NAME, FILE_NAME);
            CLOMN_MAP.put(SAVE_PATH, SAVE_PATH);
            CLOMN_MAP.put(DATE, DATE);
            CLOMN_MAP.put(RECEIVE_SIZE, RECEIVE_SIZE);
            CLOMN_MAP.put(TOTAL_SIZE, TOTAL_SIZE);
            CLOMN_MAP.put(STATUS, STATUS);
        }
    }
}
