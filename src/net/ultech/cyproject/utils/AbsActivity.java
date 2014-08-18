package net.ultech.cyproject.utils;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

public class AbsActivity extends Activity {

	protected static SharedPreferences sp;
	protected static SQLiteDatabase mDatabase;
	protected static CYDbOpenHelper mHelper;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getApplicationContext().getSharedPreferences(
				Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

		String theme = sp.getString("appearance", "blueandgreen");
		if (theme.equals("blueandgreen"))
			setTheme(R.style.BlueAndGreenTheme);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
