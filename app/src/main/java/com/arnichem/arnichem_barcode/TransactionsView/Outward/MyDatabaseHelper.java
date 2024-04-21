package com.arnichem.arnichem_barcode.TransactionsView.Outward;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "Outward.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String CYCLINDER_NO = "cyclinder_no";
    private static final String FILL_WITH = "fill_with";
    private static final String Volume = "volume";
    private static final String STATUS = "staus";


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CYCLINDER_NO + " TEXT UNIQUE, " +
                FILL_WITH + " TEXT, " +
                Volume + " TEXT, " +
                STATUS + " TEXT);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    public void addBook(String title,String Fill,String Volume1,String status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(CYCLINDER_NO, title);
        cv.put(FILL_WITH, Fill);
        cv.put(STATUS, status);
        cv.put(Volume, Volume1);
        long result = db.insertWithOnConflict(TABLE_NAME,null, cv,SQLiteDatabase.CONFLICT_REPLACE);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
        }

    }

    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME+" ORDER BY "+CYCLINDER_NO+" ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    void deleteOneRow(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Successfully Deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readcount(){
        String query = "SELECT "+COLUMN_ID+",SUM("+Volume+"),COUNT("+FILL_WITH+"),"+FILL_WITH+","+Volume+" FROM " + TABLE_NAME+" GROUP BY "+FILL_WITH;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

}