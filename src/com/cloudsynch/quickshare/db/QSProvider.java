
package com.cloudsynch.quickshare.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class QSProvider extends ContentProvider {
    private DBHelper mDb;
    private static final UriMatcher mUriMatcher;

    private static final int USER = 1;
    private static final int USER_ID = 2;

    private static final int HISTORY = 3;
    private static final int HISTORY_ID = 4;

    private static final int TRAFFIC = 5;
    private static final int TRAFFIC_ID = 6;

    private static final int DOWNLOAD = 7;
    private static final int DOWNLOAD_ID = 8;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(Provider.AUTHORITY, UserTable.TABLE_NAME, USER);
        mUriMatcher.addURI(Provider.AUTHORITY, UserTable.TABLE_NAME + "/#", USER_ID);

        mUriMatcher.addURI(Provider.AUTHORITY, HistoryTable.TABLE_NAME, HISTORY);
        mUriMatcher.addURI(Provider.AUTHORITY, HistoryTable.TABLE_NAME + "/#", HISTORY_ID);

        mUriMatcher.addURI(Provider.AUTHORITY, TrafficStatusTable.TABLE_NAME, TRAFFIC);
        mUriMatcher.addURI(Provider.AUTHORITY, TrafficStatusTable.TABLE_NAME + "/#", TRAFFIC_ID);

        mUriMatcher.addURI(Provider.AUTHORITY, DownloadTable.TABLE_NAME, DOWNLOAD);
        mUriMatcher.addURI(Provider.AUTHORITY, DownloadTable.TABLE_NAME + "/#", DOWNLOAD_ID);
    }

    @Override
    public boolean onCreate() {
        mDb = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case USER:
                tableName = UserTable.TABLE_NAME;
                break;
            case HISTORY:
                tableName = HistoryTable.TABLE_NAME;
                break;
            case TRAFFIC:
                tableName = TrafficStatusTable.TABLE_NAME;
                break;
            case DOWNLOAD:
                tableName = DownloadTable.TABLE_NAME;
                break;
            case USER_ID:
            case HISTORY_ID:
            case TRAFFIC_ID:
            case DOWNLOAD_ID:
                tableName = uri.getPathSegments().get(0);
                selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException();
        }
        SQLiteDatabase db = mDb.getReadableDatabase();
        Cursor c = db.query(tableName, projection, selection, selectionArgs, null, null,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case USER:
            case HISTORY:
            case TRAFFIC:
            case DOWNLOAD:
                return Provider.CONTENT_TYPE;
            case USER_ID:
            case HISTORY_ID:
            case TRAFFIC_ID:
            case DOWNLOAD_ID:
                return Provider.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case USER:
                tableName = UserTable.TABLE_NAME;
                break;
            case HISTORY:
                tableName = HistoryTable.TABLE_NAME;
                break;
            case TRAFFIC:
                tableName = TrafficStatusTable.TABLE_NAME;
                break;
            case DOWNLOAD:
                tableName = DownloadTable.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = mDb.getReadableDatabase();
        long rowId = db.insert(tableName, null, values);
        if (rowId > 0) {
            Uri notifyUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return notifyUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case USER:
                tableName = UserTable.TABLE_NAME;
                break;
            case HISTORY:
                tableName = HistoryTable.TABLE_NAME;
                break;
            case TRAFFIC:
                tableName = TrafficStatusTable.TABLE_NAME;
                break;
            case DOWNLOAD:
                tableName = DownloadTable.TABLE_NAME;
                break;
            case USER_ID:
            case HISTORY_ID:
            case TRAFFIC_ID:
            case DOWNLOAD_ID:
                tableName = uri.getPathSegments().get(0);
                selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int count = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDb.getWritableDatabase();
        String tableName = null;
        switch (mUriMatcher.match(uri)) {
            case USER:
                tableName = UserTable.TABLE_NAME;
                break;
            case HISTORY:
                tableName = HistoryTable.TABLE_NAME;
                break;
            case TRAFFIC:
                tableName = TrafficStatusTable.TABLE_NAME;
                break;
            case DOWNLOAD:
                tableName = DownloadTable.TABLE_NAME;
                break;
            case USER_ID:
            case HISTORY_ID:
            case TRAFFIC_ID:
            case DOWNLOAD_ID:
                tableName = uri.getPathSegments().get(0);
                selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int count = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

}
