package com.arnichem.arnichem_barcode.TransactionsView.deliveryAmmonia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class delhisdb extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "delhisdb.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_cylname = "cylname";
    private static final String COLUMN_dcno = "dcno";
    private static final String COLUMN_date = "date";
    private static final String COLUMN_code = "custcode";
    private static final String COLUMN_name = "custname";
    private static final String COLUMN_totalwei = "totalwei";
    private static final String COLUMN_totalquan = "totalquantity";
    private static final String COLUMN_vol = "vol";
    private static final String COLUMN_full = "fullcl";
    private static final String COLUMN_net = "net";


    public delhisdb(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_cylname + " TEXT UNIQUE, " +
                COLUMN_dcno + " TEXT, " +
                COLUMN_date + " TEXT, " +
                COLUMN_code + " TEXT, " +
                COLUMN_name + " TEXT, " +
                COLUMN_totalwei + " TEXT, " +
                COLUMN_totalquan + " TEXT, " +
                COLUMN_full + " TEXT, " +
                COLUMN_net + " TEXT, " +
                COLUMN_vol + " TEXT);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBook(String cyname, String cydc, String cydate, String custcode, String custname, String wei, String county, String full, String vol, String net){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_cylname, cyname);
        cv.put(COLUMN_dcno, cydc);
        cv.put(COLUMN_date, cydate);
        cv.put(COLUMN_code, custcode);
        cv.put(COLUMN_name, custname);
        cv.put(COLUMN_totalwei, wei);
        cv.put(COLUMN_totalquan, county);
        cv.put(COLUMN_full, full);
        cv.put(COLUMN_net, net);
        cv.put(COLUMN_vol, vol);
        long result = db.insertWithOnConflict(TABLE_NAME,null, cv,SQLiteDatabase.CONFLICT_REPLACE);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            //  Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(context, "तुमचा बारकोड नंबर सिलेंडर नंबर "+title+" शी जोडला आहे ", Toast.LENGTH_LONG).show();

    }
    Cursor readAllData(){
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
