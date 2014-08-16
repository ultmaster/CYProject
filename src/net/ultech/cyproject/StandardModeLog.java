package net.ultech.cyproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class StandardModeLog extends Activity {

	private String rawResult;
	private List<Word> wordList;
	private ScrollView scroll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.standard_log_layout);
		LinearLayout llRoot = (LinearLayout) findViewById(R.id.st_ll_log_root);
		scroll = (ScrollView) findViewById(R.id.st_sv_log);
		try {
			File file = new File(this.getFilesDir(), "st.log");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			rawResult = new String();
			int length = 1024;
			char[] buffer = new char[1024];
			while (br.read(buffer, 0, length) != -1) {
				rawResult = rawResult + (new String(buffer)).trim();
			}
			wordList = new ArrayList<StandardModeLog.Word>();

			logReader();
			for (int i = wordList.size() - 1; i >= 0; --i) {
				TextView tv = new TextView(this);
				tv.setText(wordList.get(i).getName());
				tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
				tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
				tv.setLineSpacing(0, 1.5f);
				if (wordList.get(i).type == Word.humanType) {
					tv.setTextColor(0xff00d2ff);
					tv.setGravity(Gravity.LEFT);
				} else {
					tv.setGravity(Gravity.RIGHT);
					tv.setTextColor(0xffe33c3c);
				}
				llRoot.addView(tv);
			}

		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "日志读写失败。是否为空？", 1).show();
			e.printStackTrace();
		}
	}

	public void logReader() {
		for (int i = 0; i < rawResult.length(); ++i) {
			Word word = new Word();
			if (rawResult.charAt(++i) == 'r')
				word.setType(Word.robotType);
			else {
				word.setType(Word.humanType);
			}
			++i;
			int start = (++i);
			while (rawResult.charAt(i) != '$')
				++i;
			word.setName(rawResult.substring(start, i));
			wordList.add(word);
		}
	}

	private class Word {
		public Word() {

		}

		public Word(String name, boolean type) {
			super();
			this.name = name;
			this.type = type;
		}

		private String name;
		private boolean type;
		private static final boolean humanType = false;
		private static final boolean robotType = true;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isType() {
			return type;
		}

		public void setType(boolean type) {
			this.type = type;
		}
	}
}
