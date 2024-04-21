package com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EmptyPrintMainActivity extends AppCompatActivity {
    ArrayList<String> id, dcno;
    RecyclerView recyclerView;
    EmptyPrintAdapter delhisadapter;
    EmptyPrintDB delhisdb;
    TextView dateView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_print_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id=new ArrayList<String>();
        dcno=new ArrayList<String>();
        getSupportActionBar().setTitle("Empty Print History");
        dateView=findViewById(R.id.dateshow);
        SimpleDateFormat df = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = new SimpleDateFormat("dd-MMM-YYY", Locale.getDefault());
        }
        String datestr=df.format(new Date());
        dateView.setText(datestr);
        recyclerView=findViewById(R.id.recyclerViewdelhis);
        delhisdb=new EmptyPrintDB(this);
        storeDataInArrays();
        delhisadapter = new EmptyPrintAdapter(EmptyPrintMainActivity.this,this, id, dcno);
        recyclerView.setAdapter(delhisadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(EmptyPrintMainActivity.this));
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
        startActivity(new Intent(EmptyPrintMainActivity.this, MainPrintActivity.class));
    }
}