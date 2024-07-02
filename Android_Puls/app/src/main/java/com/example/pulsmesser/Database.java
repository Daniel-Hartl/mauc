package com.example.pulsmesser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class Database {
    private Database() {}

    public static class PulseEntry implements BaseColumns {
        public static final String TABLE_NAME = "Puls_Eintraege";
        public static final String COLUMN_NAME_USERNAME = "Benutzername";
        public static final String COLUMN_NAME_TIME = "Messzeitpunkt";
        public static final String COLUMN_NAME_PULSE = "Puls";
        public static final String COLUMN_NAME_OXYGEN = "Sauerstoff";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PulseEntry.TABLE_NAME + " (" +
                    PulseEntry._ID + " INTEGER PRIMARY KEY," +
                    PulseEntry.COLUMN_NAME_USERNAME + " TEXT," +
                    PulseEntry.COLUMN_NAME_TIME + " TEXT," +
                    PulseEntry.COLUMN_NAME_PULSE + " REAL," +
                    PulseEntry.COLUMN_NAME_OXYGEN + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PulseEntry.TABLE_NAME;

    public static class PulseDatabaseHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PulseDatabase.db";

        public PulseDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}