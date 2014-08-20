package net.ultech.cyproject.ui.preference;

import net.ultech.cyproject.R;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

	private NumberPicker mPicker;
	private int mLevel;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLevel = 1;
		setDialogLayoutResource(R.layout.pref_picker);
	}

	public void setNowLevel(int nowLevel) {
		mLevel = nowLevel;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mPicker = (NumberPicker) view.findViewById(R.id.level_picker);
		mPicker.setMinValue(1);
		mPicker.setMaxValue(12);
		mPicker.setValue(mLevel);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mLevel = mPicker.getValue();
			callChangeListener(mLevel);
		}
	}

}
