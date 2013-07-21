
package com.cloudsynch.quickshare.entity;

import java.io.Serializable;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.cloudsynch.quickshare.db.UserTable;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public String name;
    public String avatar;
    public String nickname;
    public String male;
    public String signture;
    public String block;
    public String status;
    public String data;
    public int type;
    public Bitmap avatarBitmap;

    public static UserInfo getUserInfoFromCursor(Cursor c) {
        UserInfo uInfo = null;
        if (c != null) {
            uInfo = new UserInfo();
            uInfo.id = c.getInt(c.getColumnIndex(UserTable.Columns._ID));
            uInfo.name = c.getString(c.getColumnIndex(UserTable.Columns.NAME));
            uInfo.avatar = c.getString(c.getColumnIndex(UserTable.Columns.AVATAR));
            uInfo.nickname = c.getString(c.getColumnIndex(UserTable.Columns.NICK_NAME));
            uInfo.male = c.getString(c.getColumnIndex(UserTable.Columns.MALE));
            uInfo.signture = c.getString(c.getColumnIndex(UserTable.Columns.SIGNTURE));
            uInfo.block = c.getString(c.getColumnIndex(UserTable.Columns.BLOCK));
            uInfo.status = c.getString(c.getColumnIndex(UserTable.Columns.STATUS));
            uInfo.type = c.getInt(c.getColumnIndex(UserTable.Columns.TYPE));
            uInfo.data = c.getString(c.getColumnIndex(UserTable.Columns.DATA));
        }
        return uInfo;
    }

    public static class MaleType {
        public static final String TYPE_MALE = "male";
        public static final String TYPE_FEMALE = "female";
    }

    public static class UserType {
        public static final int ME = 1;
        public static final int OTHER = 0;
    }
}
