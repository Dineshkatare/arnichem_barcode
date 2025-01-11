package com.arnichem.arnichem_barcode.PrintReceipt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.DeliveryPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.DeliveryPrint.ViewDeliveryPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.DuraDeliveyPrint.DuraDelPrintMain;
import com.arnichem.arnichem_barcode.PrintReceipt.DuraEmptyPrint.DuraEmptyPrintMain;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintAdapter;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.EmptyPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.EmptyPrint.ViewEmptyPrint;
import com.arnichem.arnichem_barcode.PrintReceipt.FullReceipt.FullReceiptPrintMain;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownDeliveryPrintActivity.GodownDeliveryMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownEmptyPrintActivity.GodownEmptyPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.GodownFullReceiptmain.GodownFullRecPrintMain;
import com.arnichem.arnichem_barcode.PrintReceipt.InwardPrint.InwardPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.OutwardPrint.OutwardPrintMainActivity;
import com.arnichem.arnichem_barcode.PrintReceipt.OutwardPrint.outwardPrintVIew;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.TransactionsView.Transactions;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainPrintActivity extends AppCompatActivity {
    Spinner spinnermanifold;
    ArrayAdapter<CharSequence> adapter;
    String spinnerval,strNumber;
    public int manifoldpos;
    Button button;
    EditText etDcEmpNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_print2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Print History Version 8.3.1");
        spinnermanifold=findViewById(R.id.spinermanifold);
        etDcEmpNo = findViewById(R.id.etvDcEmpNo);
        button=findViewById(R.id.printRequest);
        adapter= ArrayAdapter.createFromResource(this,R.array.selectCategory, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnermanifold.setAdapter(adapter);
        if(manifoldpos!=0)
        {
            spinnermanifold.setSelection(manifoldpos);
        }

        spinnermanifold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ///  Log.v("item", (String) parent.getItemAtPosition(position));
                spinnerval=(String) parent.getItemAtPosition(position);
                if(spinnerval.equalsIgnoreCase("Delivery")) {
                    etDcEmpNo.setHint("Enter DC No");
                }else if(spinnerval.equalsIgnoreCase("Empty")){
                    etDcEmpNo.setHint("Enter EMPB No");
                }
                else{
                    etDcEmpNo.setHint("Enter No");
                }
                manifoldpos=position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(manifoldpos));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               postVal();
            }
        });
    }

    private void postVal() {
        strNumber = etDcEmpNo.getText().toString();
        if (manifoldpos == 0) {
            MDToast.makeText(MainPrintActivity.this, "कृपया category निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        }
        else if(TextUtils.isEmpty(strNumber))
        {
            MDToast.makeText(MainPrintActivity.this, "Empty Number !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

        }
        else {
            String type;
            if(spinnerval.equalsIgnoreCase("Delivery"))
            {
                type = "DEL";
                Intent i = new Intent(MainPrintActivity.this, ViewDeliveryPrint.class);
                i.putExtra("no",strNumber);
                i.putExtra("type",type);
                startActivity(i);
                etDcEmpNo.setText("");
                etDcEmpNo.setHint("Enter DC No");
            }
            else if(spinnerval.equalsIgnoreCase("Empty"))
            {
                type = "EMP";
                Intent i = new Intent(MainPrintActivity.this, ViewEmptyPrint.class);
                i.putExtra("no",strNumber);
                i.putExtra("type",type);
                startActivity(i);
                etDcEmpNo.setText("");
                etDcEmpNo.setHint("Enter EMPB No");
            }
            else
            {
                MDToast.makeText(MainPrintActivity.this, "Please Select Category!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                return;
            }

        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }


}
