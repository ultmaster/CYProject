package net.ultech.cyproject.ui.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.ultech.cyproject.R;
import net.ultech.cyproject.bean.RecordInfo;
import net.ultech.cyproject.utils.BasicColorConstants;
import net.ultech.cyproject.utils.Constants;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

@SuppressLint({ "ViewHolder", "InflateParams" })
public class HighRecord extends Fragment implements OnItemClickListener,
		OnClickListener {

	private ListView lvRecord;
	private File file;
	//private FileOutputStream outputStream;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.record_layout, null);
		lvRecord = (ListView) view.findViewById(R.id.lv_record);
		btEdit = (Button) view.findViewById(R.id.re_bt_edit);
		btExpand = (Button) view.findViewById(R.id.re_bt_expand);
		file = new File(getActivity().getFilesDir(), Constants.RECORD_FILE_NAME);

		try {
			recordList = new ArrayList<RecordInfo>();
			updateList();
			maxDisplaySize = recordList.size();
			deleteSelection = new boolean[maxDisplaySize + 1];
			Arrays.fill(deleteSelection, false);

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
			Toast.makeText(getActivity(), R.string.empty_record_reminder, Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}
		return view;
	}

	public void updateList() throws IOException {
		recordList.clear();
		inputStream = new FileInputStream(file);
		byte[] buffer = new byte[8192];
		String rawResult = new String();
		while ((inputStream.read(buffer)) != -1)
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
			View view = View.inflate(getActivity(), R.layout.record_list_view,
					null);
			TextView tvRank = (TextView) view.findViewById(R.id.re_tv_rank);
			TextView tvUsername = (TextView) view
					.findViewById(R.id.re_tv_username);
			TextView tvScore = (TextView) view.findViewById(R.id.re_tv_score);
			tvRank.setText(Integer.toString(recordList.get(position).getRank()));
			tvUsername.setText(Html.fromHtml("<b>"
					+ getString(R.string.user_info) + "</b>"
					+ recordList.get(position).getUsername()));
			tvScore.setText(Html.fromHtml("<b>" + getString(R.string.score)
					+ "：</b>"
					+ Integer.toString(recordList.get(position).getScore())));
			if (!editState) {
				tvRank.setTextColor(BasicColorConstants.colorGrey);
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
						recordList.set(i - 1, null);
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
					Toast.makeText(getActivity(),
							R.string.modify_record_failure, Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				maxDisplaySize = recordList.size();
				if (DisplaySize > maxDisplaySize)
					DisplaySize = maxDisplaySize;
				Arrays.fill(deleteSelection, false);
				btEdit.setText(R.string.edit);
				listAdapter.notifyDataSetChanged();
			} else {
				editState = true;
				btEdit.setText(R.string.delete_selection);
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
				rank.setTextColor(BasicColorConstants.colorDefault);
			} else {
				deleteSelection[position + 1] = true;
				rank.setTextColor(BasicColorConstants.colorBlue);
			}
		}
	}
}
