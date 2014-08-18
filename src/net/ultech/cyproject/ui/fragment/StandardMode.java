package net.ultech.cyproject.ui.fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.ultech.cyproject.R;
import net.ultech.cyproject.R.id;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.bean.WordInfoSpecial;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import net.ultech.cyproject.utils.DatabaseHolder;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StandardMode extends Fragment implements OnClickListener {

	private final int db_size = 31851;

	private EditText etHuman;
	private TextView tvRobot;
	private Button btOK;
	private Button btRestart;
	private Button btFigure;
	private Button btHint;
	private Button btLog;
	private SharedPreferences sp;
	private boolean locked;
	private SQLiteDatabase mDatabase;

	private final int random_size = 12;
	private String textHuman;
	private String textRobot;
	private int level; // 从1到12
	File file;
	FileOutputStream fos;
	private MainActivity mActivity;

	public void setConfirmText(String text) {
		textHuman = text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("StandardMode", "onCreateView");
		mDatabase = DatabaseHolder.getDatabase();
		mActivity = (MainActivity) getActivity();
		View view = inflater.inflate(R.layout.standard_layout, null);
		etHuman = (EditText) view.findViewById(R.id.st_et_human);
		tvRobot = (TextView) view.findViewById(R.id.st_tv_robot);
		btOK = (Button) view.findViewById(R.id.st_bt_ok);
		btRestart = (Button) view.findViewById(R.id.st_bt_restart);
		btFigure = (Button) view.findViewById(R.id.st_bt_figure);
		btHint = (Button) view.findViewById(R.id.st_bt_hint);
		btLog = (Button) view.findViewById(R.id.st_bt_log);

		locked = false;
		btOK.setOnClickListener(this);
		btFigure.setOnClickListener(this);
		btRestart.setOnClickListener(this);
		btHint.setOnClickListener(this);
		btLog.setOnClickListener(this);
		etHuman.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_UP) {
					btOK.performClick();
					return true;
				}
				return false;
			}
		});
		sp = getActivity().getSharedPreferences(Constants.PREFERENCE_FILE_NAME,
				Context.MODE_PRIVATE);
		if (textHuman == null)
			textHuman = sp.getString(PreferenceName.STRING_ST_TEXT_HUMAN, "");
		textRobot = sp.getString(PreferenceName.STRING_ST_TEXT_ROBOT,
				getActivity().getString(R.string.first_word));
		level = sp.getInt(PreferenceName.INT_LEVEL, 1);
		etHuman.setText(textHuman);
		tvRobot.setText(textRobot);
		openFile();
		return view;
	}

	@Override
	public void onStart() {
		Log.d("StandardMode", "onStart");
		Bundle bundle = mActivity.mActivityStack.getBackBundle();
		if (bundle != null) {
			String text = bundle.getString("textHuman", "");
			if (!TextUtils.isEmpty(text)) {
				textHuman = text;
				etHuman.setText(textHuman);
			}
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		Log.d("StandardMode", "onStop");
		if (locked) {
			restart();
			locked = false;
		}
		Editor editor = sp.edit();
		editor.putString(PreferenceName.STRING_ST_TEXT_HUMAN, textHuman);
		editor.putString(PreferenceName.STRING_ST_TEXT_ROBOT, textRobot);
		editor.putInt(PreferenceName.INT_LEVEL, level);
		editor.commit();
		closeFile();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.st_bt_ok:
			textHuman = etHuman.getText().toString().trim();
			textRobot = tvRobot.getText().toString().trim();
			if (TextUtils.isEmpty(textHuman)) {
				Toast.makeText(getActivity(), R.string.please_type_in_the_word,
						1).show();
			} else {
				if (textRobot != null
						&& !TextUtils.isEmpty(textRobot)
						&& textHuman.charAt(0) != textRobot.charAt(textRobot
								.length() - 1)) {
					Toast.makeText(getActivity(),
							R.string.first_equal_last_error, 1).show();
				} else if (textRobot.charAt(0) == textHuman.charAt(textHuman
						.length() - 1)) {
					Toast.makeText(getActivity(),
							R.string.last_diff_first_error, 1).show();
				} else if (CYDbDAO.find(textHuman, mDatabase)) {
					writeFile("$" + "h" + "$" + textHuman + "$");
					String first = new String(
							new char[] { textHuman.charAt(textHuman.length() - 1) });
					List<WordInfoSpecial> candidate = CYDbDAO.findByFirst(
							first, mDatabase);
					if (candidate.size() != 0) {
						List<WordInfoSpecial> candidate2 = new ArrayList<WordInfoSpecial>();
						Random random = new Random();
						for (int i = 0; i < random_size; ++i) {
							int r = random.nextInt(candidate.size());
							WordInfoSpecial word = candidate.get(r);
							String wordName = word.getName();
							if (word.getCountOfLast() != 0
									&& wordName.charAt(wordName.length() - 1) != textHuman
											.charAt(0))
								candidate2.add(candidate.get(r));
							else {
								--i;
							}
						}
						if (candidate2.isEmpty()) {
							locked = true;
							Toast.makeText(getActivity(), R.string.robot_query_failure, 1)
									.show();
						} else {
							sortByCountOfLastChar(candidate2);
							WordInfoSpecial chosen = candidate2.get(level - 1);
							textRobot = chosen.getName();
							tvRobot.setText(textRobot);
							writeFile("$" + "r" + "$" + textRobot + "$");
						}
					} else {
						locked = true;
						Toast.makeText(getActivity(), R.string.robot_query_failure, 1)
								.show();
					}
				} else {
					Toast.makeText(getActivity(), R.string.illegal_word_error,
							1).show();
				}
			}
			break;
		case R.id.st_bt_figure:
			Bundle bundleForQuery = new Bundle();
			bundleForQuery.putString("word", tvRobot.getText().toString());
			mActivity.mActivityStack.pushStack(bundleForQuery,
					mActivity.mFragments[FragmentList.QUERY_MODE]);
			mActivity.updateFragment();
			break;
		case R.id.st_bt_restart:
			restart();
			break;
		case R.id.st_bt_hint:
			if (textRobot == null && TextUtils.isEmpty(textRobot)) {
				Toast.makeText(getActivity(), R.string.empty_robot_given, 1).show();
			} else {
				Bundle bundleHint = new Bundle();
				bundleHint.putString(
						"first",
						new String(new char[] { textRobot.charAt(textRobot
								.length() - 1) }));
				StandardModeHint fragmentHint = new StandardModeHint();
				mActivity.mActivityStack.pushStack(bundleHint, fragmentHint);
				mActivity.updateFragment();
			}
			break;
		case R.id.st_bt_log:
			StandardModeLog fragmentLog = new StandardModeLog();
			mActivity.mActivityStack.pushStack(null, fragmentLog);
			mActivity.updateFragment();
			break;
		}
	}

	public void restart() {
		Random random = new Random();
		List<WordInfoSpecial> wordlist = new ArrayList<WordInfoSpecial>();
		for (int i = 0; i < random_size; ++i) {
			WordInfoSpecial word = CYDbDAO.findById(
					random.nextInt(db_size) + 1, mDatabase);
			if (word.getCountOfLast() != 0)
				wordlist.add(word);
			else {
				--i;
			}
		}
		sortByCountOfLastChar(wordlist);
		WordInfoSpecial chosen = wordlist.get(level - 1);
		textRobot = chosen.getName();
		textHuman = "";
		tvRobot.setText(textRobot);
		etHuman.setText(textHuman);
		writeFile("$" + "r" + "$" + textRobot + "$");
		locked = false;
	}

	public void sortByCountOfLastChar(List<WordInfoSpecial> list) {
		Collections.sort(list);
		Collections.reverse(list);
	}

	public void openFile() {
		try {
			file = new File(getActivity().getFilesDir(), Constants.LOG_FILE_NAME);
			fos = new FileOutputStream(file, true);
		} catch (Exception e) {
			Toast.makeText(getActivity(), R.string.log_io_failure, 1).show();
			e.printStackTrace();
		}
	}

	public void writeFile(String str) {
		try {
			fos.write(str.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeFile() {
		try {
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
