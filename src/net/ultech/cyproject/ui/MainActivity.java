package net.ultech.cyproject.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.fragment.AboutUs;
import net.ultech.cyproject.ui.fragment.Help;
import net.ultech.cyproject.ui.fragment.HighRecord;
import net.ultech.cyproject.ui.fragment.QueryMode;
import net.ultech.cyproject.ui.fragment.StandardMode;
import net.ultech.cyproject.ui.fragment.StandardModeHint;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import net.ultech.cyproject.utils.DatabaseHolder;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AbsActivity implements
        StandardMode.standardModeListener,
        StandardModeHint.standardModeHintListener {

    private ListView mListView;
    private String[] mDrawerItemNames;
    private String mTitle;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences sp;
    private FragmentManager mManager;
    private FragmentTransaction mTransaction;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = getFragmentManager();
        mTitle = getString(R.string.app_name);

        sp = getSharedPreferences(Constants.PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE);
        boolean firstUse = sp.getBoolean(PreferenceName.BOOL_FIRSTUSE, true);
        if (firstUse) {
            copyDatabase();
            new AlertDialog.Builder(this)
                    .setMessage(
                            "您好，欢迎使用成语接龙1.0测试版。希望您能使用愉快，并提出宝贵意见。联系方式请参见“关于我们”。谢谢。")
                    .setPositiveButton("好的，立即开始！",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    Editor editor = sp.edit();
                                    editor.putBoolean(
                                            PreferenceName.BOOL_FIRSTUSE, false);
                                    editor.commit();
                                }
                            }).show();
        }

        try {
            initializeDatabase();
        } catch (Exception e) {
            Log.w("Database", "数据库打开失败");
        }
        

        mDrawerItemNames = getResources().getStringArray(R.array.drawer_items);

        mFragments = new Fragment[mDrawerItemNames.length];
        mFragments[FragmentList.STANDARD_MODE] = new StandardMode();
        mFragments[FragmentList.QUERY_MODE] = new QueryMode();
        mFragments[FragmentList.HIGH_RECORD] = new HighRecord();
        mFragments[FragmentList.HELP_ACTIVITY] = new Help();
        mFragments[FragmentList.ABOUT_US] = new AboutUs();
        mTransaction = mManager.beginTransaction();
        for (Fragment f : mFragments) {
            if (f != null) {
                mTransaction.add(R.id.main_content_frame, f);
                mTransaction.hide(f);
            }
        }
        mTransaction.commit();
        switchToFragment(FragmentList.ABOUT_US);
        // 你应该做个主界面

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        initializeDrawer();
        mListView = (ListView) findViewById(R.id.lv_main);
        mListView.setAdapter(new myListAdapter());
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                switch (position) {
                case FragmentList.STANDARD_MODE:
                case FragmentList.QUERY_MODE:
                case FragmentList.HIGH_RECORD:
                case FragmentList.HELP_ACTIVITY:
                case FragmentList.ABOUT_US:
                    switchToFragment(position);
                    break;
                case FragmentList.CHALLENGE_MODE:
                    Intent intent_challenge = new Intent(MainActivity.this,
                            ChallengeMode.class);
                    startActivity(intent_challenge);
                    break;
                case FragmentList.PERSONAL_SETTINGS:
                    Intent intent_setting = new Intent(MainActivity.this,
                            PersonalSettings.class);
                    startActivity(intent_setting);
                    break;
                default:
                    throw new RuntimeException("你干了什么？");
                }
                mDrawerLayout.closeDrawers();
            }
        });
    }

    private void switchToFragment(int id) {
        mTitle = mDrawerItemNames[id];
        mTransaction = mManager.beginTransaction();
        mTransaction.setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out, android.R.animator.fade_in,
                android.R.animator.fade_out);
        for (int i = 0; i < mFragments.length; i++) {
            Fragment f = mFragments[i];
            if (f != null) {
                if (i != id) {
                    mTransaction.hide(f);
                } else {
                    mTransaction.show(f);
                }
            }
        }
        mTransaction.commit();
    }

    /*
     * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
     * (keyCode == KeyEvent.KEYCODE_BACK) { FragmentManager fm =
     * getFragmentManager(); if (fm.getBackStackEntryCount() > 1)
     * fm.popBackStack(); else { this.finish(); } } return true; }
     */

    private class myListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDrawerItemNames.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(MainActivity.this,
                    R.layout.main_list_view, null);
            TextView tv_id = (TextView) view.findViewById(R.id.tv_show);
            tv_id.setText(mDrawerItemNames[position]);
            return view;
        }
    }

    private boolean checkDatabaseAvailable(String dir) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(dir + "cydb.db", null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (db != null) {
            db.close();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void firstText(String first, Fragment fragReceiver,
            Fragment fragSender) {
        if (fragReceiver != null) {
            StandardModeHint smh = (StandardModeHint) fragReceiver;
            smh.setFirst(first);
            smh.setCaller(fragSender);
        } else {
            Log.e("CALLBACK ERROR", "fragment is null");
        }
    }

    @Override
    public void confirmText(String text, Fragment fragReceiver,
            Fragment fragSender) {
        if (fragReceiver != null) {
            StandardMode sMode = (StandardMode) fragReceiver;
            sMode.setConfirmText(text);
        } else {
            Log.e("CALLBACK ERROR", "fragment is null");
        }
    }

    public void queryText(String text, Fragment fragReceiver,
            Fragment fragSender) {
        if (fragReceiver != null) {
            QueryMode queryMode = (QueryMode) fragReceiver;
            queryMode.setCaller(fragSender);
            queryMode.setText(text);
        } else {
            Log.e("CALLBACK ERROR", "fragment is null");
        }
    }
    
    
    private void initializeDatabase() throws Exception{
        try {
            mHelper = new CYDbOpenHelper(this, this.getExternalFilesDir(null)
                    .getAbsolutePath(), Constants.DATABASE_FILE_NAME);
        } catch (Exception e1) {
            Log.e("Database", "External database open error");
            e1.printStackTrace();
            try {
                mHelper = new CYDbOpenHelper(this, this.getFilesDir()
                        .getAbsolutePath(), Constants.DATABASE_FILE_NAME);
            } catch (Exception e2) {
                Log.e("Database", "Internal database open error");
                e2.printStackTrace();
            }
            throw new Exception("Database not available");
        }
        DatabaseHolder.putDatabase(mHelper.getReadableDatabase());
    }
    
    private void copyDatabase() {
        if(!getExternalFilesDir(null).exists()){
            getExternalFilesDir(null).mkdir();
        }
        if(!getFilesDir().exists()){
            getFilesDir().mkdir();
        }
        try{
            InputStream is=getAssets().open(Constants.DATABASE_FILE_NAME);
            FileOutputStream fos=new FileOutputStream(getExternalFilesDir(null).getAbsolutePath()+Constants.DATABASE_FILE_NAME);
            byte[] buffer=new byte[1024];  
            int count = 0;  
            while((count = is.read(buffer))>0){  
                fos.write(buffer,0,count);  
            }  
            fos.flush();  
            fos.close();  
            is.close(); 
            //这个动作可能相当慢，不能在主线程进行，不然可能会造成程序ANR
        }
        catch(IOException e1){
            Log.e("Database","Copy to External Storage Error");
            e1.printStackTrace();
            try{
                InputStream is=getAssets().open(Constants.DATABASE_FILE_NAME);
                FileOutputStream fos=new FileOutputStream(getFilesDir().getAbsolutePath()+Constants.DATABASE_FILE_NAME);
                byte[] buffer=new byte[1024];  
                int count = 0;  
                while((count = is.read(buffer))>0){  
                    fos.write(buffer,0,count);  
                }  
                fos.flush();  
                fos.close();  
                is.close(); 
            }
            catch (IOException e2){
                Log.e("Database","Copy to Internal Storage Error");
                Log.wtf("Database","Database copy all failes");
                e2.printStackTrace();
                throw new RuntimeException("ABORT");
            }
        }
    }

    private void initializeDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        0, /* "open drawer" description for accessibility */
        0 /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.app_name);

                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()

            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        return super.onOptionsItemSelected(item);
    }

}
