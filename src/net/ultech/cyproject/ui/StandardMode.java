package net.ultech.cyproject.ui;

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
import net.ultech.cyproject.utils.AbsActivity;
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

	private CYDbOpenHelper helper;
	private SQLiteDatabase db;
	private EditText etHuman;
	private TextView tvRobot;
	private Button btOK;
	private Button btRestart;
	private Button btFigure;
	private Button btHint;
	private Button btLog;
	private SharedPreferences sp;
	private boolean locked;
	
	private standardModeListener mCallback;

	private final int random_size = 12;
	private String textHuman;
	private String textRobot;
	private int level; // 从1到12
	File file;
	FileOutputStream fos;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("StandardMode", "onCreateView");
		View view = inflater.inflate(R.layout.standard_layout, null);
		helper = new CYDbOpenHelper(getActivity());
		db = helper.getReadableDatabase();
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
		sp = getActivity()
				.getSharedPreferences("setting", Context.MODE_PRIVATE);
		textHuman = sp.getString("st_savedTextHuman", "");
		textRobot = sp.getString("st_savedTextRobot", "坐井观天");
		level = sp.getInt("st_savedLevel", 1);
		etHuman.setText(textHuman);
		tvRobot.setText(textRobot);
		openFile();
		return view;
	}

	@Override
	public void onStop() {
		Log.d("StandardMode", "onStop");
		if (locked) {
			restart();
			locked = false;
		}
		Editor editor = sp.edit();
		editor.putString("st_savedTextHuman", textHuman);
		editor.putString("st_savedTextRobot", textRobot);
		editor.putInt("st_savedLevel", level);
		editor.commit();
		closeFile();
		db.close();
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (standardModeListener) activity;
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (resultCode == 200) {
//			textHuman = data.getStringExtra("hint");
//			etHuman.setText(textHuman);
//		}
//		super.onActivityResult(requestCode, resultCode, data);
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.st_bt_ok:
			textHuman = etHuman.getText().toString().trim();
			textRobot = tvRobot.getText().toString().trim();
			if (TextUtils.isEmpty(textHuman)) {
				Toast.makeText(getActivity(), "请输入成语", 1).show();
			} else {
				if (textRobot != null
						&& !TextUtils.isEmpty(textRobot)
						&& textHuman.charAt(0) != textRobot.charAt(textRobot
								.length() - 1)) {
					Toast.makeText(getActivity(), "输入的首字必须和所给词的末字相同", 1).show();
				} else if (textRobot.charAt(0) == textHuman.charAt(textHuman
						.length() - 1)) {
					Toast.makeText(getActivity(), "输入的末字不能与所给词的首字相同", 1).show();
				} else if (CYDbDAO.find(textHuman, db)) {
					writeFile("$" + "h" + "$" + textHuman + "$");
					String first = new String(
							new char[] { textHuman.charAt(textHuman.length() - 1) });
					List<WordInfoSpecial> candidate = CYDbDAO.findByFirst(
							first, db);
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
							Toast.makeText(getActivity(), "机器人已死，点重新开始拯救它", 1)
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
						Toast.makeText(getActivity(), "机器人已死，点重新开始拯救它", 1)
								.show();
					}
				} else {
					Toast.makeText(getActivity(), "该成语不在词典中", 1).show();
				}
			}
			break;
		case R.id.st_bt_figure:
//			Intent intent_query = new Intent(this, QueryMode.class);
//			intent_query.putExtra("word", tvRobot.getText().toString());
//			startActivity(intent_query);
			break;
		case R.id.st_bt_restart:
			restart();
			break;
		case R.id.st_bt_hint:
			if (textRobot == null && TextUtils.isEmpty(textRobot)) {
				Toast.makeText(getActivity(), "所给词汇尚为空，请点击重新开始", 1).show();
			}
			StandardModeHint standardModeHint = new StandardModeHint();
			mCallback.firstText(new String(new char[] { textRobot.charAt(textRobot
					.length() - 1) }), standardModeHint);
			FragmentManager fManager = getFragmentManager();
			fManager.beginTransaction()
					.add(R.id.main_content_frame, standardModeHint).commit();
			break;
		case R.id.st_bt_log:
			break;
		}
	}

	public void restart() {
		Random random = new Random();
		List<WordInfoSpecial> wordlist = new ArrayList<WordInfoSpecial>();
		for (int i = 0; i < random_size; ++i) {
			WordInfoSpecial word = CYDbDAO.findById(
					random.nextInt(db_size) + 1, db);
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
			file = new File(getActivity().getFilesDir(), "st.log");
			fos = new FileOutputStream(file, true);
		} catch (Exception e) {
			Toast.makeText(getActivity(), "日志读写失败", 1).show();
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

	public interface standardModeListener {
		public void firstText(String first, Fragment frag);
	}
}
