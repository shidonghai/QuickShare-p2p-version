
package com.cloudsynch.quickshare.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quickshare.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserTable.CREATE_DB_TABLE);
        db.execSQL(HistoryTable.CREATE_DB_TABLE);
        db.execSQL(TrafficStatusTable.CREATE_DB_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(UserTable.DEL_TABLE);
        db.execSQL(HistoryTable.DEL_TABLE);
        db.execSQL(TrafficStatusTable.DEL_TABLE);
        onCreate(db);
    }

}
