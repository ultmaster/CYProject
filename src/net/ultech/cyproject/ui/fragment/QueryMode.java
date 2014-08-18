package net.ultech.cyproject.ui.fragment;

import net.ultech.cyproject.R;
import net.ultech.cyproject.bean.WordInfoComplete;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.utils.DatabaseHolder;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class QueryMode extends Fragment implements OnClickListener {

	private EditText etWord;
	private Button btOK;
	private TextView tvResult;
	private String text;
	private MainActivity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.query_layout, null);
		etWord = (EditText) view.findViewById(R.id.qu_et_word);
		btOK = (Button) view.findViewById(R.id.qu_bt_ok);
		tvResult = (TextView) view.findViewById(R.id.qu_tv_result);
		btOK.setOnClickListener(this);
		mActivity = (MainActivity) getActivity();
		text = mActivity.getSharedPreferences("setting", Context.MODE_PRIVATE)
				.getString("last_query", "");
		Bundle backBundle = mActivity.mActivityStack.getBackBundle();
		String newQueryWord = null;
		if (backBundle != null)
			newQueryWord = backBundle.getString("word", "");

		if (newQueryWord != null && !TextUtils.isEmpty(newQueryWord))
			text = newQueryWord;
		if (!TextUtils.isEmpty(text)) {
			etWord.setText(text);
			btOK.performClick();
		}
		return view;
	}

	@Override
	public void onStop() {
		mActivity.getSharedPreferences("setting", Context.MODE_PRIVATE).edit()
				.putString("last_query", text).commit();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.qu_bt_ok:
			String text = etWord.getText().toString().trim();
			if (TextUtils.isEmpty(text))
				tvResult.setText("当前查询内容为空");
			WordInfoComplete word = CYDbDAO.findComplete(text,
					DatabaseHolder.getDatabase());
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
