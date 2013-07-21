
package com.cloudsynch.quickshare.db;

import android.database.Cursor;

public class DBManager {
    public DBManager() {
    }

    public interface Callbace {
        void onStrat();

        void onDoing();

        void onFinish(Cursor c);
    }
}
