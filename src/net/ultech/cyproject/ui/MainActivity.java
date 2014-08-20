package net.ultech.cyproject.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.fragment.AboutUs;
import net.ultech.cyproject.ui.fragment.Help;
import net.ultech.cyproject.ui.fragment.HighRecord;
import net.ultech.cyproject.ui.fragment.PersonalSettings;
import net.ultech.cyproject.ui.fragment.QueryMode;
import net.ultech.cyproject.ui.fragment.StandardMode;
import net.ultech.cyproject.ui.fragment.StandardModeHint;
import net.ultech.cyproject.ui.fragment.StandardModeLog;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import net.ultech.cyproject.utils.DatabaseHolder;
import net.ultech.cyproject.utils.MainActivityStack;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AbsActivity {

	public static int RECREATE_MSG = 0;

	private ListView mListView;
	private String[] mDrawerItemNames;
	private String mTitle;
	private DrawerLayout mDrawerLayout;
	private SharedPreferences sp;
	private FragmentManager mManager;
	private FragmentTransaction mTransaction;
	private ActionBarDrawerToggle mDrawerToggle;
	public Fragment[] mFragments;
	public MainActivityStack mActivityStack;
	public int fragmentIndicator;
	private myListAdapter mAdapter;

	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == RECREATE_MSG)
				recreate();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mManager = getFragmentManager();
		mTitle = getString(R.string.app_name);
		mActivityStack = new MainActivityStack();

		sp = getSharedPreferences(Constants.PREFERENCE_FILE_NAME,
				Context.MODE_PRIVATE);
		boolean firstUse = sp.getBoolean(PreferenceName.BOOL_FIRSTUSE, true);
		if (firstUse) {
			copyDatabase();
			new AlertDialog.Builder(this)
					.setMessage(
							getResources().getString(
									R.string.first_use_welcome_text))
					.setPositiveButton(
							getResources().getString(
									R.string.first_use_accepted_welcome_text),
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
		mFragments[FragmentList.HELP] = new Help();
		mFragments[FragmentList.ABOUT_US] = new AboutUs();
		mFragments[FragmentList.PERSONAL_SETTINGS] = new PersonalSettings();
		int lastFragment = sp.getInt(PreferenceName.INT_LAST_FRAGMENT,
				FragmentList.ABOUT_US);
		mActivityStack.pushStack(null, mFragments[lastFragment], -1);
		fragmentIndicator = lastFragment;
		updateFragment();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		initializeDrawer();
		mListView = (ListView) findViewById(R.id.lv_main);
		mAdapter = new myListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case FragmentList.STANDARD_MODE:
				case FragmentList.QUERY_MODE:
				case FragmentList.HIGH_RECORD:
				case FragmentList.HELP:
				case FragmentList.ABOUT_US:
				case FragmentList.PERSONAL_SETTINGS:
					mActivityStack.pushStack(null, mFragments[position], 0);
					updateFragment();
					break;
				case FragmentList.CHALLENGE_MODE:
					Intent intent_challenge = new Intent(MainActivity.this,
							ChallengeMode.class);
					startActivity(intent_challenge);
					break;
				default:
					throw new RuntimeException("你干了什么？");
				}
				mDrawerLayout.closeDrawers();
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		while (mActivityStack.getCount() > 1)
			mActivityStack.popBack();
		Fragment fragment = mActivityStack.getBackFragment();
		int id = getFragmentId(fragment);
		if (id != -1)
			sp.edit().putInt(PreferenceName.INT_LAST_FRAGMENT, id).commit();
		else {
			sp.edit()
					.putInt(PreferenceName.INT_LAST_FRAGMENT,
							FragmentList.ABOUT_US).commit();
		}
		super.onDestroy();
	}

	public void updateFragment() {
		Fragment fragment = mActivityStack.getBackFragment();
		mTitle = getFragmentTitle(fragment);
		mTransaction = mManager.beginTransaction();
		mTransaction.setCustomAnimations(android.R.animator.fade_in,
				android.R.animator.fade_out, android.R.animator.fade_in,
				android.R.animator.fade_out);
		mTransaction.replace(R.id.main_content_frame, fragment);
		mTransaction.commit();
		int j = mActivityStack.getCount();
		int id = -1;
		while (id == -1) {
			id = getFragmentId(mActivityStack.getFragment(--j));
		}
		fragmentIndicator = id;
		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mActivityStack.getCount() == 1)
				finish();
			else {
				mActivityStack.popBack();
				updateFragment();
			}
		}
		return true;
	}

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
			View view_indicator = (View) view.findViewById(R.id.view_indicator);
			if (position == fragmentIndicator)
				view_indicator.setVisibility(View.VISIBLE);
			else {
				view_indicator.setVisibility(View.INVISIBLE);
			}
			return view;
		}
	}

	private void initializeDatabase() throws Exception {
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
		if (!getExternalFilesDir(null).exists()) {
			getExternalFilesDir(null).mkdir();
		}
		if (!getFilesDir().exists()) {
			getFilesDir().mkdir();
		}
		final ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setTitle(getResources().getString(R.string.initializing));
		pDialog.setIndeterminate(false);
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setCancelable(false);
		pDialog.show();

		new Thread() {
			public void run() {
				try {
					InputStream is = getAssets().open(
							Constants.DATABASE_FILE_NAME);
					int available = is.available();
					pDialog.setMax(available);
					FileOutputStream fos = new FileOutputStream(
							getExternalFilesDir(null).getAbsolutePath() + "/"
									+ Constants.DATABASE_FILE_NAME);
					byte[] buffer = new byte[1024];
					int read_length = 0;
					int count = 0;
					while ((count = is.read(buffer)) > 0) {
						read_length += count;
						pDialog.setProgress(read_length);
						fos.write(buffer, 0, count);
					}
					fos.flush();
					fos.close();
					is.close();
					pDialog.cancel();
				} catch (IOException e1) {
					Log.e("Database", "Copy to External Storage Error");
					e1.printStackTrace();
					try {
						InputStream is = getAssets().open(
								Constants.DATABASE_FILE_NAME);
						FileOutputStream fos = new FileOutputStream(
								getDatabasePath(null).getAbsolutePath() + "/"
										+ Constants.DATABASE_FILE_NAME);
						byte[] buffer = new byte[1024];
						int read_length = 0;
						int count = 0;
						while ((count = is.read(buffer)) > 0) {
							read_length += count;
							pDialog.setProgress(read_length);
							fos.write(buffer, 0, count);
						}
						fos.flush();
						fos.close();
						is.close();
						pDialog.cancel();
					} catch (IOException e2) {
						Log.e("Database", "Copy to Internal Storage Error");
						Log.wtf("Database", "Database copy all failes");
						e2.printStackTrace();
						throw new RuntimeException("ABORT");
					}
				}
			};
		}.start();

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

	public String getFragmentTitle(Fragment fragment) {
		int id = getFragmentId(fragment);
		if (id != -1)
			return mDrawerItemNames[id];
		else {
			if (fragment instanceof StandardModeHint)
				return getResources().getString(R.string.st_hint_name);
			if (fragment instanceof StandardModeLog)
				return getResources().getString(R.string.st_log_name);
		}
		throw new RuntimeException("Type cannot be identified.");
	}

	/*
	 * get the value in Constants.FragmentList. If not exist, return -1
	 */
	public int getFragmentId(Fragment fragment) {
		if (fragment instanceof StandardMode)
			return FragmentList.STANDARD_MODE;
		else if (fragment instanceof QueryMode)
			return FragmentList.QUERY_MODE;
		else if (fragment instanceof HighRecord)
			return FragmentList.HIGH_RECORD;
		else if (fragment instanceof PersonalSettings)
			return FragmentList.PERSONAL_SETTINGS;
		else if (fragment instanceof Help)
			return FragmentList.HELP;
		else if (fragment instanceof AboutUs)
			return FragmentList.ABOUT_US;
		else {
			return -1;
		}
	}
}
