package net.ultech.cyproject.ui;

import net.ultech.cyproject.R;
import net.ultech.cyproject.R.id;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.bean.WordInfoComplete;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.utils.AbsActivity;
import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class QueryMode extends Fragment implements OnClickListener {

	private EditText etWord;
	private Button btOK;
	private TextView tvResult;
	private SQLiteDatabase db;
	private CYDbOpenHelper helper;
	private String text;
	private Fragment mCaller;

	public void setText(String str) {
		text = str;
	}
	
	public void setCaller(Fragment frag) {
		mCaller = frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.query_layout, null);
		etWord = (EditText) view.findViewById(R.id.qu_et_word);
		btOK = (Button) view.findViewById(R.id.qu_bt_ok);
		tvResult = (TextView) view.findViewById(R.id.qu_tv_result);
		helper = new CYDbOpenHelper(getActivity());
		db = helper.getReadableDatabase();
		btOK.setOnClickListener(this);
		if (text != null && !TextUtils.isEmpty(text)) {
			etWord.setText(text);
			btOK.performClick();
		}
		return view;
	}

	@Override
	public void onStop() {
		db.close();
		super.onStop();
	}
	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.qu_bt_ok:
			String text = etWord.getText().toString().trim();
			if (TextUtils.isEmpty(text))
				tvResult.setText("当前查询内容为空");
			WordInfoComplete word = CYDbDAO.findComplete(text, db);
			if (word != null) {
				String source = "<b>【成语】</b>" + word.getName()
						+ "<br><b>【读音】</b>" + word.getSpell();
				if (!TextUtils.isEmpty(word.getContent()))
					source = source + "<br><b>【解释】</b>" + word.getContent();
				if (!TextUtils.isEmpty(word.getDerivation()))
					source = source + "<br><b>【出处】</b>" + word.getDerivation();
				if (!TextUtils.isEmpty(word.getSamples()))
					source = source + "<br><b>【例句】</b>" + word.getSamples();
				tvResult.setText(Html.fromHtml(source));
			} else {
				tvResult.setText("很抱歉，查询失败。请检查拼写是否正确。");
			}
			break;
		}
	}
}
