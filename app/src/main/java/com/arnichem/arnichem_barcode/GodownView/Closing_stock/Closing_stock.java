package com.arnichem.arnichem_barcode.GodownView.Closing_stock;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Barcode.NewScanner;
import com.arnichem.arnichem_barcode.GodownView.GOdownMainActivity;
import com.arnichem.arnichem_barcode.Producation.SearchAdapter;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.view.ItemCode;
import com.arnichem.arnichem_barcode.view.LocationHandler;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.arnichem.arnichem_barcode.view.fromloccodehandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.valdesekamdem.library.mdtoast.MDToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Closing_stock extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    ImageView empty_imageview;
    TextView usernamevalue, date, Totalscanvalue;
    closing_helper myDB;
    com.arnichem.arnichem_barcode.view.fromloccodehandler fromloccodehandler;
    ArrayList<String> book_id, book_title;
   Closing_Adapter customAdapter;
    SharedPreferences pref;
    Button button, inwardpint;
    ProgressDialog dialog;
    public String selected;
    Spinner spinner;
    boolean status = false;
    AutoCompleteTextView inwardmaincylindernumber;
    public int poslocfixdel;
    String from_warehouse, from_code, count, srno;
    ArrayAdapter<String> dataAdapter;
    static JSONObject object = null;
    List<String> cylinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_stock);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Stock");
        recyclerView = findViewById(R.id.recyclerView);

        cylinder = new ArrayList<String>();
        add_button = findViewById(R.id.add_fab);
        spinner = findViewById(R.id.spinfrominward);
        Totalscanvalue = findViewById(R.id.Totalscanvalue);
        inwardpint = findViewById(R.id.inwardprintbtn);
        inwardpint.setVisibility(View.GONE);
        empty_imageview = findViewById(R.id.empty_imageview);
        inwardmaincylindernumber = findViewById(R.id.inwardmaincylindernumber);
        poslocfixdel = Integer.parseInt(SharedPref.getInstance(this).getfrom_loc());
        fetchData();
        loadata();
        usernamevalue = findViewById(R.id.usernametxtvalue);
        date = findViewById(R.id.date);
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        button = findViewById(R.id.InWardMainPost);
        button.setEnabled(true);
        myDB = new closing_helper(Closing_stock.this);
        fromloccodehandler = new fromloccodehandler(Closing_stock.this);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status =true;
                Intent intent =new Intent(Closing_stock.this, NewScanner.class);
                intent.putExtra("type", "closing_stock");
                startActivity(intent);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                from_warehouse = dataAdapter.getItem(position);
                poslocfixdel = position;
                SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(poslocfixdel));
                Cursor cursor = fromloccodehandler.readAllData();
                if (cursor.getCount() == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    while (cursor.moveToNext()) {
                        String col = cursor.getString(1);
                        String col1 = cursor.getString(2);
                        if (col.contentEquals(from_warehouse)) {
                            from_code = col1;

                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                postUsingVolley();

            }
        });
        book_id = new ArrayList<>();
        book_title = new ArrayList<>();
        storeDataInArrays();
        Totalscanvalue.setText(count);
        customAdapter = new Closing_Adapter(Closing_stock.this, this, book_id, book_title);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(Closing_stock.this));
        inwardpint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Closing_stock.this, closing_stock_print.class);
                intent.putExtra("durano", String.valueOf(cylinder));
                intent.putExtra("empb", srno);
                intent.putExtra("count", count);
                intent.putExtra("cylinder", String.valueOf(cylinder));
                startActivity(intent);
            }
        });
    }



    private void fetchData() {
        fromloccodehandler db = new fromloccodehandler(getApplicationContext());
        List<String> labels = db.getAllLabels();
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        if (poslocfixdel != 0) {
            spinner.setSelection(poslocfixdel);
        }
    }

    private void postUsingVolley() {
            dialog = new ProgressDialog(Closing_stock.this);
            dialog.setTitle("Data Inserting");
            dialog.setMessage("Please wait....");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            if (poslocfixdel == 0) {
                dialog.dismiss();
                button.setEnabled(true);
                MDToast.makeText(Closing_stock.this, "कृपया लोकेशन निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            } else {
                StringBuilder str = new StringBuilder("");
                for (String eachstring : cylinder) {
                    str.append(eachstring).append(",");
                }


                StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.closing_stock_entry,
                        new Response.Listener<String>() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        object = array.getJSONObject(i);
                                        String status = object.getString("status");
                                        String msg = object.getString("msg");
                                        srno = object.getString("srno");
                                        if (status.equals("success")) {
                                            MDToast.makeText(Closing_stock.this, "Closing Stcok Entry Done!"+srno, MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                            dialog.dismiss();
                                            button.setVisibility(View.GONE);
                                            Intent intent = new Intent(Closing_stock.this, closing_stock_print.class);
                                            intent.putExtra("empb",srno);
                                            intent.putExtra("count",count);
                                            intent.putExtra("warehouse",from_warehouse);
                                            button.setEnabled(true);
                                            startActivity(intent);

                                        } else {
                                            button.setEnabled(true);

                                            dialog.dismiss();

                                        }

                                        Log.e("JSON", "> " + status + msg);
                                    }

                                } catch (JSONException e) {
                                    button.setEnabled(true);

                                    dialog.dismiss();
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                dialog.dismiss();
                                button.setEnabled(true);

                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("aicode", String.valueOf(cylinder));
                        params.put("warehouse", from_code);
                        params.put("email", SharedPref.getInstance(Closing_stock.this).getEmail());
                        params.put("db_host",SharedPref.mInstance.getDBHost());
                        params.put("db_username",SharedPref.mInstance.getDBUsername());
                        params.put("db_password",SharedPref.mInstance.getDBPassword());
                        params.put("db_name",SharedPref.mInstance.getDBName());
                        return params;
                    }
                };
                VolleySingleton.getInstance(Closing_stock.this).addToRequestQueue(stringRequest);
            }
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            recreate();
        }
    }

    private void loadata() {
        List<ItemCode> itemCodes = new ArrayList<>();
        SearchAdapter searchAdapter = new SearchAdapter(getApplicationContext(), itemCodes);
        inwardmaincylindernumber.setThreshold(1);
        inwardmaincylindernumber.setAdapter(searchAdapter);
        inwardmaincylindernumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myDB.addBook(inwardmaincylindernumber.getText().toString(),"no");
                finish();
                startActivity(getIntent());
            }
        });
    }

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {

        } else {
            while (cursor.moveToNext()) {
                book_id.add(cursor.getString(0));
                book_title.add(cursor.getString(1));
                cylinder.add(cursor.getString(1));

            }
            int cou = cursor.getCount();
            count = String.valueOf(cou);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            confirmDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All?");
        builder.setMessage("Are you sure you want to delete all Data?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myDB.deleteAllData();
                //Refresh Activity
                finish();
                startActivity(getIntent());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }


    @Override
    protected void onResume() {
        if(status){
            status = false;
            startActivity(getIntent());
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDB != null)
            myDB.close();


    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GOdownMainActivity.class);
        startActivity(intent);
    }
}