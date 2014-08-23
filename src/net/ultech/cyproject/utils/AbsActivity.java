package net.ultech.cyproject.utils;

import java.io.IOException;
import java.util.HashMap;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.ChallengeMode;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MenuItem;

public class AbsActivity extends Activity {

	protected static SharedPreferences sp;
	protected static SQLiteDatabase mDatabase;
	protected static CYDbOpenHelper mHelper;
	protected static SoundPool soundPool;
	protected static HashMap<String, Integer> soundPoolHashMap;
	protected MediaPlayer mPlayer;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (this instanceof ChallengeMode) {
				ChallengeMode cm = (ChallengeMode) this;
				cm.exitDialog();
			} else
				this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getApplicationContext().getSharedPreferences(
				Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		soundPool = new SoundPool(3, AudioManager.STREAM_ALARM, 0);
		soundPoolHashMap = new HashMap<String, Integer>();
		soundPoolHashMap.put("bell", soundPool.load(this, R.raw.bell, 1));
		soundPoolHashMap.put("congratulations",
				soundPool.load(this, R.raw.congratulations, 1));
		soundPoolHashMap.put("click", soundPool.load(this, R.raw.click, 1));
		mPlayer = MediaPlayer.create(this, R.raw.bgm);
		String theme = sp.getString("appearance", "blueandgreen");
		if (theme.equals("blueandgreen")) {
			setTheme(R.style.BlueAndGreenTheme);
			BasicColorConstants.setColor(this, R.style.BlueAndGreenTheme);
		} else if (theme.equals("yellowandorange")) {
			setTheme(R.style.YellowAndOrangeTheme);
			BasicColorConstants.setColor(this, R.style.YellowAndOrangeTheme);
		} else if (theme.equals("darkocean")) {
			setTheme(R.style.DarkOceanTheme);
			BasicColorConstants.setColor(this, R.style.DarkOceanTheme);
		} else {
			throw new IllegalStateException("Skin not found.");
		}

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		try {
			mPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.start();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mPlayer.stop();
	}

	public void playSound(String id) {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		soundPool.play(soundPoolHashMap.get(id),
				am.getStreamVolume(AudioManager.STREAM_ALARM),
				am.getStreamVolume(AudioManager.STREAM_ALARM), 1, 0, 1.0f);
	}
}
