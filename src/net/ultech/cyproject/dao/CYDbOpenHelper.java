package net.ultech.cyproject.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import net.ultech.cyproject.R;

public class CYDbOpenHelper extends SQLiteOpenHelper {

	public CYDbOpenHelper(Context context, String filename) {
		super(context, filename, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
