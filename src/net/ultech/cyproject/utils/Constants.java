package net.ultech.cyproject.utils;

public final class Constants {

	public final class FragmentList {
		public static final int STANDARD_MODE = 0;
		public static final int CHALLENGE_MODE = 1;
		public static final int QUERY_MODE = 2;
		public static final int HIGH_RECORD = 3;
		public static final int PERSONAL_SETTINGS = 4;
		public static final int HELP = 5;
		public static final int ABOUT_US = 6;
	}

	public final static String PREFERENCE_FILE_NAME = "setting";
	public final static String DATABASE_FILE_NAME = "cydb.db";
	public final static String LOG_FILE_NAME = "st.log";
	public final static String RECORD_FILE_NAME = "ch.record";

	public final class PreferenceName {
		public static final String BOOL_FIRSTUSE = "firstUse";
		public static final String STRING_DEFAULT_USERNAME = "ch_defaultUsername";
		public static final String INT_LEVEL = "st_savedLevel";
		public static final String STRING_TIME_OR_LIFE = "ch_savedPrimary";
		public static final String STRING_LAST_QUERY = "last_query";
		public static final String STRING_ST_TEXT_HUMAN = "st_savedTextHuman";
		public static final String STRING_ST_TEXT_ROBOT = "st_savedTextRobot";
		public static final String STRING_APPEARANCE = "appearance";
		public static final String INT_LAST_FRAGMENT = "last_fragment";
		public static final String INT_LATEST_VERSION = "latest_version";
		public static final String BOOL_SYSTEM_UI = "use_system_ui";
		public static final String BOOL_CLOSE_SFX = "close_sfx";
		public static final String BOOL_CLOSE_MUSIC = "close_music";
	}

	public final class UpdateRelated {
		public final static String UPDATE_PATH = "http://ultech.tk/cy/update.php";
		public final static String VERSION_CODE_PATH = "http://ultech.tk/cy/version.php";
		public final static String FEEDBACK_PATH = "http://ultech.tk/cy/feedback.php";
		public final static String RELEASE_DATE = "2014/8/22";
	}

	public final class Icons {
		public static final String ICON_ORIGINAL_ACTIVITY_NAME = "net.ultech.cyproject.ui.MainActivity-Original";
		public static final String ICON_ORANGE_ACTIVITY_NAME = "net.ultech.cyproject.ui.MainActivity-Orange";
		public static final int ICON_ORIGINAL = 0;
		public static final int ICON_ORANGE = 1;
	}

	public final class Mode {
		public static final int MODE_STANDARD = 0;
		public static final int MODE_QUERY = 1;
	}

}
