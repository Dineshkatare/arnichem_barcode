package com.arnichem.arnichem_barcode.TransactionsView.deliverynew;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class deliDB extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "delidb.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "book_title";
    private static final String COLUMN_Fill = "book_author";
    private static final String COLUMN_Volume= "book_pages";
    private static final String STATUS= "status";
    private static final String COLUMN_TIMESTAMP = "timestamp"; // New column for timestamp


    public deliDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


       @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT UNIQUE, " +
                COLUMN_Fill + " TEXT, " +
                COLUMN_Volume + " TEXT, " +
                STATUS + " TEXT, " +
                COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        }
    }

    public void addBook(String title,String Fill,String Volume,String status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_Fill, Fill);
        cv.put(COLUMN_Volume, Volume);
        cv.put(STATUS, status);

        long result = db.insertWithOnConflict(TABLE_NAME,null, cv,SQLiteDatabase.CONFLICT_REPLACE);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            //  Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();

    }

    public Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME +" ORDER BY "+COLUMN_TITLE+" ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
    public Cursor readAllDataInFIFOOrder() {
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TIMESTAMP + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db != null ? db.rawQuery(query, null) : null;
    }


    void updateData(String row_id, String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, title);
//        cv.put(COLUMN_AUTHOR, author);
//        cv.put(COLUMN_PAGES, pages);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }

    }
    public Cursor readcount(){
        String query = "SELECT "+COLUMN_ID+",SUM("+COLUMN_Volume+"),COUNT("+COLUMN_Fill+"),"+COLUMN_Fill+","+COLUMN_Volume+" FROM " + TABLE_NAME+" GROUP BY "+COLUMN_Fill;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void deleteOneRow(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

}