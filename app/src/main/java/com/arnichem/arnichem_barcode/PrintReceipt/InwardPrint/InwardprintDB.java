package com.arnichem.arnichem_barcode.PrintReceipt.InwardPrint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class InwardprintDB extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "InwardprintDB.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "my_library";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_cylinder = "CylinderNumber";
    private static final String deliveryDate = "deliveryDate";
    private static final String customerName = "customerName";
    private static final String totalQuantity = "totalQuan";
    private static final String DC_NO = "DCNO";

    public InwardprintDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_cylinder + " TEXT, " +
                deliveryDate + " TEXT, " +
                customerName + " TEXT, " +
                totalQuantity + " TEXT, " +
                DC_NO + " TEXT);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBook(String cylinder,String deliDate,String cnane,String totalQ,String dcno){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_cylinder, cylinder);
        cv.put(deliveryDate, deliDate);
        cv.put(customerName, cnane);
        cv.put(totalQuantity, totalQ);
        cv.put(DC_NO, dcno);
        long result = db.insert(TABLE_NAME,null, cv);

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

}
