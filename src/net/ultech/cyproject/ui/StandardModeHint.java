package net.ultech.cyproject.ui;

import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.R.id;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.bean.WordInfoSpecial;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.StandardMode.standardModeListener;
import net.ultech.cyproject.utils.AbsActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class StandardModeHint extends Fragment implements OnClickListener {

	private SQLiteDatabase db;
	private CYDbOpenHelper helper;
	private ListView lv;
	private Button btSelect;
	private Button btFigure;
	private List<WordInfoSpecial> candidate;
	private int shadowPosition;
	private myAdapter adapter;
	private String first;
	private standardModeHintListener mCallback;

	public void setFirst(String mFirst) {
		first = mFirst;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.standard_hint_layout, null);
		helper = new CYDbOpenHelper(getActivity());
		db = helper.getReadableDatabase();
		if (first != null) {
			candidate = CYDbDAO.findByFirst(first, db);
		} else {
			candidate = new ArrayList<WordInfoSpecial>();
		}
		if (candidate.isEmpty()) {
			new AlertDialog.Builder(getActivity())
					.setMessage("无法找到匹配，点击确定返回标准模式。")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									getFragmentManager().beginTransaction()
											.remove(StandardModeHint.this)
											.commit();
								}
							}).show();
		} else {
			lv = (ListView) view.findViewById(R.id.st_hint_lv);
			btSelect = (Button) view.findViewById(R.id.st_hint_bt_select);
			btFigure = (Button) view.findViewById(R.id.st_hint_bt_figure);
			btSelect.setOnClickListener(this);
			btFigure.setOnClickListener(this);
			adapter = new myAdapter();
			lv.setAdapter(adapter);
			shadowPosition = -1;
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					shadowPosition = position;
					adapter.notifyDataSetChanged();
				}
			});

		}
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (standardModeHintListener) activity;
	}

	private class myAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return candidate.size();
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
			View view = View.inflate(getActivity(),
					R.layout.standard_hint_list_view, null);
			TextView tv = (TextView) view.findViewById(R.id.st_hint_tv);
			tv.setText(candidate.get(position).getName());
			if (shadowPosition == position)
				view.setBackgroundColor(0x33000000);
			else {
				view.setBackgroundColor(0x00000000);
			}
			return view;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.st_hint_bt_select:
			if (shadowPosition == -1) {
				new AlertDialog.Builder(getActivity())
						.setMessage("没有选择成语。")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			} else {
				
				//TODO:
				//mCallback.confirmText("", frag);
			}
			break;
		case R.id.st_hint_bt_figure:/*
			if (shadowPosition == -1) {
				new AlertDialog.Builder(getActivity())
						.setMessage("没有选择成语。")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			} else {
				Intent intent_query = new Intent(this, QueryMode.class);
				intent_query.putExtra("word", candidate.get(shadowPosition)
						.getName());
				startActivity(intent_query);
			}*/
			break;
		}
	}

	public interface standardModeHintListener {
		public void confirmText(String text, Fragment frag);
	}
}
