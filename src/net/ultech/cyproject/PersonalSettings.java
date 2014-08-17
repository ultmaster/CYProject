package net.ultech.cyproject;

import java.io.File;

import net.ultech.cyproject.uiutils.MyEditTextPreference;
import net.ultech.cyproject.uiutils.NumberPickerPreference;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class PersonalSettings extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener {

	private int mLevel;
	private String mUsername;
	private String mTimeOrLife;
	// 每一个preference都做一个类变量，记得类变量加上m前缀，是个命名规范
	private SharedPreferences mSharedPref;
	private MyEditTextPreference mPrefUsername;
	private NumberPickerPreference mPrefLevel;
	private ListPreference mPrefTimeOrLife;
	private Preference mPrefEmptyLog;
	private Preference mPrefEmptyRecord;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.personal_preference);
		mSharedPref = getApplicationContext().getSharedPreferences("setting",
				Context.MODE_PRIVATE);
		Log.d("Settings", "菜单成功加载");

		/*
		 * 每一个选项都这样做就可以了 一定要做个常量类把你用到的菜单key啊，sharedPreference的名字都存起来
		 * 要不然那里错了都看不出来 记得一定要setOnPreferenceChangeListener，我之前蛋疼了好久
		 */
		mPrefUsername = (MyEditTextPreference) findPreference("text_username");
		mPrefUsername.setOnPreferenceChangeListener(this);
		mUsername = mSharedPref.getString("ch_defaultUsername", "无名氏");
		mPrefUsername.setNowText(mUsername);
		mPrefUsername.setHint("请输入默认用户名：（不能为空，不能含有$字符）");
		mPrefUsername.setSummary("目前：" + mUsername);

		mLevel = mSharedPref.getInt("st_savedLevel", 1);
		mPrefLevel = (NumberPickerPreference) findPreference("level_picker");
		mPrefLevel.setNowLevel(mLevel);
		mPrefLevel.setOnPreferenceChangeListener(this);
		mPrefLevel.setSummary("目前：" + mLevel + "级");

		mTimeOrLife = mSharedPref.getString("ch_savedPrimary", "life");
		mPrefTimeOrLife = (ListPreference) findPreference("list_timelife");
		mPrefTimeOrLife.setOnPreferenceChangeListener(this);
		mPrefTimeOrLife.setSummary(convertTimeOrLife(mTimeOrLife));
		mPrefTimeOrLife.setDefaultValue(mTimeOrLife);

		mPrefEmptyLog = (Preference) findPreference("empty_log");
		mPrefEmptyLog.setOnPreferenceClickListener(this);

		mPrefEmptyRecord = (Preference) findPreference("empty_record");
		mPrefEmptyRecord.setOnPreferenceClickListener(this);

		PreferenceScreen ps = getPreferenceScreen();
		setLayoutResource(ps);
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
					mSharedPref.edit()
							.putString("ch_defaultUsername", mUsername)
							.commit();
					preference.setSummary("目前：" + mUsername);
				} else
					Toast.makeText(getApplicationContext(), "输入不合法", 1).show();
			}
		} else if (preference == mPrefLevel) {
			if (mLevel != (Integer) newValue) {
				mLevel = (Integer) newValue;
				mSharedPref.edit().putInt("st_savedLevel", mLevel).commit();
				preference.setSummary("目前：" + mLevel + "级");
			}
		} else if (preference == mPrefTimeOrLife) {
			mTimeOrLife = (String) newValue;
			System.out.println(mTimeOrLife);
			mSharedPref.edit().putString("ch_savedPrimary", mTimeOrLife)
					.commit();
			preference.setSummary(convertTimeOrLife(mTimeOrLife));
		}
		return true;
	}

	private String convertTimeOrLife(String str) {
		if (str.equals("time")) {
			return "目前：时间优先";
		} else
			return "目前：生命值优先";
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mPrefEmptyLog) {
			new AlertDialog.Builder(this)
					.setMessage("此操作不可恢复。确定要删除日志？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(
											getApplicationContext()
													.getFilesDir(), "st.log");
									if (file.exists()) {
										file.delete();
										Toast.makeText(getApplicationContext(),
												"删除成功", 1).show();
									}
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		} else if (preference == mPrefEmptyRecord) {
			new AlertDialog.Builder(this)
					.setMessage("此操作不可恢复。确定要删除记录？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(
											getApplicationContext()
													.getFilesDir(), "ch.record");
									if (file.exists()) {
										file.delete();
										Toast.makeText(getApplicationContext(),
												"删除成功", 1).show();
									}
								}
							})
					.setNegativeButton("取消",
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