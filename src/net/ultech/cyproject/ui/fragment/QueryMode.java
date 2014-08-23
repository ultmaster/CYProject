package net.ultech.cyproject.ui.fragment;

import net.ultech.cyproject.R;
import net.ultech.cyproject.bean.WordInfoComplete;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.DatabaseHolder;
import net.ultech.cyproject.utils.Constants.Mode;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import android.annotation.SuppressLint;
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

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.query_layout, null);
        etWord = (EditText) view.findViewById(R.id.qu_et_word);
        btOK = (Button) view.findViewById(R.id.qu_bt_ok);
        tvResult = (TextView) view.findViewById(R.id.qu_tv_result);
        btOK.setOnClickListener(this);
        mActivity = (MainActivity) getActivity();
        text = mActivity.getSharedPreferences(Constants.PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE).getString(
                PreferenceName.STRING_LAST_QUERY, "");
        Bundle backBundle = mActivity.mActivityStack.getBackBundle();
        String newQueryWord = null;
        if (backBundle != null)
            newQueryWord = backBundle.getString("word", "");
        if (newQueryWord != null && !TextUtils.isEmpty(newQueryWord)) {
            text = newQueryWord;
            etWord.setText(text);
            etWord.invalidate();
            btOK.performClick();
        }
        return view;
    }

    @Override
    public void onStop() {
        mActivity
                .getSharedPreferences(Constants.PREFERENCE_FILE_NAME,
                        Context.MODE_PRIVATE).edit()
                .putString(PreferenceName.STRING_LAST_QUERY, text).commit();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.qu_bt_ok:
            String text = etWord.getText().toString().trim();
            if (TextUtils.isEmpty(text))
                tvResult.setText(R.string.empty_query_reminder);
            WordInfoComplete word = CYDbDAO.findComplete(text,
                    DatabaseHolder.getDatabase());
            if (word != null) {
                String source = "<b>"
                        + getActivity().getString(R.string.query_result_title)
                        + "</b>" + word.getName() + "<br><b>"
                        + getActivity().getString(R.string.query_result_spell)
                        + "</b>" + word.getSpell();
                if (!TextUtils.isEmpty(word.getContent()))
                    source = source
                            + "<br><b>"
                            + getActivity().getString(
                                    R.string.query_result_content) + "</b>"
                            + word.getContent();
                if (!TextUtils.isEmpty(word.getDerivation()))
                    source = source
                            + "<br><b>"
                            + getActivity().getString(
                                    R.string.query_result_derivation) + "</b>"
                            + word.getDerivation();
                if (!TextUtils.isEmpty(word.getSamples()))
                    source = source
                            + "<br><b>"
                            + getActivity().getString(
                                    R.string.query_result_samples) + "</b>"
                            + word.getSamples();
                tvResult.setText(Html.fromHtml(source));
            } else {
                Bundle bundleHint = new Bundle();
                bundleHint.putString("query", text);
                ResultList fragmentHint = new ResultList(
                        Mode.MODE_QUERY);
                mActivity.mActivityStack.pushStack(bundleHint, fragmentHint);
                mActivity.updateFragment();
            }
            break;
        }
    }
}
