package net.ultech.cyproject.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.ultech.cyproject.R;
import net.ultech.cyproject.bean.WordInfoSpecial;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.ui.fragment.HighRecord;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.BasicColorConstants;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import net.ultech.cyproject.utils.DatabaseHolder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChallengeMode extends AbsActivity implements OnClickListener {

	private SQLiteDatabase mDatabase;
	private SharedPreferences sp;

	private EditText etHuman;
	private TextView tvRobot;
	private Button btOK;
	private Button btRestart;
	private Button btExit;
	private TextView tvTime;
	private TextView tvStatus;
	private TextView tvLevel;
	private TextView tvUsername;
	private TextView tvCharged;
	private ProgressBar pbCharged;

	private final int db_size = 31851;
	private final int random_size = 12;
	private final int[] score_update = { 0, 5, 12, 20, 30, 45, 60, 75, 95, 120,
			150, 190, 250 };
	private int time_limit = 30;
	private int full_charged = 7;
	private final int success_plus = 10;
	private final int success_charged = 1;
	private String textHuman;
	private String textRobot;
	private int scoreHuman;
	private int charged;
	private String username;
	private int level;
	private int round;
	private int successAnswer;
	private boolean timerOn;
	private final int timerReact = 1;
	private int timeRemain;
	private TimeThread timer;
	private boolean alreadyIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ChallengeMode", "onCreate");
		mDatabase = DatabaseHolder.getDatabase();
		LayoutInflater inflater = LayoutInflater.from(this);
		final View view = inflater.inflate(R.layout.challenge_layout_dialog1,
				null);
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle(R.string.reminder_info_title);
		builder1.setView(view);
		builder1.setPositiveButton(R.string.start,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText etDialogUsername = (EditText) view
								.findViewById(R.id.ch_et_dialog1_username);
						username = etDialogUsername.getText().toString();
						sp = getSharedPreferences(
								Constants.PREFERENCE_FILE_NAME,
								Context.MODE_PRIVATE);
						if (sp.getString(PreferenceName.STRING_TIME_OR_LIFE,
								getString(R.string.life)).equals(R.string.time)) {
							System.out.println(R.string.time_first);
							time_limit = 60;
							full_charged = 4;
						}
						if (TextUtils.isEmpty(username))
							username = sp.getString(
									PreferenceName.STRING_DEFAULT_USERNAME,
									getString(R.string.null_username));
						if (username != null && !TextUtils.isEmpty(username))
							tvUsername.setText(Html.fromHtml("<b>"
									+ getString(R.string.user_info) + "</b>"
									+ username));
						else {
							tvUsername.setText(Html.fromHtml("<b>"
									+ getString(R.string.user_info) + "</b>"));
						}
						alreadyIn = true;
						completeRestart();
					}
				});
		builder1.setNegativeButton(R.string.back,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		builder1.create().show();

		setContentView(R.layout.challenge_layout);
		etHuman = (EditText) findViewById(R.id.ch_et_human);
		tvRobot = (TextView) findViewById(R.id.ch_tv_robot);
		btOK = (Button) findViewById(R.id.ch_bt_ok);
		btRestart = (Button) findViewById(R.id.ch_bt_restart);
		btExit = (Button) findViewById(R.id.ch_bt_exit);
		tvTime = (TextView) findViewById(R.id.ch_tv_time);
		tvStatus = (TextView) findViewById(R.id.ch_tv_status);
		tvLevel = (TextView) findViewById(R.id.ch_tv_level);
		tvUsername = (TextView) findViewById(R.id.ch_tv_username);
		tvCharged = (TextView) findViewById(R.id.ch_tv_charged);
		pbCharged = (ProgressBar) findViewById(R.id.ch_pb_charged);
		btOK.setOnClickListener(this);
		btRestart.setOnClickListener(this);
		btExit.setOnClickListener(this);
		pbCharged.setMax(full_charged);
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
	}

	@Override
	protected void onDestroy() {
		Log.d("ChallengeMode", "onDestroy");
		if (alreadyIn) {
			saveScore();
			timerOn = false;
			timer.interrupt();
		}
		try {
			HighRecord highRecord = (HighRecord) MainActivity.mainActivity
					.getFragmentById(FragmentList.HIGH_RECORD);
			highRecord.updateList();
			highRecord.listAdapter.notifyDataSetChanged();
		} catch (IOException e) {
			Toast.makeText(this, R.string.empty_record_reminder, 1).show();
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ch_bt_ok:
			textHuman = etHuman.getText().toString().trim();
			textRobot = tvRobot.getText().toString().trim();
			if (TextUtils.isEmpty(textHuman)) {
				Toast.makeText(this, R.string.please_type_in_the_word, 1)
						.show();
			} else {
				if (textHuman.charAt(0) != textRobot
						.charAt(textRobot.length() - 1)) {
					Toast.makeText(this, R.string.first_equal_last_error, 1)
							.show();
				} else if (textRobot.charAt(0) == textHuman.charAt(textHuman
						.length() - 1)) {
					Toast.makeText(this, R.string.last_diff_first_error, 1)
							.show();
				} else if (CYDbDAO.find(textHuman, mDatabase)) {
					++scoreHuman;
					++successAnswer;
					Toast.makeText(this, R.string.plus_one, 0).show();
					if (score_update[level] <= successAnswer) {
						++level;
						Toast.makeText(this, R.string.plus_five, 0).show();
					}
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
							else
								--i;
						}
						if (candidate2.isEmpty()) {
							btRestart.setText(R.string.next_round);
							Toast.makeText(this, "+" + success_plus, 0).show();
							Toast.makeText(this, R.string.next_round_reminder,
									1).show();
							charged += success_charged;
							if (charged > full_charged)
								charged = full_charged;
							timerOn = false;
							btOK.setClickable(false);
							scoreHuman += success_plus;
							setStatusAndLevel();
							updateTimeAppearance();
							updateProgressBar();
						} else {
							sortByCountOfLastChar(candidate2);
							WordInfoSpecial chosen = candidate2.get(level - 1);
							textRobot = chosen.getName();
							tvRobot.setText(textRobot);
							timer.interrupt();
							timer = new TimeThread();
							timeRemain = time_limit;
							updateTimeAppearance();
							timer.start();
							setStatusAndLevel();
						}
					} else {
						btRestart.setText(R.string.next_round);
						Toast.makeText(this, "+" + success_plus, 0).show();
						Toast.makeText(this, R.string.next_round_reminder, 1)
								.show();
						charged += success_charged;
						if (charged > full_charged)
							charged = full_charged;
						timerOn = false;
						btOK.setClickable(false);
						scoreHuman += success_plus;
						updateTimeAppearance();
						setStatusAndLevel();
						updateProgressBar();
					}
				} else {
					Toast.makeText(this, R.string.illegal_word_error, 1).show();
				}
			}
			break;
		case R.id.ch_bt_restart:
			if (btRestart.getText().toString()
					.equals(getString(R.string.restart))) {
				new AlertDialog.Builder(this)
						.setMessage(R.string.challenge_restart_reminder)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										saveScore();
										completeRestart();
									}
								})
						.setNegativeButton(R.string.back,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
			} else
				restart();
			break;
		case R.id.ch_bt_exit:
			exitDialog();
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			exitDialog();
		return super.onKeyDown(keyCode, event);
	}

	public void restart() {
		++round;
		Random random = new Random();
		List<WordInfoSpecial> wordlist = new ArrayList<WordInfoSpecial>();
		for (int i = 0; i < random_size; ++i) {
			WordInfoSpecial word = CYDbDAO.findById(
					random.nextInt(db_size) + 1, mDatabase);
			if (word.getCountOfLast() != 0)
				wordlist.add(word);
			else
				--i;
		}
		sortByCountOfLastChar(wordlist);
		WordInfoSpecial chosen = wordlist.get(level - 1);
		textRobot = chosen.getName();
		textHuman = "";
		tvRobot.setText(textRobot);
		etHuman.setText(textHuman);
		setStatusAndLevel();
		btRestart.setText(R.string.restart);
		btOK.setClickable(true);
		updateProgressBar();
		timeRemain = time_limit;
		updateTimeAppearance();
		timerOn = true;
		if (timer != null)
			timer.interrupt();
		timer = new TimeThread();
		timer.start();
	}

	public void completeRestart() {
		scoreHuman = 0;
		level = 1;
		round = 0;
		charged = full_charged;
		successAnswer = 0;
		restart();
	}

	public void sortByCountOfLastChar(List<WordInfoSpecial> list) {
		Collections.sort(list);
		Collections.reverse(list);
	}

	public void setStatusAndLevel() {
		tvStatus.setText(Html.fromHtml("<b>" + getString(R.string.score)
				+ "：</b>" + "<font color=\""
				+ BasicColorConstants.stringColorBlue + "\">"
				+ Integer.toString(scoreHuman) + "</font>"));
		tvLevel.setText(Html.fromHtml("<b>" + getString(R.string.level)
				+ "：</b>" + Integer.toString(level)
				+ getString(R.string.pref_level_unit) + "；"
				+ getString(R.string.round) + Integer.toString(round)));
	}

	public void updateTimeAppearance() {
		int second = timeRemain % 60;
		int minute = (timeRemain - second) / 60;
		String strSecond = Integer.toString(second);
		if (strSecond.length() == 1)
			strSecond = "0" + strSecond;
		else if (strSecond.length() == 0)
			strSecond = "00";
		String strMinute = Integer.toString(minute);
		if (strMinute.length() == 1)
			strMinute = "0" + strMinute;
		else if (strMinute.length() == 0)
			strMinute = "00";
		String str = strMinute + ":" + strSecond;
		tvTime.setText(str);
	}

	public void updateProgressBar() {
		pbCharged.setMax(full_charged);
		pbCharged.setProgress(charged);
		tvCharged.setText(Integer.toString(charged) + "/"
				+ Integer.toString(full_charged));
	}

	public void exitDialog() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.challenge_quit_reminder)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ChallengeMode.this.finish();
							}
						})

				.setNegativeButton(R.string.back,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();
	}

	private final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case timerReact:
				if (timeRemain == 0 && timerOn) {
					timerOn = false;
					textHuman = etHuman.getText().toString().trim();
					textRobot = tvRobot.getText().toString().trim();
					charged--;
					btOK.setClickable(false);
					if (charged != 0) {
						btRestart.setText(R.string.next_round);
						Toast.makeText(ChallengeMode.this,
								R.string.round_lose_proceed_reminder, 1).show();
						updateProgressBar();
					} else {
						btRestart.setText(R.string.restart);
						updateProgressBar();
						new AlertDialog.Builder(ChallengeMode.this)
								.setMessage(
										R.string.your_score_is
												+ Integer.toString(scoreHuman)
												+ R.string.challenge_game_over_reminder)
								.setPositiveButton(R.string.restart,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												saveScore();
												completeRestart();
											}
										})
								.setNegativeButton(R.string.back,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												ChallengeMode.this.finish();
											}
										}).show();
					}
				} else if (timeRemain != 0) {
					--timeRemain;
					updateTimeAppearance();
				}
			}
		};
	};

	public class TimeThread extends Thread {

		@Override
		public void run() {
			while (timerOn) {
				try {
					Thread.sleep(1000);
					Message message = new Message();
					message.what = timerReact;
					handler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	public void saveScore() {
		try {
			File file = new File(getFilesDir(), Constants.RECORD_FILE_NAME);
			String[] result;
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				byte[] buffer = new byte[8192];
				int length = 0;
				String rawResult = new String();
				while ((length = fis.read(buffer)) != -1)
					rawResult = rawResult + new String(buffer).trim();
				fis.close();
				if (!TextUtils.isEmpty(rawResult)) {
					result = rawResult.split("\\$");
				} else
					result = new String[] { "0" };
				rawResult = null;
			} else {
				result = new String[] { "0" };
			}

			int n = Integer.parseInt(result[0]);
			String[] modifiedResult = new String[2 * n + 3];
			modifiedResult[0] = Integer.toString(n + 1);
			for (int i = 1; i <= 2 * n + 2; i = i + 2) {
				if (i > 2 * n || Integer.parseInt(result[i + 1]) <= scoreHuman) {
					modifiedResult[i] = username;
					modifiedResult[i + 1] = Integer.toString(scoreHuman);
					i = i + 2;
					for (; i <= 2 * n + 2; i = i + 2) {
						modifiedResult[i] = result[i - 2];
						modifiedResult[i + 1] = result[i - 1];
					}
				} else {
					modifiedResult[i] = result[i];
					modifiedResult[i + 1] = result[i + 1];
				}
			}
			result = null;

			String doneResult = modifiedResult[0];
			for (int i = 1; i < modifiedResult.length; ++i) {
				doneResult = doneResult + "$" + modifiedResult[i];
			}
			modifiedResult = null;

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(doneResult.getBytes());
			fos.close();
		} catch (IOException e) {
			Toast.makeText(this, R.string.save_failure, 1).show();
			e.printStackTrace();
		}
	}
}
