package com.cloudsynch.quickshare.db;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.provider.BaseColumns;

public class HistoryTable extends Provider {
	public static final String TABLE_NAME = "history";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);

	public static final String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME + "(" + Columns._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.PATH
			+ " VARCHAR, " + Columns.DATE + " INTEGER, " + Columns.SENDER
			+ " VARCHAR, " + Columns.RECIVER + " VARCHAR,  " + Columns.TYPE
			+ " INTEGER, " + Columns.STATUS + " VARCHAR, " + Columns.RECV_SIZE
			+ " INTEGER, " + Columns.TOTAL_SIZE + " INTEGER " + ")";

	public static final String DEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	public static class Columns implements BaseColumns {
		public static final String PATH = "path";
		public static final String DATE = "date";
		public static final String SENDER = "sender";
		public static final String RECIVER = "reciver";
		public static final String TYPE = "type";
		public static final String STATUS = "status";
		public static final String RECV_SIZE = "recvSize";
		public static final String TOTAL_SIZE = "totalSize";

		public static final String[] CLOMNS_PRJECTION = { _ID, PATH, DATE,
				SENDER, RECIVER, TYPE, STATUS, RECV_SIZE, TOTAL_SIZE };

		public static final Map<String, String> CLOMN_MAP = new HashMap<String, String>();
		static {
			CLOMN_MAP.put(_ID, _ID);
			CLOMN_MAP.put(PATH, PATH);
			CLOMN_MAP.put(DATE, DATE);
			CLOMN_MAP.put(SENDER, SENDER);
			CLOMN_MAP.put(RECIVER, RECIVER);
			CLOMN_MAP.put(TYPE, TYPE);
			CLOMN_MAP.put(STATUS, STATUS);
			CLOMN_MAP.put(RECV_SIZE, RECV_SIZE);
			CLOMN_MAP.put(TOTAL_SIZE, TOTAL_SIZE);
		}
	}
}
