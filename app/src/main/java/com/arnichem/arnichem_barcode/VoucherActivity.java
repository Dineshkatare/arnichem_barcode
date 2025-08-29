package com.arnichem.arnichem_barcode;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.Googlepay.GooglepayScreen;
import com.arnichem.arnichem_barcode.PaymentReceipt.CashVoucherActivity;
import com.arnichem.arnichem_barcode.PaymentReceipt.MainPaymentReceipt;
import com.arnichem.arnichem_barcode.TransactionsView.deliverynew.Maindelivery;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class VoucherActivity extends AppCompatActivity {
    CardView googlePay,paymenteceipt,cashReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Payments");

        setContentView(R.layout.activity_voucher);
        googlePay = findViewById(R.id.googlepay);
        paymenteceipt = findViewById(R.id.PrintReceipt);
        cashReceipt = findViewById(R.id.cashVoucher);

        googlePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(VoucherActivity.this, GooglepayScreen.class);
                startActivity(i);
            }
        });
        paymenteceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(VoucherActivity.this, MainPaymentReceipt.class);
                startActivity(i);
            }
        });
        cashReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(VoucherActivity.this, CashVoucherActivity.class);
                startActivity(i);
            }
        });


    }
}