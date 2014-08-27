package net.ultech.cyproject.ui.fragment;

import net.ultech.cyproject.R;
import net.ultech.cyproject.ui.MainActivity;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.TextView;

public class NewVersionWelcome extends Fragment implements OnTouchListener {

	private String[] mText;
	private int mIndex;
	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mText = new String[] { "本版本新增反馈功能，遇到程序崩溃或者成语库的问题可以点击右上角反馈按钮告诉我们！",
				"本版本亦自带背景音乐及点击音效（默认关闭），可以在设置中自由开关。" };
		mView = View.inflate(getActivity(),
				R.layout.new_version_welcome_layout, null);
		TextView textView = (TextView) mView
				.findViewById(R.id.new_version_welcome_text);
		mIndex = 0;
		textView.setText(mText[mIndex++]);
		mView.setOnTouchListener(this);
		return mView;
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i("NewVersionWelcome", "onTouch");
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mIndex < mText.length) {
				TextView textView = (TextView) mView
						.findViewById(R.id.new_version_welcome_text);
				AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
				alphaAnimation.setDuration(800);
				textView.startAnimation(alphaAnimation);
				textView.setText(mText[mIndex++]);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				alphaAnimation = new AlphaAnimation(0, 1);
				alphaAnimation.setDuration(800);
				textView.startAnimation(alphaAnimation);
			} else {
				((MainActivity) getActivity()).updateFragment();

			}
		}
		return true;
	}

}
