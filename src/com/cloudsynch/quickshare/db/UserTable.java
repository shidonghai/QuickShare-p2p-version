
package com.cloudsynch.quickshare.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.cloudsynch.quickshare.entity.UserInfo;

public class UserTable extends Provider {
    public static final String TABLE_NAME = "user";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    public static final String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "(" + Columns._ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Columns.NAME
            + " VARCHAR, " + Columns.AVATAR + " VARCHAR, " + Columns.NICK_NAME
            + " VARCHAR, " + Columns.MALE + " VARCHAR,  " + Columns.SIGNTURE
            + " VAECHAR, " + Columns.BLOCK + " VARCHAR, " + Columns.STATUS + " INTEGER, "
            + Columns.DATA
            + " VAECHAR, "
            + Columns.TYPE
            + " INTEGER " + ")";

    public static final String DEL_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static class Columns implements BaseColumns {
        public static final String NAME = "name";
        public static final String AVATAR = "avata";
        public static final String NICK_NAME = "nickname";
        public static final String MALE = "male";
        public static final String SIGNTURE = "signture";
        public static final String BLOCK = "block";
        public static final String STATUS = "status";
        public static final String DATA = "data";
        public static final String TYPE = "type";

        public static final String[] CLOMNS_PRJECTION = {
                _ID, NAME, AVATAR,
                NICK_NAME, MALE, SIGNTURE, BLOCK, STATUS, DATA, TYPE
        };

        public static final Map<String, String> CLOMN_MAP = new HashMap<String, String>();
        static {
            CLOMN_MAP.put(_ID, _ID);
            CLOMN_MAP.put(NAME, NAME);
            CLOMN_MAP.put(AVATAR, AVATAR);
            CLOMN_MAP.put(NICK_NAME, NICK_NAME);
            CLOMN_MAP.put(MALE, MALE);
            CLOMN_MAP.put(SIGNTURE, SIGNTURE);
            CLOMN_MAP.put(BLOCK, BLOCK);
            CLOMN_MAP.put(STATUS, STATUS);
            CLOMN_MAP.put(DATA, DATA);
            CLOMN_MAP.put(TYPE, TYPE);
        }
    }

    public static ContentValues converUserToValues(UserInfo uInfo) {
        ContentValues cv = new ContentValues();
        if(uInfo.id!=0){
            cv.put(Columns._ID, uInfo.id);
        }
        cv.put(Columns.NAME, uInfo.name);
        cv.put(Columns.AVATAR, uInfo.avatar);
        cv.put(Columns.NICK_NAME, uInfo.nickname);
        cv.put(Columns.MALE, uInfo.male);
        cv.put(Columns.SIGNTURE, uInfo.signture);
        cv.put(Columns.BLOCK, uInfo.block);
        cv.put(Columns.STATUS, uInfo.status);
        cv.put(Columns.DATA, uInfo.data);
        cv.put(Columns.TYPE, uInfo.type);
        return cv;
    }
}
