package com.example.pulsmesser;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseManager {
    private PulseDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        dbHelper = new PulseDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void insertPulseData(String username, double pulse, double oxygen) {
        ContentValues values = new ContentValues();
        values.put(PulseEntry.COLUMN_NAME_USERNAME, username);
        values.put(PulseEntry.COLUMN_NAME_DATE, getCurrentDateTime());
        values.put(PulseEntry.COLUMN_NAME_PULSE, pulse);
        values.put(PulseEntry.COLUMN_NAME_OXYGEN, oxygen);

        db.insert(PulseEntry.TABLE_NAME, null, values);
    }

    public void close() {
        dbHelper.close();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static class PulseEntry implements BaseColumns {
        public static final String TABLE_NAME = "pulse_data";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_PULSE = "pulse";
        public static final String COLUMN_NAME_OXYGEN = "oxygen";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PulseEntry.TABLE_NAME + " (" +
                    PulseEntry._ID + " INTEGER PRIMARY KEY," +
                    PulseEntry.COLUMN_NAME_USERNAME + " TEXT," +
                    PulseEntry.COLUMN_NAME_DATE + " TEXT," +
                    PulseEntry.COLUMN_NAME_PULSE + " REAL," +
                    PulseEntry.COLUMN_NAME_OXYGEN + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PulseEntry.TABLE_NAME;

    private static class PulseDatabaseHelper extends SQLiteOpenHelper {
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
