package net.ultech.cyproject.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.ultech.cyproject.R;
import net.ultech.cyproject.dao.CYDbOpenHelper;
import net.ultech.cyproject.ui.fragment.AboutUs;
import net.ultech.cyproject.ui.fragment.Help;
import net.ultech.cyproject.ui.fragment.HighRecord;
import net.ultech.cyproject.ui.fragment.PersonalSettings;
import net.ultech.cyproject.ui.fragment.QueryMode;
import net.ultech.cyproject.ui.fragment.StandardMode;
import net.ultech.cyproject.ui.fragment.ResultList;
import net.ultech.cyproject.ui.fragment.StandardModeLog;
import net.ultech.cyproject.utils.AbsActivity;
import net.ultech.cyproject.utils.Constants;
import net.ultech.cyproject.utils.Constants.FragmentList;
import net.ultech.cyproject.utils.Constants.Icons;
import net.ultech.cyproject.utils.Constants.PreferenceName;
import net.ultech.cyproject.utils.Constants.UpdateRelated;
import net.ultech.cyproject.utils.DatabaseHolder;
import net.ultech.cyproject.utils.MainActivityStack;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AbsActivity {

	public static int THEME_CHANGE = 0;
	public static int UPDATE_REMINDER = 1;
	public static int RELOAD_SETTINGS = 2;
	public static int RECREATE = 3;

	private String databasePath;
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

	@SuppressLint("HandlerLeak")
	// FIXME Handler Leak
	// FIXME 还有一处，我找不到了
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == THEME_CHANGE) {
				int icon = msg.arg1;
				PackageManager pm = getPackageManager();
				ActivityManager am = (ActivityManager) getSystemService(MainActivity.ACTIVITY_SERVICE);
				pm.setComponentEnabledSetting(
						new ComponentName(MainActivity.this,
								Icons.ICON_ORIGINAL_ACTIVITY_NAME),
						icon == Icons.ICON_ORIGINAL ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
								: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
				pm.setComponentEnabledSetting(
						new ComponentName(MainActivity.this,
								Icons.ICON_ORANGE_ACTIVITY_NAME),
						icon == Icons.ICON_ORANGE ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
								: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_HOME);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
				for (ResolveInfo res : resolves) {
					if (res.activityInfo != null) {
						am.killBackgroundProcesses(res.activityInfo.packageName);
					}
				}
				recreate();
			} else if (msg.what == UPDATE_REMINDER) {
				Toast.makeText(MainActivity.this, R.string.update_reminder,
						Toast.LENGTH_LONG).show();
			} else if (msg.what == RELOAD_SETTINGS) {
				PersonalSettings settings = new PersonalSettings();
				mFragments[FragmentList.PERSONAL_SETTINGS] = settings;
				mManager.beginTransaction()
						.replace(R.id.main_content_frame, settings).commit();
			} else if (msg.what == RECREATE) {
				recreate();
			}
		};
	};

	@SuppressLint("SdCardPath")
	@SuppressWarnings("deprecation")
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
			final Dialog dialog = new Dialog(this, R.style.fullscreenDialog);

			View hintView = View.inflate(this, R.layout.main_hint_view, null);
			ImageView imageView = (ImageView) hintView
					.findViewById(R.id.main_hint_finger);
			Animation animation = AnimationUtils.loadAnimation(this,
					R.anim.finger_translate);
			animation.setRepeatCount(Animation.INFINITE);
			imageView.startAnimation(animation);
			Button hintViewButton = (Button) hintView
					.findViewById(R.id.main_hint_dismiss_button);
			hintViewButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					sp.edit().putBoolean(PreferenceName.BOOL_FIRSTUSE, false)
							.commit();
					dialog.dismiss();
				}
			});

			dialog.setContentView(hintView);
			Window dialogWindow = dialog.getWindow();
			LayoutParams lp = dialogWindow.getAttributes();
			lp.width = getWindowManager().getDefaultDisplay().getWidth();
			lp.height = getWindowManager().getDefaultDisplay().getHeight();
			dialogWindow.setAttributes(lp);
			dialog.show();
		} else {
			new Thread() {
				public void run() {
					InputStream is = null;
					HttpURLConnection connection = null;
					try {
						// Connect Web
						URL url = new URL(UpdateRelated.VERSION_CODE_PATH);
						connection = (HttpURLConnection) url.openConnection();
						connection.setReadTimeout(5000);
						connection.setRequestMethod("GET");
						connection.setConnectTimeout(3000);
						is = connection.getInputStream();
						byte[] buffer = new byte[1024];
						String stringBuffer, stringResult;
						stringResult = new String();
						while (is.read(buffer) > 0) {
							stringBuffer = new String(buffer);
							stringResult = stringResult + stringBuffer;
						}
						stringResult = stringResult.trim();
						int newVersionCode = Integer.parseInt(stringResult);
						Log.d("UpdateChecker", "New version code: "
								+ newVersionCode);
						sp.edit()
								.putInt(PreferenceName.INT_LATEST_VERSION,
										newVersionCode).commit();
						int versionCode = getPackageManager().getPackageInfo(
								getPackageName(), 0).versionCode;
						Log.d("UpdateChecker", "Old version code: "
								+ versionCode);
						if (versionCode < newVersionCode) {
							Message msg = new Message();
							msg.what = UPDATE_REMINDER;
							handler.sendMessage(msg);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (NameNotFoundException e2) {
						e2.printStackTrace();
					} finally {
						if (connection != null)
							connection.disconnect();
						if (is != null) {
							try {
								is.close();
							} catch (IOException e3) {
								e3.printStackTrace();
							}
						}
					}
				};
			}.start();
		}

		databasePath = "/data/data/" + getPackageName() + "/databases/";
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
				FragmentList.HELP);
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
				playSound("click");
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
					startActivityForResult(intent_challenge, 0);
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
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		playSound("click");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
				mDrawerLayout.closeDrawers();
			else {
				if (mActivityStack.getCount() == 1)
					finish();
				else {
					mActivityStack.popBack();
					updateFragment();
				}
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

		@SuppressLint("ViewHolder")
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
		String path = databasePath + Constants.DATABASE_FILE_NAME;
		System.out.println(path);
		File file = new File(path);
		if (!file.exists()) {
			copyDatabase();
		}
		mHelper = new CYDbOpenHelper(this, Constants.DATABASE_FILE_NAME);
		DatabaseHolder.putDatabase(mHelper.getReadableDatabase());
	}

	private void copyDatabase() {
		File path = new File(databasePath);
		if (!path.exists()) {
			path.mkdir();
		}
		ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setTitle(getResources().getString(R.string.initializing));
		pDialog.setIndeterminate(false);
		pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDialog.setCancelable(false);
		pDialog.show();

		try {
			InputStream is = getAssets().open(Constants.DATABASE_FILE_NAME);
			int available = is.available();
			pDialog.setMax(available);
			FileOutputStream fos = new FileOutputStream(new File(databasePath
					+ Constants.DATABASE_FILE_NAME));
			byte[] buffer = new byte[1024];
			int read_length = 0;
			int count = -1;
			while ((count = is.read(buffer)) != -1) {
				fos.flush();
				read_length += count;
				if (read_length < available)
					pDialog.setProgress(read_length);
				fos.write(buffer, 0, count);
			}
			fos.flush();
			fos.close();
			is.close();
			pDialog.cancel();
		} catch (IOException e) {
			Log.e("Database", "Copy to Internal Storage Error");
			Log.wtf("Database", "Database copy all failes");
			e.printStackTrace();
			throw new RuntimeException("ABORT");
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

	public String getFragmentTitle(Fragment fragment) {
		int id = getFragmentId(fragment);
		if (id != -1)
			return mDrawerItemNames[id];
		else {
			if (fragment instanceof ResultList)
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

	public Fragment getFragmentById(int id) {
		return mFragments[id];
	}
}
