package com.arnichem.arnichem_barcode.CRM;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.arnichem.arnichem_barcode.R;

import java.util.Date;
import java.util.Locale;

public class CRMViewActivity extends AppCompatActivity {

    private TextView dateTextView, nameTextView, contact_person_txt, discussionTxt, meeting_type_txt, linkTextView;
    private LinearLayout copyIcon, shareIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crmview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CRM Entry");

        // Retrieve data passed from the previous activity
        String name = getIntent().getStringExtra("name");

        String contact_name = getIntent().getStringExtra("contact_name");
        String meeting_type = getIntent().getStringExtra("meeting_type");
        String discussion = getIntent().getStringExtra("discussion");
        String link = getIntent().getStringExtra("link");
        // Find views
        dateTextView = findViewById(R.id.cddateid);
        nameTextView = findViewById(R.id.name);
        contact_person_txt = findViewById(R.id.contact_person_txt);
        meeting_type_txt = findViewById(R.id.meeting_type_txt);
        discussionTxt = findViewById(R.id.discussionVal);
        linkTextView = findViewById(R.id.link_val);

        nameTextView.setText(name);
        contact_person_txt.setText(contact_name);
        meeting_type_txt.setText(meeting_type);
        discussionTxt.setText(discussion);
        linkTextView.setText(link);

        copyIcon = findViewById(R.id.text_copy);
        shareIcon = findViewById(R.id.text_whatsapp);

        String currentDate = getCurrentDate();
        dateTextView.setText(currentDate);



        linkTextView.setText(link);  // Set your URL as text
        Linkify.addLinks(linkTextView, Linkify.WEB_URLS);  // Make it clickable
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());  // Allow clicking

        // Set the copy data functionality
        copyIcon.setOnClickListener(v -> copyData(currentDate,  name, contact_name, meeting_type, discussion, link));

        // Set the share data functionality
        shareIcon.setOnClickListener(v -> shareData(currentDate,  name, contact_name, meeting_type, discussion, link));
    }

    // Function to copy data to clipboard
    private void copyData(String date, String name,  String contact_name, String meeting_type, String discussion, String link) {
        String dataToCopy = "*" +
                "CRM Entry Details*\n" +
                "Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Contact Person: " + contact_name+ "\n" +
                "Meeting Type: " + meeting_type + "\n" +
                "Discussion: " + discussion + "\n" +
                "Link: " + link;

        // Get the ClipboardManager system service
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Order Data", dataToCopy);
        clipboard.setPrimaryClip(clip);

        // Show a confirmation toast
        Toast.makeText(CRMViewActivity.this, "Data copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // Function to share data via WhatsApp
    private void shareData(String date, String name,String contact_name,String meeting_type, String discussion, String link) {
        String shareMessage = "*CRM Entry Details*\n" +
                "Date: " + date + "\n" +
                "*Name: " + name + "*\n" +
                "Contact Person: " + contact_name+ "\n" +
                "Meeting Type: " + meeting_type + "\n" +
                "*Discussion: " + discussion + "*\n" +
                "Link: " + link;


        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        // Check if WhatsApp is installed and share data
        shareIntent.setPackage("com.whatsapp");
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(CRMViewActivity.this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

}
