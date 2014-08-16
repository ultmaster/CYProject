/*
package net.ultech.cyproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class CopyOfPersonalSettings extends Activity implements OnItemClickListener {

	private SharedPreferences sp;
	private ListView lv;
	private myAdapter adapter;
	private List<ListItem> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);
		sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
		int savedLevel = sp.getInt("st_savedLevel", 1);
		String savedPrimary = sp.getString("ch_savedPrimary", "life");
		String defaultUsername = sp.getString("ch_defaultUsername", "无名氏");
		list = new ArrayList<CopyOfPersonalSettings.ListItem>();
		list.add(new ListItem(ListItem.category, "标准模式", null));
		list.add(new ListItem(ListItem.catLevel, "难度", Integer
				.toString(savedLevel)));
		list.add(new ListItem(ListItem.catEmptyLog, "清空日志", null));
		list.add(new ListItem(ListItem.category, "挑战模式", null));
		list.add(new ListItem(ListItem.catTimeOrLife, "时间/生命值优先", savedPrimary));
		list.add(new ListItem(ListItem.catDefUser, "默认用户名", defaultUsername));
		list.add(new ListItem(ListItem.catEmptyRecord, "清空记录", null));
		ListView lv = (ListView) findViewById(R.id.set_lv);
		adapter = new myAdapter();
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
	}

	private class myAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
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
			View view = null;
			TextView tv1;
			TextView tv2;
			switch (list.get(position).getType()) {
			case ListItem.category:
				view = View.inflate(CopyOfPersonalSettings.this,
						R.layout.setting_category, null);
				tv1 = (TextView) view.findViewById(R.id.set_cat_tv);
				tv1.setText(Html.fromHtml("<b>" + list.get(position).getData1()
						+ "</b>"));
				break;
			case ListItem.catLevel:
				view = View.inflate(CopyOfPersonalSettings.this,
						R.layout.setting_with_subtitle, null);
				tv1 = (TextView) view.findViewById(R.id.set_sub_tv_main);
				tv2 = (TextView) view.findViewById(R.id.set_sub_tv_sub);
				tv1.setText(list.get(position).getData1());
				tv2.setText("目前难度：" + list.get(position).getData2());
				break;
			case ListItem.catTimeOrLife:
				view = View.inflate(CopyOfPersonalSettings.this,
						R.layout.setting_with_subtitle, null);
				tv1 = (TextView) view.findViewById(R.id.set_sub_tv_main);
				tv2 = (TextView) view.findViewById(R.id.set_sub_tv_sub);
				tv1.setText(list.get(position).getData1());
				if (list.get(position).getData2().equals("life"))
					tv2.setText("目前：生命值优先");
				else
					tv2.setText("目前：时间优先");
				break;
			case ListItem.catDefUser:
				view = View.inflate(CopyOfPersonalSettings.this,
						R.layout.setting_with_subtitle, null);
				tv1 = (TextView) view.findViewById(R.id.set_sub_tv_main);
				tv2 = (TextView) view.findViewById(R.id.set_sub_tv_sub);
				tv1.setText(list.get(position).getData1());
				tv2.setText("目前默认用户名：" + list.get(position).getData2());
				break;
			case ListItem.catEmptyLog:
			case ListItem.catEmptyRecord:
				view = View.inflate(CopyOfPersonalSettings.this,
						R.layout.setting_blank, null);
				tv1 = (TextView) view.findViewById(R.id.set_bla_tv);
				tv1.setText(list.get(position).getData1());
				tv1.setTextColor(0xffe33c3c);
				break;
			}
			return view;
		}

	}

	private class ListItem {
		public ListItem() {

		}

		public ListItem(int type, String data1, String data2) {
			super();
			this.type = type;
			this.data1 = data1;
			this.data2 = data2;
		}

		public static final int category = 1;
		public static final int catEmptySetting = 2;
		public static final int catLevel = 3;
		public static final int catEmptyLog = 4;
		public static final int catTimeOrLife = 5;
		public static final int catDefUser = 6;
		public static final int catEmptyRecord = 7;

		private int type;
		private String data1;
		private String data2;

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getData1() {
			return data1;
		}

		public void setData1(String data1) {
			this.data1 = data1;
		}

		public String getData2() {
			return data2;
		}

		public void setData2(String data2) {
			this.data2 = data2;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Editor editor = sp.edit();
		final int pos = position;
		switch (list.get(position).getType()) {
		case ListItem.catLevel:
			final View viewLevel = View.inflate(this,
					R.layout.setting_dialog_level, null);
			new AlertDialog.Builder(this)
					.setTitle("设置")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(viewLevel)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									EditText etLevel = (EditText) viewLevel
											.findViewById(R.id.set_et_dialog_level);
									String strLevel = etLevel.getText()
											.toString();
									if (strLevel == null
											|| TextUtils.isEmpty(strLevel))
										Toast.makeText(CopyOfPersonalSettings.this,
												"输入不合法", 1).show();
									else {
										int tempLevel = Integer
												.parseInt(strLevel);
										if (tempLevel < 1 || tempLevel > 12)
											Toast.makeText(
													CopyOfPersonalSettings.this,
													"输入不合法", 1).show();
										else {
											editor.putInt("st_savedLevel",
													tempLevel);
											editor.commit();
											list.get(pos)
													.setData2(
															Integer.toString(tempLevel));
											adapter.notifyDataSetChanged();
										}
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
			break;
		case ListItem.catTimeOrLife:
			final View viewTimeLife = View.inflate(this,
					R.layout.setting_dialog_time_or_life, null);
			final RadioButton rbTime = (RadioButton) viewTimeLife
					.findViewById(R.id.set_rb_time);
			final RadioButton rbLife = (RadioButton) viewTimeLife
					.findViewById(R.id.set_rb_life);
			if (list.get(position).getData2() == "time")
				rbTime.setChecked(true);
			else {
				rbLife.setChecked(true);
			}
			new AlertDialog.Builder(this)
					.setTitle("设置")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(viewTimeLife)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (rbTime.isChecked()) {
										editor.putString("ch_savedPrimary",
												"time");
										list.get(pos).setData2("time");
									} else if (rbLife.isChecked()) {
										editor.putString("ch_savedPrimary",
												"life");
										list.get(pos).setData2("life");
									} else {
										Toast.makeText(CopyOfPersonalSettings.this,
												"输入不合法", 1).show();
									}
									editor.commit();
									adapter.notifyDataSetChanged();

								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
			break;
		case ListItem.catDefUser:
			final View viewUsername = View.inflate(this,
					R.layout.setting_dialog_username, null);
			final EditText etUsername = (EditText) viewUsername
					.findViewById(R.id.set_et_dialog_username);
			etUsername.setText(list.get(position).getData2());
			new AlertDialog.Builder(this)
					.setTitle("设置")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(viewUsername)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String strUsername = etUsername.getText()
											.toString();
									if (strUsername == null || TextUtils.isEmpty(strUsername))
										Toast.makeText(CopyOfPersonalSettings.this,
												"输入中不能为空", 1).show();
									else if (strUsername.contains("$")
											|| strUsername.contains(" "))
										Toast.makeText(CopyOfPersonalSettings.this,
												"输入中不能含有$和空格", 1).show();
									else {
										editor.putString("ch_defaultUsername",
												strUsername);
										list.get(pos).setData2(strUsername);
										editor.commit();
										adapter.notifyDataSetChanged();
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
			break;
		case ListItem.catEmptyLog:
			new AlertDialog.Builder(this)
					.setMessage("此操作不可恢复。确定要删除日志？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(CopyOfPersonalSettings.this
											.getFilesDir(), "st.log");
									if (file.exists()) {
										file.delete();
										Toast.makeText(CopyOfPersonalSettings.this,
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
			break;
		case ListItem.catEmptyRecord:
			new AlertDialog.Builder(this)
					.setMessage("此操作不可恢复。确定要删除记录？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(CopyOfPersonalSettings.this
											.getFilesDir(), "ch.record");
									if (file.exists()) {
										file.delete();
										Toast.makeText(CopyOfPersonalSettings.this,
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
			break;
		}
	}
}*/
