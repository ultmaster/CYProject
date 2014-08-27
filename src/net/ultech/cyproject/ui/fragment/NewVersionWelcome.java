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
import android.widget.TextView;

public class NewVersionWelcome extends Fragment implements OnTouchListener {

    private String[] mText;
    private int mIndex;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getActivity().setTitle(R.string.new_version_welcome_title);
        mText = getActivity().getResources().getStringArray(
                R.array.new_version_features);
        mView = View.inflate(getActivity(),
                R.layout.new_version_welcome_layout, null);
        TextView textView = (TextView) mView
                .findViewById(R.id.new_version_welcome_text);
        mIndex = 0;
        textView.setText(mText[mIndex++]);
        mView.setOnTouchListener(this);
        return mView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
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
