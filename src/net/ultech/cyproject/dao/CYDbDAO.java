package net.ultech.cyproject.dao;

import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.bean.WordInfoComplete;
import net.ultech.cyproject.bean.WordInfoSpecial;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CYDbDAO {

	public static WordInfoSpecial findById(int id, SQLiteDatabase db) {
		Cursor cursor = db.query("CY", new String[] { "name", "last_count" }, "ID=?",
				new String[] { Integer.toString(id) }, null, null, null);
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex("name"));
		int lastCount = cursor.getInt(cursor.getColumnIndex("last_count"));
		WordInfoSpecial word = new WordInfoSpecial(name, lastCount);
		cursor.close();
		return word;
	}

	public static boolean find(String text, SQLiteDatabase db) {
		Cursor cursor = db.query("CY", null, "name=?", new String[] { text },
				null, null, null);
		boolean result;
		if (cursor.moveToNext())
			result = true;
		else {
			result = false;
		}
		cursor.close();
		return result;
	}

	public static List<WordInfoSpecial> findByFirst(String text,
			SQLiteDatabase db) {
		Cursor cursor = db.query("CY",
				new String[] { "name", "last_count" }, "first=?",
				new String[] { text }, null, null, null);
		List<WordInfoSpecial> result = new ArrayList<WordInfoSpecial>();
		while (cursor.moveToNext()) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			int lastCount = cursor.getInt(cursor.getColumnIndex("last_count"));
			WordInfoSpecial word = new WordInfoSpecial(name, lastCount);
			result.add(word);
		}
		cursor.close();
		return result;
	}

	public static int findByFirstCount(String text, SQLiteDatabase db) {
		Cursor cursor = db.query("CY", new String[] { "count" }, "first=?",
				new String[] { text }, null, null, null);
		int count = 0;
		if (cursor.moveToNext())
			count = cursor.getInt(cursor.getColumnIndex("count"));
		cursor.close();
		return count;
	}

	public static WordInfoComplete findComplete(String text, SQLiteDatabase db) {
		Cursor cursor = db.query("CY", null, "name=?", new String[] { text },
				null, null, null);
		WordInfoComplete word;
		if (!cursor.moveToNext())
			word = null;
		else {
			word = new WordInfoComplete(cursor.getString(cursor
					.getColumnIndex("name")), cursor.getString(cursor
					.getColumnIndex("first")), cursor.getInt(cursor
					.getColumnIndex("count")), cursor.getString(cursor
					.getColumnIndex("spell")), cursor.getString(cursor
					.getColumnIndex("content")), cursor.getString(cursor
					.getColumnIndex("derivation")), cursor.getString(cursor
					.getColumnIndex("samples")));
			if (word.getContent() == null)
				word.setContent("");
			if (word.getDerivation() == null)
				word.setDerivation("");
			if (word.getSamples() == null)
				word.setSamples("");
		}
		cursor.close();
		return word;
	}

}
