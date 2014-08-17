package net.ultech.cyproject.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.ultech.cyproject.R;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.PreferenceName;
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
import android.view.Gravity;
import android.view.KeyEvent;
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
	private String databasePath;
	private String databaseName;
	private String helpPath;
	private String helpName;
	private String mTitle;
	private DrawerLayout mDrawerLayout;
	private SharedPreferences sp;
	private StandardMode standardMode;
	private QueryMode queryMode;
	private HighRecord highRecord;
	private HelpActivity helpActivity;
	private AboutUs aboutUs;
	private FragmentManager mManager;
	private FragmentTransaction mTransaction;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mManager=getFragmentManager();
		mTitle=getString(R.string.app_name);

		sp = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
		boolean firstUse = sp.getBoolean(PreferenceName.BOOL_FIRSTUSE, true);
		if (firstUse) {
			new AlertDialog.Builder(this)
					.setMessage(
							"您好，欢迎使用成语接龙1.0测试版。希望您能使用愉快，并提出宝贵意见。联系方式请参见“关于我们”。谢谢。")
					.setPositiveButton("好的，立即开始！",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Editor editor = sp.edit();
									editor.putBoolean(PreferenceName.BOOL_FIRSTUSE, false);
									editor.commit();
								}
							}).show();
		}

        databasePath = getFilesDir().getPath() + getPackageName() + "/databases/";
		databaseName = "cydb.db";
		if (!checkDatabaseExist()) {
			try {
				copyDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		helpPath = getFilesDir().getAbsolutePath() + "/";
		helpName = "help";
		if (!checkHelpExist()) {
			try {
				copyHelpFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		mDrawerItemNames=getResources().getStringArray(R.array.drawer_items);

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		initializeDrawer();
		mListView = (ListView) findViewById(R.id.lv_main);
		mListView.setAdapter(new myListAdapter());
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mTransaction=mManager.beginTransaction();
				mDrawerLayout.closeDrawers();
				mTitle=mDrawerItemNames[position];
				switch(position){
				case FragmentList.STANDARD_MODE:
				    standardMode = new StandardMode();
                    replaceToMainFrame(standardMode);
				    break;
                case FragmentList.CHALLENGE_MODE:
                    Intent intent_challenge = new Intent(MainActivity.this,
                            ChallengeMode.class);
                    startActivity(intent_challenge);
                    break;
				case FragmentList.QUERY_MODE:
				    queryMode = new QueryMode();
                    replaceToMainFrame(queryMode);
				    break;
				case FragmentList.HIGH_RECORD:
				    highRecord = new HighRecord();
                    replaceToMainFrame(highRecord);
				    break;
				case FragmentList.PERSONAL_SETTINGS:
				    Intent intent_setting = new Intent(MainActivity.this,
                            PersonalSettings.class);
                    startActivity(intent_setting);
				    break;
				case FragmentList.HELP_ACTIVITY:
				    helpActivity = new HelpActivity();
                    replaceToMainFrame(helpActivity);
				    break;
				case FragmentList.ABOUT_US:
				    aboutUs = new AboutUs();
                    replaceToMainFrame(aboutUs);
				    break;
				default:
				      throw new RuntimeException("你干了什么？");
				}
				
			}
		});
	}
	
	private void replaceToMainFrame(Fragment fragment) {
	        mTransaction.replace(R.id.main_content_frame, fragment);
	        mTransaction.show(fragment).commit();
	        mManager.executePendingTransactions();
	    }

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			FragmentManager fm = getFragmentManager();
			if (fm.getBackStackEntryCount() > 1)
				fm.popBackStack();
			else {
				this.finish();
			}
		}
		return true;
	}*/

	private class myListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDrawerItemNames.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
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

	private boolean checkDatabaseExist() {
		SQLiteDatabase db = null;
		try {
			db = SQLiteDatabase.openDatabase(databasePath + databaseName, null,
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

	private void copyDatabase() throws IOException {
		File fileDir = new File(databasePath);
		if (!fileDir.exists())
			fileDir.mkdir();
		File outputFile = new File(databasePath + databaseName);
		FileOutputStream fos = new FileOutputStream(outputFile);
		InputStream is = getAssets().open(databaseName);
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = is.read(buffer)) != -1) {
			fos.write(buffer, 0, count);
			fos.flush();
		}
		is.close();
		fos.close();
	}

	private boolean checkHelpExist() {
		File file = new File(helpPath + helpName);
		if (file.exists())
			return true;
		else
			return false;
	}

	private void copyHelpFile() throws IOException {
		File fileDir = new File(helpPath);
		if (!fileDir.exists())
			fileDir.mkdir();
		File outputFile = new File(helpPath + helpName);
		FileOutputStream fos = new FileOutputStream(outputFile);
		InputStream is = getResources().openRawResource(R.raw.help);
		byte[] buffer = new byte[8192];
		int count = -1;
		while ((count = is.read(buffer)) != -1) {
			fos.write(buffer, 0, count);
			fos.flush();
		}
		is.close();
		fos.close();
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
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }
}
