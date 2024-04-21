package com.arnichem.arnichem_barcode.view;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DuraUpdateInfo extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "duraupdateinfo.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "myduraupdateinfo";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_duracode = "duracode";
    private static final String COLUMN_full_wt = "full_wt";
    private static final String COLUMN_empty_wt= "empty_wt";
    private static final String COLUMN_net_wt = "net_wt";
    private static final String COLUMN_cubic = "cubic";

    public DuraUpdateInfo(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_duracode + " TEXT, " +
                COLUMN_full_wt + " TEXT, " +
                COLUMN_empty_wt + " TEXT, " +
                COLUMN_net_wt + " TEXT, " +
                COLUMN_cubic + " TEXT);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}