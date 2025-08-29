package com.arnichem.arnichem_barcode.PaymentReceipt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arnichem.arnichem_barcode.Barcode.NewCamerActivity;
import com.arnichem.arnichem_barcode.R;
import com.arnichem.arnichem_barcode.Reset.APIClient;
import com.arnichem.arnichem_barcode.Reset.APIInterface;
import com.arnichem.arnichem_barcode.VoucherActivity;
import com.arnichem.arnichem_barcode.constant.constant;
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature;
import com.arnichem.arnichem_barcode.util.SharedPref;
import com.arnichem.arnichem_barcode.util.Util;
import com.arnichem.arnichem_barcode.view.Dashboard;
import com.arnichem.arnichem_barcode.view.VolleySingleton;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashVoucherActivity extends AppCompatActivity {
    private TextView usernamevalue, date, tvSelectedItem,transactionsTv;
    private EditText amountValue, descriptionEt, transactionsEt, remarksEt;
    private Spinner amountSpinner;
    private Button submit;
    private ImageView uploadOption,signedImg;
    private ConstraintLayout constraintSigned;
    Button uploadSign;

    String digital_sign = "", digitalSignPath = "";

    private CardView uploadedImageCard;
    private ImageView uploadedImageView;
    private ArrayAdapter<CharSequence> adapter;
    private ArrayList<String> itemList;
    private ArrayAdapter<String> nameAdapter;
    private SharedPreferences pref;
    private ProgressDialog dialog;
    private String amountType, srno, selectedName, imagePath = "";
    private int amountTypePos;
    private File imageFile;
    APIInterface apiInterface;

    private boolean isCapture = false;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 101;
    private static final int MEDIA_IMAGES_PERMISSION_REQUEST_CODE = 102;

    private final BroadcastReceiver mServiceReceiverSign = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("digital_sign")) {
                //Extract your data - better to use constants...
                String Signed = intent.getStringExtra("Signed");
                digitalSignPath = intent.getStringExtra("path");
                if (Signed.equalsIgnoreCase("true")) {
                    constraintSigned.setVisibility(View.VISIBLE);
                    File imgFile = new File(digitalSignPath);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        digital_sign = Util.getImage(myBitmap);
                        signedImg.setImageBitmap(myBitmap);
                    }
                }
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_voucher);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.cash_voucher);
        uploadSign = findViewById(R.id.uploadSign);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom back press logic here
                finish(); // Close the Activity
            }
        });
        pref = getSharedPreferences(constant.TAG, MODE_PRIVATE);
        itemList = new ArrayList<>();
        loadCustomerCodes();
        apiInterface = APIClient.getClient().create(APIInterface.class);

        // Initialize views
        usernamevalue = findViewById(R.id.usernametxtvalue);
        transactionsTv = findViewById(R.id.transactionsTv);
        date = findViewById(R.id.date);
        amountValue = findViewById(R.id.amountval);
        amountSpinner = findViewById(R.id.amountspinner);
        submit = findViewById(R.id.PaymentSubmitBtn);
        tvSelectedItem = findViewById(R.id.tvSelectedItem);
        descriptionEt = findViewById(R.id.descriptionEt);
        transactionsEt = findViewById(R.id.transactionsEt);
        constraintSigned = findViewById(R.id.constraintSigned);
        signedImg = findViewById(R.id.signedImg);
        remarksEt = findViewById(R.id.remarksEt);
        uploadOption = findViewById(R.id.uploadOption);
        uploadedImageCard = findViewById(R.id.uploaded_image);
        uploadedImageView = findViewById(R.id.uploaded_image_view);

        // Register BroadcastReceiver for camera data
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver,
                new IntentFilter("camera_data"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiverSign,
                new IntentFilter("digital_sign"));


        // Set username and date
        usernamevalue.setText(SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        date.setText(sdf.format(new Date()));
        date.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CashVoucherActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        date.setText(dateStr);
                    },
                    year, month, day
            );
            // Disable future dates
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        // Setup amount spinner
        adapter = ArrayAdapter.createFromResource(this, R.array.ammounttype, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amountSpinner.setAdapter(adapter);
        amountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                amountType = (String) parent.getItemAtPosition(position);
                if(amountType.equals("UPI")){
                    transactionsEt.setVisibility(View.VISIBLE);
                    transactionsTv.setVisibility(View.VISIBLE);
                }else {
                    transactionsTv.setVisibility(View.GONE);
                    transactionsEt.setVisibility(View.GONE);
                }
                amountTypePos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup searchable spinner
        tvSelectedItem.setOnClickListener(v -> showSearchableSpinner());

        // Setup image upload option
        uploadOption.setOnClickListener(v -> showImageSourceDialog());

        // Setup submit button
        submit.setOnClickListener(v -> {
            submit.setEnabled(false);
            postUsingRetrofit();
        });
        uploadSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CashVoucherActivity.this, ActivityDigitalSignature.class);
                intent.putExtra("type", "voucher");
                startActivity(intent);
            }
        });
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        String[] options = {"Camera", "Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkCameraPermission()) {
                    startCamera();
                } else {
                    requestCameraPermission();
                }
            } else {
                if (checkGalleryPermission()) {
                    pickImageFromGallery();
                } else {
                    requestGalleryPermission();
                }
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MEDIA_IMAGES_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("camera_data") && !isCapture) {
                isCapture = true;
                imagePath = intent.getStringExtra("url");
                imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");
                UCrop.of(Uri.parse(imagePath), Uri.fromFile(imageFile))
                        .start(CashVoucherActivity.this);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = getPath(uri);
            imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");
            UCrop.of(uri, Uri.fromFile(imageFile))
                    .start(CashVoucherActivity.this);
        }
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri croppedUri = UCrop.getOutput(data);
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedUri);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
                byte[] byteArray = outputStream.toByteArray();

                // Save the compressed bitmap to a file
                imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_image.jpg");
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                fileOutputStream.write(byteArray);
                fileOutputStream.flush();
                fileOutputStream.close();

                // Update UI
                uploadOption.setVisibility(View.GONE);
                uploadedImageCard.setVisibility(View.VISIBLE);
                uploadedImageView.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                MDToast.makeText(this, "Error processing image", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                e.printStackTrace();
            }
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    MDToast.makeText(this, "Camera permission denied", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
                break;
            case GALLERY_PERMISSION_REQUEST_CODE:
            case MEDIA_IMAGES_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    MDToast.makeText(this, "Gallery permission denied", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
                break;
        }
    }

    private void loadCustomerCodes() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading Customers");
        dialog.setMessage("Please wait...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIClient.voucher_payment_names,
                response -> {
                    try {
                        dialog.dismiss();
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String status = obj.getString("status");
                            if ("success".equals(status)) {
                                JSONArray custCodes = obj.getJSONArray("cust_codes");
                                for (int j = 0; j < custCodes.length(); j++) {
                                    itemList.add(custCodes.getString(j));
                                }
                                nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
                                nameAdapter.notifyDataSetChanged();
                            } else {
                                MDToast.makeText(this, obj.getString("msg"), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                            }
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        MDToast.makeText(this, "Error parsing response", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    dialog.dismiss();
                    MDToast.makeText(this, "Network error: " + error.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("db_host", SharedPref.mInstance.getDBHost());
                params.put("db_username", SharedPref.mInstance.getDBUsername());
                params.put("db_password", SharedPref.mInstance.getDBPassword());
                params.put("db_name", SharedPref.mInstance.getDBName());
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void showSearchableSpinner() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_searchable_spinner, null);
        dialogBuilder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.etSearch);
        ListView listView = dialogView.findViewById(R.id.listView);
        ImageView ivAdd = dialogView.findViewById(R.id.ivAdd);
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText(R.string.empty_list_message);
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(16, 16, 16, 16);

        nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(nameAdapter);
        listView.setEmptyView(tvEmpty);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!itemList.isEmpty()) {
                    nameAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedName = nameAdapter.getItem(position);
            tvSelectedItem.setText(selectedName);
            alertDialog.dismiss();
        });

        ivAdd.setOnClickListener(v -> {
            String newText = etSearch.getText().toString().trim();
            if (!newText.isEmpty()) {
                tvSelectedItem.setText(newText);
                selectedName = newText;
                itemList.add(newText);
                nameAdapter.notifyDataSetChanged();
                etSearch.setText("");
                alertDialog.dismiss();
            } else {
                MDToast.makeText(this, getString(R.string.enter_text_to_add), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            }
        });
    }

    private void postUsingRetrofit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Data Inserting");
        builder.setMessage("Please wait...");
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Input validation
      
        if (selectedName == null || selectedName.isEmpty()) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया नाव निवडा किंवा टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }
        if (amountTypePos == 0) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया amountType निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }
        String amountStr = amountValue.getText().toString();
        if (amountStr.isEmpty() || !isValidAmount(amountStr)) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया वैध Amount टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }
        if (descriptionEt.getText().toString().isEmpty()) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया Description टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }
        if (amountType.equals("UPI") && transactionsEt.getText().toString().isEmpty()) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया Transactions ID टाका !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }

        // Validate amountType against allowed ENUM values
        String[] validModes = {"CASH", "CHEQUE", "DD", "UPI", "Online"};
        if (!Arrays.asList(validModes).contains(amountType)) {
            dialog.dismiss();
            submit.setEnabled(true);
            MDToast.makeText(this, "कृपया वैध Payment Type निवडा !", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
            return;
        }

        if(!amountType.equals("UPI")){
            transactionsEt.setText("");
        }
        // Prepare form data
        RequestBody custCode = RequestBody.create(MediaType.parse("text/plain"), selectedName);
        RequestBody mode = RequestBody.create(MediaType.parse("text/plain"), amountType);
        RequestBody amount = RequestBody.create(MediaType.parse("text/plain"), amountStr);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), descriptionEt.getText().toString());
        RequestBody transactionId = RequestBody.create(MediaType.parse("text/plain"), transactionsEt.getText().toString());
        RequestBody remarks = RequestBody.create(MediaType.parse("text/plain"), remarksEt.getText().toString());
        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getEmail());
        RequestBody dbHost = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getDBHost());
        RequestBody dbUsername = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getDBUsername());
        RequestBody dbPassword = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getDBPassword());
        RequestBody dbName = RequestBody.create(MediaType.parse("text/plain"), SharedPref.getInstance(this).getDBName());
        RequestBody dateBody = RequestBody.create(MediaType.parse("text/plain"), date.getText().toString());

        // Prepare file part
        MultipartBody.Part filePart = null;
        if (imageFile != null && imageFile.exists()) {
            if (imageFile.length() > 5 * 1024 * 1024) { // 5MB limit
                dialog.dismiss();
                submit.setEnabled(true);
                MDToast.makeText(this, "Image size exceeds 5MB limit!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                return;
            }
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            filePart = MultipartBody.Part.createFormData("file_path", imageFile.getName(), fileBody);
        }

        // Make API call
        Call<ResponseBody> call = apiInterface.uploadVoucherPayment(
                custCode, mode, amount, description, transactionId, remarks,
                email, dbHost, dbUsername, dbPassword, dbName, dateBody, filePart
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                submit.setEnabled(true);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        Log.d("Retrofit", "Response: " + responseString);
                        JSONObject obj = new JSONObject(responseString);
                        String status = obj.optString("status");
                        String msg = obj.optString("msg");
                        String filePath = obj.optString("file_path");
                        String series = obj.optString("series");
                        String vch_no = obj.optString("vch_no");

                        if ("success".equalsIgnoreCase(status)) {
                            srno = obj.optString("srno");
                            MDToast.makeText(CashVoucherActivity.this, "Payment Entry Done!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                            Intent intent = new Intent(CashVoucherActivity.this, CashVoucherPrintActivity.class);
                            intent.putExtra("custname", selectedName);
                            intent.putExtra("amountstr", amountStr);
                            intent.putExtra("paymentstr", amountType);
                            intent.putExtra("description", descriptionEt.getText().toString());
                            intent.putExtra("transaction_id", transactionsEt.getText().toString());
                            intent.putExtra("remarks", remarksEt.getText().toString());
                            intent.putExtra("srno", srno);
                            intent.putExtra("series", series);
                            intent.putExtra("vch_no", vch_no);
                            intent.putExtra("image_path", filePath);
                            intent.putExtra("date", date.getText().toString());
                            startActivity(intent);
                        } else {
                            MDToast.makeText(CashVoucherActivity.this, msg.isEmpty() ? "Unknown error" : msg, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        }
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        MDToast.makeText(CashVoucherActivity.this, "Server error: " + response.code() + " - " + errorBody, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                    }
                } catch (Exception e) {
                    MDToast.makeText(CashVoucherActivity.this, "Error: " + e.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                submit.setEnabled(true);
                String message = "Network Error: Please check your internet connection";
                if (t instanceof java.net.SocketTimeoutException) {
                    message = "Request timed out";
                } else if (t instanceof java.io.IOException) {
                    message = "Unable to connect to the server";
                }
                MDToast.makeText(CashVoucherActivity.this, message, MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                t.printStackTrace();
            }
        });
    }

    private boolean isValidAmount(String amount) {
        try {
            float value = Float.parseFloat(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void startCamera() {
        Intent intent = new Intent(CashVoucherActivity.this, NewCamerActivity.class);
        intent.putExtra("type", "check");
        startActivity(intent);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, Dashboard.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiverSign);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver);
        Log.d("Destroy", "onDestroy called");
    }
}