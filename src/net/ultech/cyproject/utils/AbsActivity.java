package net.ultech.cyproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

public class AbsActivity extends Activity {

	protected SharedPreferences sp;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*if (item.getItemId() == android.R.id.home) {
			this.finish();
		}*/
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getApplicationContext().getSharedPreferences("setting",
				Context.MODE_PRIVATE);
		getActionBar().setDisplayShowHomeEnabled(true);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
