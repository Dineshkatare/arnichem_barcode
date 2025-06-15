package com.arnichem.arnichem_barcode.order;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.view.Dashboard;

public class PickViewActivity extends AppCompatActivity {
    private TextView dateTextView, codeTextView, nameTextView, messageTextView, remarksTextView, itemsTextView, linkTextView;
    private LinearLayout copyIcon, shareIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PICK ENTRY");

        // Retrieve data passed from the previous activity
        int srno = getIntent().getIntExtra("srno", 0); // Default -1 if not found
        String date = getIntent().getStringExtra("date_added");
        String code = getIntent().getStringExtra("code");
        String name = getIntent().getStringExtra("name");
        String message = getIntent().getStringExtra("message");
        String remarks = getIntent().getStringExtra("remarks");
        String items = getIntent().getStringExtra("items");
        String link = getIntent().getStringExtra("link");

        // Find views
        dateTextView = findViewById(R.id.cddateid);
        codeTextView = findViewById(R.id.codeid);
        nameTextView = findViewById(R.id.cdcustnameid);
        messageTextView = findViewById(R.id.message_txt);
        remarksTextView = findViewById(R.id.remarks);
        linkTextView = findViewById(R.id.link_val);

        copyIcon = findViewById(R.id.text_copy);
        shareIcon = findViewById(R.id.text_whatsapp);

        // Set the text in the TextViews
        dateTextView.setText(date);
        codeTextView.setText(code);
        nameTextView.setText(name);
        messageTextView.setText(message);
        remarksTextView.setText(remarks);
        linkTextView.setText(link);  // Set your URL as text
        Linkify.addLinks(linkTextView, Linkify.WEB_URLS);  // Make it clickable
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());  // Allow clicking

        // Set the copy data functionality
        copyIcon.setOnClickListener(v -> copyData(String.valueOf(srno),date, code, name, message, remarks, items, link));

        // Set the share data functionality
        shareIcon.setOnClickListener(v -> shareData(String.valueOf(srno),date, code, name, message, remarks, items, link));

    }
    private void copyData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String dataToCopy = "*New Holding Pick Details*\n" +
                "No: " + srno + "\n" +
                "Pick Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Message: " + message + "\n" +
                "Remarks: " + remarks + "\n" +
                "Link: " + link;

        // Get the ClipboardManager system service
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Pick Data", dataToCopy);
        clipboard.setPrimaryClip(clip);

        // Show a confirmation toast
        Toast.makeText(PickViewActivity.this, "Data copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // Function to share data via WhatsApp
    private void shareData(String srno,String date, String code, String name, String message, String remarks, String items, String link) {
        String shareMessage = "*New Holding Pick Details*\n" +
                "No: " + srno + "\n" +
                "Pick Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Message: " + message + "\n" +
                "Remarks: " + remarks + "\n" +
                "Link: " + link;


        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Check if WhatsApp is installed and share data
        shareIntent.setPackage("com.whatsapp");
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(PickViewActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PickViewActivity.this, Dashboard.class));
    }

}