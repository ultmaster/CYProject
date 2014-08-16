package net.ultech.cyproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.recorddomain.RecordInfo;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HighRecord extends Activity implements OnItemClickListener,
		OnClickListener {

	private ListView lvRecord;
	private File file;
	private FileOutputStream outputStream;
	private FileInputStream inputStream;
	private int DisplaySize;
	private int maxDisplaySize;
	private Button btEdit;
	private Button btExpand;
	private List<RecordInfo> recordList;
	private boolean[] deleteSelection;
	private boolean editState;
	private myAdapter listAdapter;
	private String[] result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_layout);
		lvRecord = (ListView) findViewById(R.id.lv_record);
		btEdit = (Button) findViewById(R.id.re_bt_edit);
		btExpand = (Button) findViewById(R.id.re_bt_expand);
		file = new File(getFilesDir(), "ch.record");

		try {
			recordList = new ArrayList<RecordInfo>();
			updateList();
			maxDisplaySize = recordList.size();
			deleteSelection = new boolean[maxDisplaySize + 1];
			for (boolean b : deleteSelection)
				b = false;

			DisplaySize = 10;
			if (maxDisplaySize < DisplaySize)
				DisplaySize = maxDisplaySize;

			editState = false;
			btEdit.setOnClickListener(this);
			btExpand.setOnClickListener(this);
			listAdapter = new myAdapter();
			lvRecord.setAdapter(listAdapter);
			lvRecord.setOnItemClickListener(this);
		} catch (IOException e) {
			Toast.makeText(this, "读取记录异常，记录是否为空？", 1).show();
			e.printStackTrace();
		}
	}

	public void updateList() throws IOException {
		recordList.clear();
		inputStream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		byte[] buffer = new byte[8192];
		int length = 0;
		String rawResult = new String();
		while ((length = inputStream.read(buffer)) != -1)
			rawResult = rawResult + new String(buffer).trim();
		inputStream.close();
		if (!TextUtils.isEmpty(rawResult)) {
			result = rawResult.split("\\$");
		} else
			result = new String[] { "0" };
		int n = Integer.parseInt(result[0]);
		int j = 1;
		for (int i = 1; i <= 2 * n; i = i + 2) {
			RecordInfo info = new RecordInfo(result[i],
					Integer.parseInt(result[i + 1]), j);
			recordList.add(info);
			++j;
		}
	}

	public void updateFile() throws IOException {
		String doneResult = Integer.toString(recordList.size());
		for (RecordInfo info : recordList) {
			doneResult = doneResult + "$" + info.getUsername() + "$"
					+ info.getScore();
		}
		FileOutputStream fos = new FileOutputStream(file, false);
		fos.write(doneResult.getBytes());
		fos.close();
	}

	private class myAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return DisplaySize;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(HighRecord.this,
					R.layout.record_list_view, null);
			TextView tvRank = (TextView) view.findViewById(R.id.re_tv_rank);
			TextView tvUsername = (TextView) view
					.findViewById(R.id.re_tv_username);
			TextView tvScore = (TextView) view.findViewById(R.id.re_tv_score);
			tvRank.setText(Integer.toString(recordList.get(position).getRank()));
			tvUsername.setText(Html.fromHtml("<b>用户名：</b>"
					+ recordList.get(position).getUsername()));
			tvScore.setText(Html.fromHtml("<b>得分：</b>"
					+ Integer.toString(recordList.get(position).getScore())));
			if (!editState) {
				tvRank.setTextColor(0xff000000);
			}
			return view;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.re_bt_edit:
			if (editState) {
				editState = false;
				for (int i = 1; i <= DisplaySize; ++i) {
					if (deleteSelection[i]) {
						recordList.set(i, null);
					}
				}
				List<RecordInfo> newRecordList = new ArrayList<RecordInfo>();
				for (RecordInfo info : recordList) {
					if (info != null) {
						newRecordList.add(info);
					}
				}
				recordList = newRecordList;
				for (int i = 0; i < recordList.size(); ++i) {
					recordList.get(i).setRank(i + 1);
				}
				try {
					updateFile();
				} catch (IOException e) {
					Toast.makeText(this, "修改记录失败。", 1).show();
					e.printStackTrace();
				}
				maxDisplaySize = recordList.size();
				if (DisplaySize > maxDisplaySize)
					DisplaySize = maxDisplaySize;
				for (boolean b : deleteSelection)
					b = false;
				btEdit.setText("编辑");
				listAdapter.notifyDataSetChanged();
			} else {
				editState = true;
				btEdit.setText("删除选中");
			}
			break;
		case R.id.re_bt_expand:
			DisplaySize = maxDisplaySize;
			listAdapter.notifyDataSetChanged();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (editState) {
			TextView rank = (TextView) view.findViewById(R.id.re_tv_rank);
			if (deleteSelection[position + 1]) {
				deleteSelection[position + 1] = false;
				rank.setTextColor(0xff000000);
			} else {
				deleteSelection[position + 1] = true;
				rank.setTextColor(0xff00d2ff);
			}
		}
	}
}
