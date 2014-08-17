package net.ultech.cyproject.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.R.id;
import net.ultech.cyproject.R.layout;
import net.ultech.cyproject.utils.AbsActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends AbsActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			File file = new File(getFilesDir(), "help");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			setContentView(R.layout.help_layout);
			LinearLayout llRoot = (LinearLayout) findViewById(R.id.hp_ll_root);
			List<TitleAndText> list = new ArrayList<HelpActivity.TitleAndText>();

			String str1, str2;
			while (true) {
				str1 = br.readLine();
				str2 = br.readLine();
				if (str1 == null || str2 == null)
					break;
				list.add(new TitleAndText(str1, str2));
			}
			br.close();
			fis.close();

			for (TitleAndText item : list) {
				View view = View.inflate(this, R.layout.help_text_view, null);
				TextView title = (TextView) view.findViewById(R.id.hp_tv_title);
				TextView text = (TextView) view.findViewById(R.id.hp_tv_text);
				title.setText(item.getTitle());
				text.setText(item.getText());
				llRoot.addView(view);
			}
		} catch (Exception e) {
			new AlertDialog.Builder(this)
					.setMessage("帮助文档打开失败。")
					.setPositiveButton("返回",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									HelpActivity.this.finish();
								}
							}).show();
			e.printStackTrace();
		}
	}

	private class TitleAndText {
		public TitleAndText() {

		}

		public TitleAndText(String title, String text) {
			super();
			this.title = title;
			this.text = text;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		private String title;
		private String text;

	}

}
