package com.example.pulsmesser;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseManager {
    private ConfigReader configReader;
    private PulseDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Timer aggregationTimer;

    public DatabaseManager(Context context) {
        configReader = new ConfigReader();
        dbHelper = new PulseDatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        startDataAggregation();
    }


    public void insertPulseData_PULSE(float entry ) {
        ContentValues values = new ContentValues();
        values.put(PulseEntry.COLUMN_NAME_USERNAME, configReader.getUsername());
        values.put(PulseEntry.COLUMN_NAME_DATE, getCurrentDateTime());
        values.put(PulseEntry.COLUMN_NAME_PULSE, entry);

        db.insert(PulseEntry.TABLE_NAME, null, values);
    }
    public void insertPulseData_O2(float entry ) {
        ContentValues values = new ContentValues();
        values.put(O2Entry.COLUMN_NAME_USERNAME, configReader.getUsername());
        values.put(O2Entry.COLUMN_NAME_DATE, getCurrentDateTime());
        values.put(O2Entry.COLUMN_NAME_O2, entry);

        db.insert(O2Entry.TABLE_NAME, null, values);
    }

    public void close() {
        if (aggregationTimer != null) {
            aggregationTimer.cancel();
        }
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

    }
    public static class O2Entry implements BaseColumns {
        public static final String TABLE_NAME = "o2_data";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_O2 = "oxygen";

    }

    private static final String SQL_CREATE_ENTRIES_PULSE =
            "CREATE TABLE " + PulseEntry.TABLE_NAME + " (" +
                    PulseEntry._ID + " INTEGER PRIMARY KEY," +
                    PulseEntry.COLUMN_NAME_USERNAME + " TEXT," +
                    PulseEntry.COLUMN_NAME_DATE + " TEXT," +
                    PulseEntry.COLUMN_NAME_PULSE + " FLOAT)";

    private static final String SQL_CREATE_ENTRIES_O2 =
            "CREATE TABLE " + O2Entry.TABLE_NAME + " (" +
                    O2Entry._ID + " INTEGER PRIMARY KEY," +
                    O2Entry.COLUMN_NAME_USERNAME + " TEXT," +
                    O2Entry.COLUMN_NAME_DATE + " TEXT," +
                    O2Entry.COLUMN_NAME_O2 + " FLOAT)";

    private static final String SQL_DELETE_ENTRIES_PULSE =
            "DROP TABLE IF EXISTS " + PulseEntry.TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_O2 =
            "DROP TABLE IF EXISTS " + O2Entry.TABLE_NAME;

    private static class PulseDatabaseHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PulseDatabase.db";

        public PulseDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(SQL_CREATE_ENTRIES_PULSE);
            db.execSQL(SQL_CREATE_ENTRIES_O2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES_PULSE);
            db.execSQL(SQL_CREATE_ENTRIES_O2);
            onCreate(db);
        }
    }

    private void startDataAggregation() {
        aggregationTimer = new Timer();
        aggregationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                aggregateData();
            }
        }, 0, 10000); // Alle 10 Sekunden
    }

    private void aggregateData() {

    }

}
