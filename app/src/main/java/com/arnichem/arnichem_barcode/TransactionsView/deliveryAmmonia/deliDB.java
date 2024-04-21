package com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia;

import static java.sql.Types.DOUBLE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class deliDB  extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "deliDBammonia.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_cylname = "cylname";
    private static final String COLUMN_vol = "vol";
    private static final String COLUMN_full = "fullcl";

    private static final String COLUMN_empty = "empty";
    private static final String COLUMN_net = "net";
    private static final String IS_SCAN = "is_scan";

    private static final String COLUMN_Fill = "fill";





    public deliDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_cylname + " TEXT UNIQUE, " +
                COLUMN_full + " TEXT, " +
                COLUMN_empty+ " TEXT, " +
                COLUMN_net + " TEXT, " +
                COLUMN_Fill   + " TEXT, " +
                COLUMN_vol + " TEXT, " +
                IS_SCAN + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addBook(String cyname,String full,String empty,String net,String fill_with,String vol,String is_scan){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_cylname, cyname);
        cv.put(COLUMN_full, full);
        cv.put(COLUMN_empty, empty);
        cv.put(COLUMN_Fill, fill_with);
        cv.put(COLUMN_net, net);
        cv.put(COLUMN_vol, vol);
        cv.put(IS_SCAN, is_scan);

        long result = db.insertWithOnConflict(TABLE_NAME,null, cv,SQLiteDatabase.CONFLICT_REPLACE);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            //  Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();

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

    void updateData(String row_id, String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_cylname, title);
//        cv.put(COLUMN_AUTHOR, author);
//        cv.put(COLUMN_PAGES, pages);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }

    }
//
public Cursor readcount(){
    String query = "SELECT "+COLUMN_ID+",SUM("+COLUMN_vol+"),COUNT("+COLUMN_Fill+"),"+COLUMN_Fill+","+COLUMN_vol+" FROM " + TABLE_NAME+" GROUP BY "+COLUMN_Fill;
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

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

}
