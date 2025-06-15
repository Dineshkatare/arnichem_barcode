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

public class DurasyncHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "durasync.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "myscan";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_item_code = "item_code";
    private static final String COLUMN_barcode = "barcode";
    private static final String COLUMN_weight = "weight";
    private static final String COLUMN_volume = "volume";
    private static final String filled_with = "filled_with";



    public DurasyncHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_item_code + " TEXT UNIQUE, " +
                COLUMN_barcode + " TEXT, " +
                COLUMN_weight + " TEXT, " +
                COLUMN_volume + " TEXT, " +
                filled_with + " TEXT);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBook(String title, String author, String weight, String volume, String fillwith){


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_item_code, title);
        cv.put(COLUMN_barcode, author);
        cv.put(COLUMN_weight, weight);
        cv.put(COLUMN_volume, volume);
        cv.put(filled_with, fillwith);
        long result = db.insertWithOnConflict(TABLE_NAME,null, cv,SQLiteDatabase.CONFLICT_REPLACE);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            //   Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
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

    public List<ItemCode> searchAllData(String searchStr){
        List<ItemCode> itemCodes=null;
        try
        {
            SQLiteDatabase sqLiteDatabase=getReadableDatabase();
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " where "+COLUMN_item_code+" LIKE ?",new String[]{"%"+searchStr+"%"});
            if(cursor.moveToNext())
            {
                itemCodes=new ArrayList<>();
                do {
                    ItemCode itemCode=new ItemCode();
                    itemCode.setItem_Code(cursor.getString(1));
                    itemCodes.add(itemCode);

                }while (cursor.moveToNext());
            }
        }
        catch (Exception e){
            itemCodes=null;
        }
        return  itemCodes;
    }


    public List<String> getAllLabels(){
        List<String> list = new ArrayList<String>();

        list.add("Dura निवडा ");
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);//selectQuery,selectedArguments

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(1));//adding 2nd column data
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning lables
        return list;
    }

//    public Cursor getcheck(String name,SQLiteDatabase sqLiteDatabase)
//    {
//        String[] proj={COLUMN_item_code};
//        String selection=COLUMN_barcode+" LIKE ?";
//        String[] selection_args={COLUMN_barcode};
//        Cursor cursor= sqLiteDatabase.query(TABLE_NAME,proj,selection,selection_args,null,null,null);
//        return cursor;
//    }

    public String getcheck(String name)
    {
        String res = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c=db.rawQuery("select * from myscan where item_code='"+name+"'",null);
        if(c.getCount()==0)
        {
            Toast.makeText(context, "No Data FOund", Toast.LENGTH_SHORT).show();
        }

        StringBuffer buffer=new StringBuffer();
        while (c.moveToNext())
        {
            buffer.append(c.getString(0));
            buffer.append(c.getString(1));
            res=c.getString(1);
        }
        Toast.makeText(context, "buffer"+buffer, Toast.LENGTH_SHORT).show();
        return res;
    }
    void updateData(String row_id, String title){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_item_code, title);
//        cv.put(COLUMN_AUTHOR, author);
//        cv.put(COLUMN_PAGES, pages);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }

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

    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

}