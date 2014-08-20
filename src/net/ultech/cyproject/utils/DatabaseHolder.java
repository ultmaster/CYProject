package net.ultech.cyproject.utils;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseHolder {
    private static SQLiteDatabase mDatabase;

    private DatabaseHolder() {

    }

    public static void putDatabase(SQLiteDatabase database) {
        DatabaseHolder.mDatabase = database;
    }

    public static SQLiteDatabase getDatabase() {
        if (DatabaseHolder.mDatabase == null) {
            throw new IllegalStateException("Not put database yet");

        }
        return DatabaseHolder.mDatabase;
    }
}
