package net.ultech.cyproject.ui.fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.utils.BasicColorConstants;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StandardModeLog extends Fragment {

    private String rawResult;
    private List<Word> wordList;
    // private ScrollView scroll;
    private MainActivity mActivity;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.standard_log_layout, null);
        LinearLayout llRoot = (LinearLayout) view
                .findViewById(R.id.st_ll_log_root);
        // scroll = (ScrollView) view.findViewById(R.id.st_sv_log);
        mActivity = (MainActivity) getActivity();
        try {
            File file = new File(mActivity.getFilesDir(), "st.log");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            rawResult = new String();
            int length = 1024;
            char[] buffer = new char[1024];
            while (br.read(buffer, 0, length) != -1) {
                rawResult = rawResult + (new String(buffer)).trim();
            }
            wordList = new ArrayList<StandardModeLog.Word>();
            br.close();
            logReader();
            for (int i = wordList.size() - 1; i >= 0; --i) {
                TextView tv = new TextView(mActivity);
                tv.setText(wordList.get(i).getName());
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                tv.setLineSpacing(0, 1.5f);
                if (wordList.get(i).type == Word.humanType) {
                    tv.setTextColor(BasicColorConstants.colorBlue);
                    tv.setGravity(Gravity.LEFT);
                } else {
                    tv.setGravity(Gravity.RIGHT);
                    tv.setTextColor(BasicColorConstants.colorRed);
                }
                llRoot.addView(tv);
            }

        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(mActivity,
                    getActivity().getString(R.string.log_io_failure), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return view;
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        public boolean isType() {
            return type;
        }

        public void setType(boolean type) {
            this.type = type;
        }
    }
}
