package com.arnichem.arnichem_barcode.PrintReceipt.GodownDeliveryPrintActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.DeliveryPrintDB;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.DeliveryPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.PrintRecyAdapter;
import com.arnichem.arnichem_barcode.PrintReceipt.MainPrintActivity;
import com.arnichem.arnichem_barcode.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GodownDeliveryMainActivity extends AppCompatActivity {
    ArrayList<String> id, dcno;
    RecyclerView recyclerView;
    GodownDeliveryPrintAdapter delhisadapter;
    GodownDelPrintDB delhisdb;
    TextView dateView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_godown_delivery_main2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id=new ArrayList<String>();
        dcno=new ArrayList<String>();
        getSupportActionBar().setTitle("Godown Delivery Print History");
        recyclerView=findViewById(R.id.recyclerViewdelhis);
        dateView=findViewById(R.id.dateshow);
        SimpleDateFormat df = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = new SimpleDateFormat("dd-MMM-YYY", Locale.getDefault());
        }
        String datestr=df.format(new Date());
        dateView.setText(datestr);
        delhisdb=new GodownDelPrintDB(this);
        storeDataInArrays();
        delhisadapter = new GodownDeliveryPrintAdapter(GodownDeliveryMainActivity.this,this, id, dcno);
        recyclerView.setAdapter(delhisadapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GodownDeliveryMainActivity.this));
    }
    void storeDataInArrays(){
        Cursor cursor = delhisdb.readAllData();
        if(cursor.getCount() == 0){
//            empty_imageview.setVisibility(View.VISIBLE);

        }else{
            while (cursor.moveToNext()){

                id.add(cursor.getString(0));
                dcno.add(cursor.getString(2));

            }


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(GodownDeliveryMainActivity.this, MainPrintActivity.class));
    }
}