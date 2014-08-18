package net.ultech.cyproject.ui.fragment;

import java.io.File;

import net.ultech.cyproject.R;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.R.xml;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.ui.preference.MyEditTextPreference;
import net.ultech.cyproject.ui.preference.NumberPickerPreference;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class PersonalSettings extends PreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {

	private int mLevel;
	private String mUsername;
	private String mTimeOrLife;
	private String mAppearance;

	private SharedPreferences mSharedPref;
	private MyEditTextPreference mPrefUsername;
	private NumberPickerPreference mPrefLevel;
	private ListPreference mPrefTimeOrLife;
	private ListPreference mPrefAppearance;
	private Preference mPrefEmptyLog;
	private Preference mPrefEmptyRecord;
	private MainActivity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		addPreferencesFromResource(R.xml.personal_preference);
		mSharedPref = mActivity.getSharedPreferences(
				Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		Log.d("Settings", "菜单成功加载");

		mPrefUsername = (MyEditTextPreference) findPreference(mActivity
				.getString(R.string.pref_key_text_username));
		mPrefUsername.setOnPreferenceChangeListener(this);
		mUsername = mSharedPref.getString(
				PreferenceName.STRING_DEFAULT_USERNAME,
				mActivity.getString(R.string.null_username));
		mPrefUsername.setNowText(mUsername);
		mPrefUsername.setHint(mActivity
				.getString(R.string.pref_username_dialog_hint));
		mPrefUsername.setSummary(getResources().getString(
				R.string.pref_at_present)
				+ mUsername);
		Log.d("Settings", "username成功加载");

		mLevel = mSharedPref.getInt(PreferenceName.INT_LEVEL, 1);
		mPrefLevel = (NumberPickerPreference) findPreference(mActivity
				.getString(R.string.pref_key_level_picker));
		mPrefLevel.setNowLevel(mLevel);
		mPrefLevel.setOnPreferenceChangeListener(this);
		mPrefLevel.setSummary(getResources()
				.getString(R.string.pref_at_present)
				+ Integer.toString(mLevel)
				+ getResources().getString(R.string.pref_level_unit));
		Log.d("Settings", "level成功加载");

		mTimeOrLife = mSharedPref.getString(PreferenceName.STRING_TIME_OR_LIFE,
				mActivity.getString(R.string.life));
		mPrefTimeOrLife = (ListPreference) findPreference(mActivity
				.getString(R.string.pref_key_list_timelife));
		mPrefTimeOrLife.setOnPreferenceChangeListener(this);
		mPrefTimeOrLife.setSummary(convertTimeOrLife(mTimeOrLife));
		mPrefTimeOrLife.setDefaultValue(mTimeOrLife);
		Log.d("Settings", "timeorlife成功加载");

		mAppearance = mSharedPref.getString(PreferenceName.STRING_APPEARANCE,
				"blueandgreen");
		mPrefAppearance = (ListPreference) findPreference("list_change_appearance");
		mPrefAppearance.setOnPreferenceChangeListener(this);
		mPrefAppearance.setSummary(convertAppearance(mAppearance));
		Log.d("Settings", "appearance成功加载");

		mPrefEmptyLog = (Preference) findPreference(mActivity
				.getString(R.string.pref_key_empty_log));
		mPrefEmptyLog.setOnPreferenceClickListener(this);
		Log.d("Settings", "emptylog成功加载");

		mPrefEmptyRecord = (Preference) findPreference(mActivity
				.getString(R.string.pref_key_empty_record));
		mPrefEmptyRecord.setOnPreferenceClickListener(this);
		Log.d("Settings", "emptyrecord成功加载");

		PreferenceScreen ps = getPreferenceScreen();
		setLayoutResource(ps);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private void setLayoutResource(Preference preference) {
		if (preference instanceof PreferenceScreen) {
			preference.setLayoutResource(R.layout.pref_screen);
			int cnt = ((PreferenceGroup) preference).getPreferenceCount();
			for (int i = 0; i < cnt; ++i) {
				Preference p = ((PreferenceScreen) preference).getPreference(i);
				setLayoutResource(p);
			}
		} else if (preference instanceof PreferenceCategory) {
			preference.setLayoutResource(R.layout.pref_category);
			int cnt = ((PreferenceCategory) preference).getPreferenceCount();
			for (int i = 0; i < cnt; ++i) {
				Preference p = ((PreferenceCategory) preference)
						.getPreference(i);
				setLayoutResource(p);
			}
		} else {
			if (preference.getKey().contains("empty")) {
				preference.setLayoutResource(R.layout.pref_item_highlight);
			} else {
				preference.setLayoutResource(R.layout.pref_item);
			}
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mPrefUsername) {
			if (newValue != null) {
				String tempUsername = (String) newValue;
				if (!TextUtils.isEmpty(tempUsername)
						&& !tempUsername.contains("$")) {
					mUsername = tempUsername;
					mSharedPref
							.edit()
							.putString(PreferenceName.STRING_DEFAULT_USERNAME,
									mUsername).commit();
					preference.setSummary(getResources().getString(
							R.string.pref_at_present)
							+ mUsername);
				} else
					Toast.makeText(mActivity, R.string.illegal_input, 1).show();
			}
		} else if (preference == mPrefLevel) {
			if (mLevel != (Integer) newValue) {
				mLevel = (Integer) newValue;
				mSharedPref.edit().putInt(PreferenceName.INT_LEVEL, mLevel)
						.commit();
				preference.setSummary(getResources().getString(
						R.string.pref_at_present)
						+ mLevel
						+ getResources().getString(R.string.pref_level_unit));
			}
		} else if (preference == mPrefTimeOrLife) {
			mTimeOrLife = (String) newValue;
			System.out.println(mTimeOrLife);
			mSharedPref.edit()
					.putString(PreferenceName.STRING_TIME_OR_LIFE, mTimeOrLife)
					.commit();
			preference.setSummary(convertTimeOrLife(mTimeOrLife));
		} else if (preference == mPrefAppearance) {
			mAppearance = (String) newValue;
			System.out.println(mAppearance);
			mSharedPref.edit()
					.putString(PreferenceName.STRING_APPEARANCE, mAppearance)
					.commit();
			preference.setSummary(convertAppearance(mAppearance));
			Toast.makeText(mActivity,
					mActivity.getString(R.string.is_going_to_restart), 1)
					.show();
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000);
						Message msg = new Message();
						msg.what = MainActivity.RECREATE_MSG;
						mActivity.handler.sendMessage(msg);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		return true;
	}

	private String convertTimeOrLife(String str) {
		if (str.equals(getResources().getString(R.string.time))) {
			return (mActivity.getString(R.string.pref_at_present) + getResources()
					.getString(R.string.time_first));
		} else
			return (mActivity.getString(R.string.pref_at_present) + getResources()
					.getString(R.string.life_first));
	}

	private String convertAppearance(String str) {
		if (str.equals("blueandgreen")) {
			return (mActivity.getString(R.string.pref_at_present) + mActivity
					.getString(R.string.blue_and_green_skin));
		}
		throw new RuntimeException("Skin not found");
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mPrefEmptyLog) {
			new AlertDialog.Builder(mActivity)
					.setMessage(R.string.unrecoverable_reminder_log)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(mActivity
											.getFilesDir(),
											Constants.LOG_FILE_NAME);
									if (file.exists()) {
										file.delete();
										Toast.makeText(mActivity,
												R.string.delete_successfully, 1)
												.show();
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		} else if (preference == mPrefEmptyRecord) {
			new AlertDialog.Builder(mActivity)
					.setMessage(R.string.unrecoverable_reminder_record)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(mActivity
											.getFilesDir(),
											Constants.RECORD_FILE_NAME);
									if (file.exists()) {
										file.delete();
										Toast.makeText(mActivity,
												R.string.delete_successfully, 1)
												.show();
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
		return true;
	}
}