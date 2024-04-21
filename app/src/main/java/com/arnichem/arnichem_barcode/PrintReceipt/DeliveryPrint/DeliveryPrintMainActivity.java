package com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DeliveryPrintMainActivity extends AppCompatActivity {
    ArrayList<String> id, dcno;
    RecyclerView recyclerView;
    PrintRecyAdapter delhisadapter;
    DeliveryPrintDB delhisdb;
    TextView dateView;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_print_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id=new ArrayList<String>();
        dcno=new ArrayList<String>();
        getSupportActionBar().setTitle("Delivery Print History");
        recyclerView=findViewById(R.id.recyclerViewdelhis);
        dateView=findViewById(R.id.dateshow);
        SimpleDateFormat df = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = new SimpleDateFormat("dd-MMM-YYY", Locale.getDefault());
        }
        String datestr=df.format(new Date());
        dateView.setText(datestr);
        delhisdb=new DeliveryPrintDB(this);
        storeDataInArrays();
        delhisadapter = new PrintRecyAdapter(DeliveryPrintMainActivity.this,this, id, dcno);
        recyclerView.setAdapter(delhisadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeliveryPrintMainActivity.this));
    }
    void storeDataInArrays(){
        Cursor cursor = delhisdb.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);

        }else{
            while (cursor.moveToNext()){
                Toast.makeText(DeliveryPrintMainActivity.this, ""+cursor.getString(6), Toast.LENGTH_SHORT).show();
                id.add(cursor.getString(0));
                dcno.add(cursor.getString(6));

            }


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DeliveryPrintMainActivity.this, MainPrintActivity.class));
    }
}