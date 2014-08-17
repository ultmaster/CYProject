package net.ultech.cyproject.ui.preference;

import net.ultech.cyproject.R;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MyEditTextPreference extends DialogPreference {

	private String mHint;
	private String mInput;
	private TextView mTextView;
	private EditText mEditText;

	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInput = new String();
		mHint = new String();
		setDialogLayoutResource(R.layout.pref_edittext);
	}

	public void setNowText(String text) {
		mInput = text;
		Log.d("success", "setEditText");
	}
	
	public void setHint(String hint) {
		mHint = hint;
		Log.d("success", "setTextView");
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mTextView = (TextView) view.findViewById(R.id.pref_username_hint);
		mEditText = (EditText) view.findViewById(R.id.pref_et_username);
		mTextView.setText(mHint);
		mEditText.setText(mInput);
		Log.d("success", "bindDialog");
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult) {
			mInput = mEditText.getText().toString();
			callChangeListener(mInput);
		}
	}

}
