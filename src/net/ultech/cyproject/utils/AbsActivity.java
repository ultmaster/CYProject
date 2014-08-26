package net.ultech.cyproject.utils;

import java.io.IOException;
import java.util.HashMap;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.ChallengeMode;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class AbsActivity extends Activity {

	protected static SharedPreferences sp;
	protected static SQLiteDatabase mDatabase;
	protected static CYDbOpenHelper mHelper;
	protected static SoundPool soundPool;
	protected static HashMap<String, Integer> soundPoolHashMap;
	public MediaPlayer mPlayer;
	public boolean isPrepared;
	public boolean mUseMediaPlayer;
	public boolean mUseSfx;

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
		mUseMediaPlayer = sp.getBoolean(PreferenceName.BOOL_CLOSE_MUSIC, true) ? false
				: true;
		mUseSfx = sp.getBoolean(PreferenceName.BOOL_CLOSE_SFX, false) ? false
				: true;
		initializeBgm();

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		playBgm();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopBgm();
	}

	public void playSound(String id) {
		if (mUseSfx) {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			soundPool.play(soundPoolHashMap.get(id),
					am.getStreamVolume(AudioManager.STREAM_ALARM),
					am.getStreamVolume(AudioManager.STREAM_ALARM), 1, 0, 1.0f);
		}
	}

	public void initializeBgm() {
		if (mUseMediaPlayer) {
			mPlayer = MediaPlayer.create(this, R.raw.bgm);
			mPlayer.setLooping(true);
			isPrepared = true;
		}
	}

	public void playBgm() {
		if (mUseMediaPlayer) {
			Log.i("MediaPlayer", "START PLAYING");
			if (!isPrepared) {
				try {
					isPrepared = true;
					mPlayer.prepare();
				} catch (Exception e) {
					initializeBgm();
					try {
						mPlayer.prepare();
					} catch (IllegalStateException e1) {
					} catch (IOException e1) {
					}
				}
			}
			mPlayer.start();
		}
	}

	public void stopBgm() {
		try {
			Log.i("MediaPlayer", "STOP PLAYING");
			mPlayer.stop();
		} catch (Exception e) {
		}
		isPrepared = false;
	}
}
