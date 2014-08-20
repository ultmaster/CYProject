package net.ultech.cyproject.ui.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Help extends Fragment {

	@SuppressLint("InflateParams")
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.help_layout, null);
		try {
			InputStream is = getResources().openRawResource(R.raw.help);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			LinearLayout llRoot = (LinearLayout) view
					.findViewById(R.id.hp_ll_root);
			List<TitleAndText> list = new ArrayList<Help.TitleAndText>();

			String str1, str2;
			while (true) {
				str1 = br.readLine();
				str2 = br.readLine();
				if (str1 == null || str2 == null)
					break;
				list.add(new TitleAndText(str1, str2));
			}
			br.close();
			is.close();

			for (TitleAndText item : list) {
				View tempView = View.inflate(getActivity(),
						R.layout.help_text_view, null);
				TextView title = (TextView) tempView.findViewById(R.id.hp_tv_title);
				TextView text = (TextView) tempView.findViewById(R.id.hp_tv_text);
				title.setText(item.getTitle());
				text.setText(item.getText());
				llRoot.addView(tempView);
			}
			return view;
			
		} catch (IOException e) {
			new AlertDialog.Builder(getActivity())
					.setMessage(R.string.helpfile_open_failure)
					.setPositiveButton(R.string.back,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									getFragmentManager().beginTransaction()
											.remove(Help.this).commit();
								}
							}).show();
			e.printStackTrace();
			return view;
		}
	}

	private class TitleAndText {
		public TitleAndText(String title, String text) {
			super();
			this.title = title;
			this.text = text;
		}

		public String getTitle() {
			return title;
		}

		@SuppressWarnings("unused")
        public void setTitle(String title) {
			this.title = title;
		}

		public String getText() {
			return text;
		}

		@SuppressWarnings("unused")
        public void setText(String text) {
			this.text = text;
		}

		private String title;
		private String text;

	}

}
