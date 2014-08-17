package net.ultech.cyproject.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.ui.StandardMode.standardModeListener;
import net.ultech.cyproject.utils.AbsActivity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AbsActivity implements
		StandardMode.standardModeListener,
		StandardModeHint.standardModeHintListener {

	private ListView lView;
	List<String> textList;
	private String databasePath;
	private String databaseName;
	private String helpPath;
	private String helpName;
	private SharedPreferences sp;
	private StandardMode standardMode;
	private AboutUs aboutUs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
		boolean firstUsed = sp.getBoolean("firstUsed", true);
		if (firstUsed) {
			new AlertDialog.Builder(this)
					.setMessage(
							"您好，欢迎使用成语接龙1.0测试版。希望您能使用愉快，并提出宝贵意见。联系方式请参见“关于我们”。谢谢。")
					.setPositiveButton("好的，立即开始！",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Editor editor = sp.edit();
									editor.putBoolean("firstUsed", false);
									editor.commit();
								}
							}).show();
		}

		databasePath = "/data/data/" + getPackageName() + "/databases/";
		databaseName = "cydb.db";
		if (!checkDatabaseExist()) {
			try {
				copyDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		helpPath = getFilesDir().getAbsolutePath() + "/";
		helpName = "help";
		if (!checkHelpExist()) {
			try {
				copyHelpFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		textList = new ArrayList<String>();
		textList.add("标准模式");
		textList.add("挑战模式");
		textList.add("查询词典");
		textList.add("高分记录");
		textList.add("个性设置");
		textList.add("请求帮助");
		textList.add("关于我们");

		lView = (ListView) findViewById(R.id.lv_main);
		lView.setAdapter(new myListAdapter());
		lView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				FragmentManager fm = getFragmentManager();
				FragmentTransaction ft;
				if (textList.get(position).equals("标准模式")) {
					standardMode = new StandardMode();
					ft = fm.beginTransaction();
					ft.replace(R.id.main_content_frame, standardMode);
					ft.commit();
				} else if (textList.get(position).equals("查询词典")) {
					Intent intent_dict = new Intent(MainActivity.this,
							QueryMode.class);
					startActivity(intent_dict);
				} else if (textList.get(position).equals("挑战模式")) {
					Intent intent_challenge = new Intent(MainActivity.this,
							ChallengeMode.class);
					startActivity(intent_challenge);
				} else if (textList.get(position).equals("高分记录")) {
					Intent intent_record = new Intent(MainActivity.this,
							HighRecord.class);
					startActivity(intent_record);
				} else if (textList.get(position).equals("个性设置")) {
					Intent intent_setting = new Intent(MainActivity.this,
							PersonalSettings.class);
					startActivity(intent_setting);
				} else if (textList.get(position).equals("请求帮助")) {
					Intent intent_help = new Intent(MainActivity.this,
							HelpActivity.class);
					startActivity(intent_help);
				} else if (textList.get(position).equals("关于我们")) {
					aboutUs = new AboutUs();
					ft = fm.beginTransaction();
					ft.replace(R.id.main_content_frame, aboutUs);
					ft.commit();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			FragmentManager fm = getFragmentManager();
			if (fm.getBackStackEntryCount() > 1)
				fm.popBackStack();
			else {
				this.finish();
			}
		}
		return true;
	}

	private class myListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return textList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(MainActivity.this,
					R.layout.main_list_view, null);
			TextView tv_id = (TextView) view.findViewById(R.id.tv_show);
			tv_id.setText(textList.get(position));
			return view;
		}
	}

	private boolean checkDatabaseExist() {
		SQLiteDatabase db = null;
		try {
			db = SQLiteDatabase.openDatabase(databasePath + databaseName, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		if (db != null) {
			db.close();
			return true;
		} else {
			return false;
		}
	}

	private void copyDatabase() throws IOException {
		File fileDir = new File(databasePath);
		if (!fileDir.exists())
			fileDir.mkdir();
		File outputFile = new File(databasePath + databaseName);
		FileOutputStream fos = new FileOutputStream(outputFile);
		InputStream is = getAssets().open("cydb.db");
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = is.read(buffer)) != -1) {
			fos.write(buffer, 0, count);
			fos.flush();
		}
		is.close();
		fos.close();
	}

	private boolean checkHelpExist() {
		File file = new File(helpPath + helpName);
		if (file.exists())
			return true;
		else
			return false;
	}

	private void copyHelpFile() throws IOException {
		File fileDir = new File(helpPath);
		if (!fileDir.exists())
			fileDir.mkdir();
		File outputFile = new File(helpPath + helpName);
		FileOutputStream fos = new FileOutputStream(outputFile);
		InputStream is = getResources().openRawResource(R.raw.help);
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = is.read(buffer)) != -1) {
			fos.write(buffer, 0, count);
			fos.flush();
		}
		is.close();
		fos.close();
	}

	@Override
	public void firstText(String first, Fragment fragReceiver,
			Fragment fragSender) {
		if (fragReceiver != null) {
			StandardModeHint smh = (StandardModeHint) fragReceiver;
			smh.setFirst(first);
			smh.setCaller(fragSender);
		} else {
			Log.e("CALLBACK ERROR", "fragment is null");
		}
	}

	@Override
	public void confirmText(String text, Fragment fragReceiver,
			Fragment fragSender) {
		if (fragReceiver != null) {
			StandardMode sMode = (StandardMode) fragReceiver;
			sMode.setConfirmText(text);
		} else {
			Log.e("CALLBACK ERROR", "fragment is null");
		}
	}

	public void queryText(String text, Fragment fragReceiver,
			Fragment fragSender) {
		if (fragReceiver != null) {
			QueryMode queryMode = (QueryMode) fragReceiver;
			queryMode.setCaller(fragSender);
			queryMode.setText(text);
		} else {
			Log.e("CALLBACK ERROR", "fragment is null");
		}
	}
}
