package com.cloudsynch.quickshare.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cloudsynch.quickshare.BaseFragment;
import com.cloudsynch.quickshare.R;
import com.cloudsynch.quickshare.db.UserTable;
import com.cloudsynch.quickshare.entity.UserInfo;
import com.cloudsynch.quickshare.widgets.Titlebar;

/**
 * Created by Xiaohu on 13-6-24.
 */
public class WhiteListFragment extends BaseFragment implements Titlebar.TitlebarClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private WhiteListAdapter mWhiteListAdapter;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_white_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView whiteList = (ListView) view.findViewById(R.id.white_list);
        mWhiteListAdapter = new WhiteListAdapter(getActivity(), null, false);
        whiteList.setAdapter(mWhiteListAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLeftClick() {

    }

    @Override
    public void onRightClick() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = UserTable.Columns.BLOCK + " == 'no' and " + UserTable.Columns.TYPE +
                " <> " + UserInfo.UserType.ME;
        return new CursorLoader(getActivity(), UserTable.CONTENT_URI, null, selection, null,
                UserTable.Columns.NAME + " COLLATE LOCALIZED");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mWhiteListAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    private class WhiteListAdapter extends CursorAdapter {

        public WhiteListAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return LayoutInflater.from(context).inflate(R.layout.white_list_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(cursor.getString(cursor.getColumnIndex(UserTable.Columns.NAME)));
            final long id = cursor.getLong(cursor.getColumnIndex(UserTable.Columns._ID));

            Button delete = (Button) view.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    removeFromWhiteList(context, id);
                }
            });
        }

        private void removeFromWhiteList(Context context, long id) {
            // ContentValues values = new ContentValues();
            // values.put(UserTable.Columns.BLOCK, "yes");
            String selections = UserTable.Columns._ID + " = " + id;
            context.getContentResolver().delete(UserTable.CONTENT_URI, selections, null);
            notifyDataSetChanged();
        }
    }
}
