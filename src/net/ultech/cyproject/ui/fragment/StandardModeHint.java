package net.ultech.cyproject.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.bean.WordInfoSpecial;
import net.ultech.cyproject.dao.CYDbDAO;
import net.ultech.cyproject.ui.MainActivity;
import net.ultech.cyproject.utils.BasicColorConstants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.Mode;
import net.ultech.cyproject.utils.DatabaseHolder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class StandardModeHint extends Fragment implements OnClickListener {

    private SQLiteDatabase db;
    private ListView lv;
    private Button btSelect;
    private Button btFigure;
    private List<String> candidate;
    private int shadowPosition;
    private myAdapter adapter;
    private String first, query;
    private MainActivity mActivity;
    private int mMode;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.standard_hint_layout, null);
        db = DatabaseHolder.getDatabase();
        mActivity = (MainActivity) getActivity();
        Bundle bundle = mActivity.mActivityStack.getBackBundle();
        if (mMode == Mode.MODE_STANDARD) {
            first = bundle.getString("first");
            candidate = new ArrayList<String>();
            if (first != null) {
                for (WordInfoSpecial word : CYDbDAO.findByFirst(first, db)) {
                    candidate.add(word.getName());
                }
            }
        }
        if (mMode == Mode.MODE_QUERY) {
            query = bundle.getString("query");
            if (query == null) {
                candidate = new ArrayList<String>();
            } else {
                candidate = CYDbDAO.inCompleteFind(query, db);
            }
            getActivity().getActionBar().setTitle(R.string.query_more);
        }
        if (candidate.isEmpty()) {
            new AlertDialog.Builder(mActivity)
                    .setMessage(R.string.hint_no_match_found)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    mActivity.mActivityStack.popBack();
                                    mActivity.updateFragment();
                                }
                            }).show();
        } else {
            lv = (ListView) view.findViewById(R.id.st_hint_lv);
            btSelect = (Button) view.findViewById(R.id.st_hint_bt_select);
            btFigure = (Button) view.findViewById(R.id.st_hint_bt_figure);
            btSelect.setOnClickListener(this);
            btFigure.setOnClickListener(this);
            if (mMode == Mode.MODE_QUERY)
                btFigure.setVisibility(View.GONE);
            adapter = new myAdapter();
            lv.setAdapter(adapter);
            shadowPosition = mActivity.mActivityStack.getBackBundle().getInt(
                    "shadowPosition", -1);
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    shadowPosition = position;
                    adapter.notifyDataSetChanged();
                }
            });

        }
        return view;
    }

    public StandardModeHint(int mode) {
        super();
        mMode = mode;
    }

    private class myAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return candidate.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(mActivity,
                    R.layout.standard_hint_list_view, null);
            TextView tv = (TextView) view.findViewById(R.id.st_hint_tv);
            tv.setText(candidate.get(position));
            if (shadowPosition == position)
                view.setBackgroundColor(BasicColorConstants.colorFocused);
            else {
                view.setBackgroundColor(BasicColorConstants.colorBackground);
            }
            return view;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.st_hint_bt_select:
            if (shadowPosition == -1) {
                new AlertDialog.Builder(mActivity)
                        .setMessage(R.string.no_word_chosen)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                    }
                                }).show();
            } else {
                if (mMode == Mode.MODE_STANDARD) {
                    mActivity.mActivityStack.popBack();
                    Bundle bundle = new Bundle();
                    bundle.putString("textHuman", candidate.get(shadowPosition));
                    mActivity.mActivityStack.setBack(bundle);
                    mActivity.updateFragment();
                }
                if (mMode == Mode.MODE_QUERY) {
                    mActivity.mActivityStack.popBack();
                    Bundle bundle = new Bundle();
                    bundle.putString("word", candidate.get(shadowPosition));
                    mActivity.mActivityStack.setBack(bundle);
                    mActivity.updateFragment();
                }
            }
            break;
        case R.id.st_hint_bt_figure:
            if (shadowPosition == -1) {
                new AlertDialog.Builder(mActivity)
                        .setMessage(R.string.no_word_chosen)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                    }
                                }).show();
            } else {
                mActivity.mActivityStack.getBackBundle().putInt(
                        "shadowPosition", shadowPosition);
                Bundle bundleForQuery = new Bundle();
                bundleForQuery.putString("word", candidate.get(shadowPosition));
                mActivity.mActivityStack.pushStack(bundleForQuery,
                        mActivity.mFragments[FragmentList.QUERY_MODE]);
                mActivity.updateFragment();
            }
            break;
        }
    }
}
