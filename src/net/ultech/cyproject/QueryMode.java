package net.ultech.cyproject;

import net.ultech.cyproject.db.CYDbOpenHelper;
import net.ultech.cyproject.db.dao.CYDbDAO;
import net.ultech.cyproject.worddomain.WordInfoComplete;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class QueryMode extends Activity implements OnClickListener {

	private EditText etWord;
	private Button btOK;
	private TextView tvResult;
	private SQLiteDatabase db;
	private CYDbOpenHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query_layout);
		etWord = (EditText) findViewById(R.id.qu_et_word);
		btOK = (Button) findViewById(R.id.qu_bt_ok);
		tvResult = (TextView) findViewById(R.id.qu_tv_result);
		helper = new CYDbOpenHelper(this);
		db = helper.getReadableDatabase();
		btOK.setOnClickListener(this);
		Intent intent = getIntent();
		String text = intent.getStringExtra("word");
		if (text != null && !TextUtils.isEmpty(text)) {
			etWord.setText(text);
			btOK.performClick();
		}
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
