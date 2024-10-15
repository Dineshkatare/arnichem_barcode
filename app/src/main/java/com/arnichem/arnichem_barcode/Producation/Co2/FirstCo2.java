package com.arnichem.arnichem_barcode.Producation.Co2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.arnichem.arnichem_barcode.Producation.Producation_Main;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class FirstCo2 extends AppCompatActivity {
    TextView date,Startvalue,endvalue,Totalscanvalue;
    int smHour;
    int smMinute;
    int emHour;
    int emMinute;
    String smHourstring;
    String smMinutestring;
    String emHourstring;
    String emMinutestring;
    AutoCompleteTextView before_tank_pressure,before_tank_liquid_liter,fillingpessure;
    EditText after_tank_pressure,after_tank_liquid_liter;
    Button nextbutton;
    String starttime,endtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_co2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CO2 Cylinder Fill");
        date=findViewById(R.id.date);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        date.setText(currentDateTimeString);
        Startvalue=findViewById(R.id.Startvalue);
        endvalue=findViewById(R.id.endvaluevalue);
        fillingpessure=findViewById(R.id.fillingval);
        after_tank_pressure =findViewById(R.id.EndTankPressurevalue);
        after_tank_liquid_liter=findViewById(R.id.EndTankVolumevalue);
        before_tank_pressure=findViewById(R.id.StartTankPressurevalue);
        before_tank_liquid_liter=findViewById(R.id.StartTankVolumevalue);
        nextbutton=findViewById(R.id.nextpage);
        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(starttime))
                {
                    MDToast.makeText(FirstCo2.this, "कृपया Start Time टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(TextUtils.isEmpty(endtime))
                {
                    MDToast.makeText(FirstCo2.this, "कृपया End Time टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(before_tank_pressure.getText().toString().isEmpty())
                {
                    MDToast.makeText(FirstCo2.this, "कृपया Start Tank Pressure टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(before_tank_liquid_liter.getText().toString().isEmpty())
                {
                    MDToast.makeText(FirstCo2.this, "कृपया Start Tank Volume टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


                }
                else if(after_tank_pressure.getText().toString().isEmpty())
                {
                    MDToast.makeText(FirstCo2.this, "कृपया End Tank Pressure टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else if(after_tank_liquid_liter.getText().toString().isEmpty())
                {
                    MDToast.makeText(FirstCo2.this, "कृपया End Tank Volume टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();


                }


                else if(fillingpessure.getText().toString().isEmpty())
                {
                    MDToast.makeText(FirstCo2.this, "कृपया Filling Pressure टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();

                }
                else {

                    Intent intent = new Intent(FirstCo2.this, CO2Filling.class);
                    SharedPref.getInstance(getApplicationContext()).setSm(starttime);
                    SharedPref.getInstance(getApplicationContext()).setEm(endtime);
                    SharedPref.getInstance(getApplicationContext()).setAfter_tank_liquid_liter(after_tank_pressure.getText().toString());
                    SharedPref.getInstance(getApplicationContext()).setAfter_tank_pressure(after_tank_liquid_liter.getText().toString());
                    SharedPref.getInstance(getApplicationContext()).setBefore_tank_liquid_liter(before_tank_pressure.getText().toString());
                    SharedPref.getInstance(getApplicationContext()).setBefore_tank_pressure(before_tank_liquid_liter.getText().toString());
                    SharedPref.getInstance(getApplicationContext()).setFillGapPressure(fillingpessure.getText().toString());
                    startActivity(intent);
                }
            }
        });
        Startvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiemPickerstart();
            }
        });
        endvalue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tiemPickerend();
            }
        });
        SharedPref.getInstance(getApplicationContext()).storefrom_loc(String.valueOf(0));
        SharedPref.getInstance(getApplicationContext()).store_dist(String.valueOf(0));
    }

    private void tiemPickerstart(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        smHour = c.get(Calendar.HOUR_OF_DAY);
        smMinute = c.get(Calendar.MINUTE);
        smHourstring= String.valueOf(smHour);
        smMinutestring=String.valueOf(smMinute);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog1 = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        smHour = hourOfDay;
                        smMinute = minute;
                        int length = (int)(Math.log10(minute)+1);
                        if(length==1)
                        {
                            Startvalue.setText(hourOfDay + ":" +"0"+minute);
                            starttime=hourOfDay + ":"+"0"+minute;

                        }
                        else {
                            Startvalue.setText(hourOfDay + ":" + minute);
                            starttime=hourOfDay + ":" + minute;

                        }

                    }
                }, smHour, smMinute, false);
        timePickerDialog1.show();

    }
    private void tiemPickerend(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        emHour = c.get(Calendar.HOUR_OF_DAY);
        emMinute = c.get(Calendar.MINUTE);
        emHourstring= String.valueOf(emHour);
        emMinutestring=String.valueOf(emMinute);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,int minute) {
                        emHour = hourOfDay;
                        emMinute = minute;
                        int length = (int)(Math.log10(minute)+1);
                        if(length==1)
                        {
                            endvalue.setText(hourOfDay + ":" +"0"+minute);
                            endtime=hourOfDay + ":"+"0"+minute;

                        }
                        else {
                            endvalue.setText(hourOfDay + ":" + minute);
                            endtime=hourOfDay + ":" + minute;

                        }
                    }
                }, emHour, emMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Producation_Main.class);
        startActivity(intent);
    }
}