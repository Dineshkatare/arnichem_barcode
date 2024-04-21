package com.arnichem.arnichem_barcode.PrintReceipt.DuraDeliveyPrint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintAdapter;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintDB;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DuraDelPrintMain extends AppCompatActivity {
    ArrayList<String> id, dcno;
    RecyclerView recyclerView;
    DuraDeliPrintAdapter delhisadapter;
    DuraDeliveryPrintDB delhisdb;
    TextView dateView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dura_del_print_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id=new ArrayList<String>();
        dcno=new ArrayList<String>();
        getSupportActionBar().setTitle("Dura Delivery Print History");
        recyclerView=findViewById(R.id.recyclerViewdelhis);
        dateView=findViewById(R.id.dateshow);
        SimpleDateFormat df = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = new SimpleDateFormat("dd-MMM-YYY", Locale.getDefault());
        }
        String datestr=df.format(new Date());
        dateView.setText(datestr);
        delhisdb=new DuraDeliveryPrintDB(this);
        storeDataInArrays();
        delhisadapter = new DuraDeliPrintAdapter(DuraDelPrintMain.this,this, id, dcno);
        recyclerView.setAdapter(delhisadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DuraDelPrintMain.this));
    }
    void storeDataInArrays(){
        Cursor cursor = delhisdb.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);

        }else{
            while (cursor.moveToNext()){

                id.add(cursor.getString(0));
                dcno.add(cursor.getString(6));

            }


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DuraDelPrintMain.this, MainPrintActivity.class));
    }
}