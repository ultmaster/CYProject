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

    public final class PreferenceName {
        public static final String BOOL_FIRSTUSE = "firstUse";
    }

    public static enum DatabaseLocation {
        INTERNAL, EXTERNAL, BOTH
    }

}
