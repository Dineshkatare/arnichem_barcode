package com.arnichem.arnichem_barcode.Company;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CompanyHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "companyHelper";
    private static final String TABLE_NAME = "labels";

    private static final String COLUMN_ID = "id";
    private static final String COMPANY_ID = "companyID";
    private static final String COMPANY_SHORT_NAME = "companyShortName";
    private static final String COMPANY_FULL_NAME = "companyFullName"; // fixed typo
    private static final String DB_HOST = "dbHost";
    private static final String DB_USERNAME = "dbUsername";
    private static final String DB_PASSWORD = "dbPassword";
    private static final String DB_NAME = "dbName";
    private static final String DB_BASE_URL = "dbBaseUrl";
    private static final String DB_TERMS_TEXT = "termsText";
    private static final String DB_OWN_CODE = "owncode";
    private static final String DB_BATCH_PREMIX = "batch_premix";
    private static final String DB_CYC_PREMIX = "cyc_premix";
    private static final String LOGIN_MSG = "login_msg";

    private final Context context;

    public CompanyHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COMPANY_ID + " TEXT UNIQUE, " +
                COMPANY_SHORT_NAME + " TEXT, " +
                COMPANY_FULL_NAME + " TEXT, " +
                DB_HOST + " TEXT, " +
                DB_USERNAME + " TEXT, " +
                DB_PASSWORD + " TEXT, " +
                DB_BASE_URL + " TEXT, " +
                DB_NAME + " TEXT, " +
                DB_TERMS_TEXT + " TEXT, " +
                DB_OWN_CODE + " TEXT, " +
                DB_BATCH_PREMIX + " TEXT, " +
                DB_CYC_PREMIX + " TEXT, " +
                LOGIN_MSG + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public void addCompany(String company_id, String company_short_name, String company_full_name,
                           String db_host, String db_username, String db_password,
                           String db_name, String base_url, String terms_text,
                           String own_code, String batch_premix, String cyc_premix,
                           String login_msg) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COMPANY_ID, company_id);
        cv.put(COMPANY_SHORT_NAME, company_short_name);
        cv.put(COMPANY_FULL_NAME, company_full_name);
        cv.put(DB_HOST, db_host);
        cv.put(DB_USERNAME, db_username);
        cv.put(DB_PASSWORD, db_password);
        cv.put(DB_NAME, db_name);
        cv.put(DB_BASE_URL, base_url);
        cv.put(DB_TERMS_TEXT, terms_text);
        cv.put(DB_OWN_CODE, own_code);
        cv.put(DB_BATCH_PREMIX, batch_premix);
        cv.put(DB_CYC_PREMIX, cyc_premix);
        cv.put(LOGIN_MSG, login_msg);

        long result = db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (result == -1) {
            Toast.makeText(context, "Failed to insert company", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public List<String> getAllLabels() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COMPANY_FULL_NAME)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
}
