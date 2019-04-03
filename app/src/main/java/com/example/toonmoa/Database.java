package com.example.toonmoa;

import android.provider.BaseColumns;

public final class Database {
    public static final class CreateDB implements BaseColumns {
        public static final String ID = "id";
        public static final String NEWEST_TITLE = "newest_title";
        public static final String _TABLENAME0 = "webtoon";
        public static final String _CREATE0 = "create table if not exists "+_TABLENAME0+"("
                + ID + " integer primary key not null, "
                + NEWEST_TITLE + " text not null);";
    }
}
