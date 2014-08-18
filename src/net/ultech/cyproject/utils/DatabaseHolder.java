package net.ultech.cyproject.utils;

import android.database.sqlite.SQLiteDatabase;
import net.ultech.cyproject.R;

public class DatabaseHolder {
    private static SQLiteDatabase mDatabase;
    private static DatabaseHolder mHolder;

    private DatabaseHolder() {

    }

    public static void putDatabase(SQLiteDatabase database) {
        if (mHolder == null) {
            mHolder = new DatabaseHolder();
        }
        mHolder.mDatabase = database;
    }

    public static SQLiteDatabase getDatabase() {
        if (mHolder == null) {
            throw new RuntimeException("Not put database yet");
        }
        return mHolder.mDatabase;
    }
}
