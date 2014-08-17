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
import net.ultech.cyproject.R.id;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.bean.WordInfoSpecial;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.utils.AbsActivity;
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

	private CYDbOpenHelper helper;
	private SQLiteDatabase db;
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

		LayoutInflater inflater = LayoutInflater.from(this);
		final View view = inflater.inflate(R.layout.challenge_layout_dialog1,
				null);
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle("提示信息");
		builder1.setIcon(android.R.drawable.ic_dialog_info);
		builder1.setView(view);
		builder1.setPositiveButton("开始", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText etDialogUsername = (EditText) view
						.findViewById(R.id.ch_et_dialog1_username);
				username = etDialogUsername.getText().toString();
				sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
				if (sp.getString("ch_savedPrimary", "life").equals("time")) {
					System.out.println("时间优先");
					time_limit = 60;
					full_charged = 4;
				}
				if (TextUtils.isEmpty(username))
					username = sp.getString("ch_defaultUsername", "无名氏");
				if (username != null && !TextUtils.isEmpty(username))
					tvUsername.setText(Html.fromHtml("<b>用户信息：</b>" + username));
				else {
					tvUsername.setText(Html.fromHtml("<b>用户信息：</b>"));
				}
				alreadyIn = true;
				completeRestart();
			}
		});
		builder1.setNegativeButton("返回", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder1.create().show();

		setContentView(R.layout.challenge_layout);
		helper = new CYDbOpenHelper(this);
		db = helper.getReadableDatabase();
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
		if (alreadyIn) {
			saveScore();
			timerOn = false;
			timer.interrupt();
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
				Toast.makeText(this, "请输入成语", 1).show();
			} else {
				if (textHuman.charAt(0) != textRobot
						.charAt(textRobot.length() - 1)) {
					Toast.makeText(this, "输入的首字必须和所给词的末字相同", 1).show();
				} else if (textRobot.charAt(0) == textHuman.charAt(textHuman
						.length() - 1)) {
					Toast.makeText(this, "输入的末字不能与所给词的首字相同", 1).show();
				} else if (CYDbDAO.find(textHuman, db)) {
					++scoreHuman;
					++successAnswer;
					Toast.makeText(this, "+1", 0).show();
					if (score_update[level] <= successAnswer) {
						++level;
						Toast.makeText(this, "+5", 0).show();
					}
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
							else
								--i;
						}
						if (candidate2.isEmpty()) {
							btRestart.setText("下一回合");
							Toast.makeText(this, "+" + success_plus, 0).show();
							Toast.makeText(this, "你赢了，点下一回合", 1).show();
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
						btRestart.setText("下一回合");
						Toast.makeText(this, "+" + success_plus, 0).show();
						Toast.makeText(this, "您赢了，点下一回合", 1).show();
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
					Toast.makeText(this, "该成语不在词典中", 1).show();
				}
			}
			break;
		case R.id.ch_bt_restart:
			if (btRestart.getText().toString().equals("重新开始")) {
				new AlertDialog.Builder(this)
						.setMessage("您的成绩将被存档。是否真的要重新开始挑战模式？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										saveScore();
										completeRestart();
									}
								})
						.setNegativeButton("返回",
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
					random.nextInt(db_size) + 1, db);
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
		btRestart.setText("重新开始");
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
		tvStatus.setText(Html.fromHtml("<b>得分：</b>"
				+ "<font color=\"#00d2ff\">" + Integer.toString(scoreHuman)
				+ "</font>"));
		tvLevel.setText(Html.fromHtml("<b>难度：</b>" + Integer.toString(level)
				+ "级；第" + Integer.toString(round) + "回合"));
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
		new AlertDialog.Builder(this).setMessage("您的成绩将被存档。是否真的要退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ChallengeMode.this.finish();
					}
				})

				.setNegativeButton("返回", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

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
						btRestart.setText("下一回合");
						Toast.makeText(ChallengeMode.this, "抱歉，您输了，点击下一回合继续", 1)
								.show();
						updateProgressBar();
					} else {
						btRestart.setText("重新开始");
						updateProgressBar();
						new AlertDialog.Builder(ChallengeMode.this)
								.setMessage(
										"你的得分为"
												+ Integer.toString(scoreHuman)
												+ "，此成绩将被存档。点击重新开始开始新的一轮，点击返回返回主界面。")
								.setPositiveButton("重新开始",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												saveScore();
												completeRestart();
											}
										})
								.setNegativeButton("返回",
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
			File file = new File(getFilesDir(), "ch.record");
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
			System.out.println("Calculate successfully.");

			String doneResult = modifiedResult[0];
			for (int i = 1; i < modifiedResult.length; ++i) {
				doneResult = doneResult + "$" + modifiedResult[i];
			}
			modifiedResult = null;

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(doneResult.getBytes());
			fos.close();
		} catch (IOException e) {
			Toast.makeText(this, "存储失败", 1).show();
			e.printStackTrace();
		}
	}
}
