package com.arnichem.arnichem_barcode.attendance

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.arnichem.arnichem_barcode.Barcode.NewCamerActivity
import com.arnichem.arnichem_barcode.R
import com.arnichem.arnichem_barcode.Reset.APIClient
import com.arnichem.arnichem_barcode.Reset.APIInterface
import com.arnichem.arnichem_barcode.util.SharedPref
import com.arnichem.arnichem_barcode.view.AlarmReceiver
import com.arnichem.arnichem_barcode.view.Dashboard
import com.arnichem.arnichem_barcode.view.EmployeHandler
import com.arnichem.arnichem_barcode.view.VolleySingleton
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import com.valdesekamdem.library.mdtoast.MDToast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class Attendance_log : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var usernamevalue: TextView
    private lateinit var button: Button
    private lateinit var timeValTxt: TextView
    private lateinit var remarks_edt: EditText
    private lateinit var dialog: ProgressDialog
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var empSpinner: SearchableSpinner
    private lateinit var empAdpater: ArrayAdapter<String>
    private lateinit var employeeHandler:EmployeHandler
    private lateinit var selfieImg :ImageView
    var status = false
    var imageFile:File ?=null
    private  lateinit var empName:String
    private  lateinit var empId:String

    private  lateinit var timeString:String
    var ba1: String? = null

    var imagePath: String? = null

    private var currentLocationMarker: Marker? = null

    private val REQUEST_CODE_LOCATION_PERMISSION = 1001
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var progressDialog: ProgressDialog

    private val UPDATE_INTERVAL: Long = 10 * 1000 /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    var addressText = ""
    var spinnerTxt = ""

    lateinit var spinner: Spinner
    var apiInterface: APIInterface? = null
    lateinit var spinKitView:SpinKitView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_log)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mServiceReceiver,
            IntentFilter("camera_data")
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize UI components
        mapView = findViewById(R.id.mapView)
        empSpinner = findViewById(R.id.emp_spinner)
        selfieImg =findViewById(R.id.selfieImg);

        mapView.onCreate(savedInstanceState)

        apiInterface = APIClient.getClient().create(APIInterface::class.java)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Attendance Entry"
        mapView.getMapAsync(this)
        employeeHandler = EmployeHandler(this)
        empId = SharedPref.getInstance(this).id
        empName = SharedPref.getInstance(this).FirstName()+" "+SharedPref.getInstance(this).LastName()
        // Initialize fused location client
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Fetching Address...")
        progressDialog.setCancelable(false)

        startLocationUpdates()
        // Check and request location permissions

        // Initialize other UI components and set text
        usernamevalue = findViewById(R.id.usernametxtvalue)
        button = findViewById<Button>(R.id.attendanceBtn)
        timeValTxt = findViewById(R.id.timeValTxt)
        remarks_edt = findViewById(R.id.remarks)
        spinKitView = findViewById(R.id.spinnerKit)
        timeValTxt.setOnClickListener(View.OnClickListener {
                showTimePickerDialog();
        })
        usernamevalue.setText(
            SharedPref.getInstance(this).FirstName() + " " + SharedPref.getInstance(this).LastName()
        )
        val date:TextView = findViewById(R.id.date)
        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        date.setText(currentDateTimeString)
        button.setOnClickListener(View.OnClickListener {
            button.isEnabled = false
            postUsingRetrofit()
        })
        loadSpinnerData()

        empSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                empName = empAdpater.getItem(position).toString()

                val cursor: Cursor = employeeHandler.readAllData()
                if (cursor.count == 0) {
                    //      empty_imageview.setVisibility(View.VISIBLE);
                    //      no_data.setVisibility(View.VISIBLE);
                } else {
                    if(empName.equals("Select Employee")){
                         empId = "0"
                    }else{
                        while (cursor.moveToNext()) {
                            val col = cursor.getString(1)
                            val col1 = cursor.getString(0)
                            if (col1.contentEquals(empName)) {
                                empId = col
                            }
                        }
                        checkdual()
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        selfieImg.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@Attendance_log, NewCamerActivity::class.java)
            intent.putExtra("type", "front")
            startActivity(intent)
            // askCameraPermissions();
        })


        spinner = findViewById(R.id.spinner)

        // Create an ArrayAdapter and set it to the spinner (as shown in previous examples)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinner_items,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set up an OnItemSelectedListener for the Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // The code inside this block will be executed when an item is selected in the Spinner
                spinnerTxt = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // This method is called when nothing is selected.
            }
        }



    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                // Handle the selected time
                val selectedTime = simpleFormatTime(hourOfDay, minute)
                timeString = formatTime(hourOfDay,minute)
                timeValTxt!!.text = selectedTime
            },
            hourOfDay,
            minute,
            false // 24-hour format (true for 24-hour, false for AM/PM)
        )

        timePickerDialog.show()
    }



    private fun postUsingRetrofit() {

// Create a MultipartBody.Part from the image file
// Create a MultipartBody.Part from the image file
        val imageBody = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("bitmap", imageFile?.name, imageBody!!)

// Create other RequestBody instances for non-file parameters
        val timeStringRequestBody = timeString.toRequestBody("text/plain".toMediaTypeOrNull())
        val remarksRequestBody = remarks_edt.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val spinnerTxtRequestBody = spinnerTxt.toRequestBody("text/plain".toMediaTypeOrNull())
        val empNameRequestBody = empName.toRequestBody("text/plain".toMediaTypeOrNull())
        val empIdRequestBody = empId.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudeRequestBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeRequestBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val addressTextRequestBody = addressText.toRequestBody("text/plain".toMediaTypeOrNull())

        val emailRequestBody = SharedPref.mInstance.email.toRequestBody("text/plain".toMediaTypeOrNull())
        val dbHostRequestBody = SharedPref.mInstance.dbHost.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dbUsernameRequestBody = SharedPref.mInstance.dbUsername.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dbPasswordTextRequestBody = SharedPref.mInstance.dbPassword.toRequestBody("text/plain".toMediaTypeOrNull())
        val dbNameRequestBody = SharedPref.mInstance.dbName.toRequestBody("text/plain".toMediaTypeOrNull())
        spinKitView.visibility = View.VISIBLE
// Make the API call
        val call = apiInterface?.uploadImageWithTextData(
            timeStringRequestBody,
            remarksRequestBody,
            spinnerTxtRequestBody,
            empNameRequestBody,
            empIdRequestBody,
            latitudeRequestBody,
            longitudeRequestBody,
            addressTextRequestBody,
            emailRequestBody,
            dbHostRequestBody,
            dbUsernameRequestBody,
            dbPasswordTextRequestBody,
            dbNameRequestBody,
            imagePart
        )


// Rest of your code remains unchanged...

// Make the API call

// Make the API call
        // Enqueue the call
        call?.enqueue(object : Callback<MyResponseModel> {
            override fun onResponse(
                call: Call<MyResponseModel>,
                response: retrofit2.Response<MyResponseModel>
            ) {
                if (response.isSuccessful) {
                    val myResponse = response.body()
                    if (myResponse?.status == "success") {

// Set the second alarm at 3:30 PM
                        if(spinnerTxt.equals("IN")){
                            AlarmReceiver.setAlarm(this@Attendance_log, 18, 0)
                        }

                        MDToast.makeText(
                            this@Attendance_log,
                            "Attendance Entry Done!",
                            MDToast.LENGTH_LONG,
                            MDToast.TYPE_SUCCESS
                        ).show()
                        val intent = Intent(this@Attendance_log, Dashboard::class.java)
                        startActivity(intent)
                    } else {
                        // Handle error
                        MDToast.makeText(
                            this@Attendance_log,
                            "Error in API response: ${myResponse?.message}",
                            MDToast.LENGTH_SHORT,
                            MDToast.TYPE_ERROR
                        ).show()
                    }
                } else {
                    // Handle error
                    MDToast.makeText(
                        this@Attendance_log,
                        "Error in API response",
                        MDToast.LENGTH_SHORT,
                        MDToast.TYPE_ERROR
                    ).show()
                }
                dialog.dismiss()
                button.isEnabled = true
                spinKitView.visibility = View.GONE

            }

            override fun onFailure(call: Call<MyResponseModel>, t: Throwable) {

                // Handle failure
                MDToast.makeText(
                    this@Attendance_log,
                    "Error in API call: ${t.message}",
                    MDToast.LENGTH_SHORT,
                    MDToast.TYPE_ERROR
                ).show()
                dialog.dismiss()
                button.isEnabled = true
                spinKitView.visibility = View.GONE

            }
        })
    }


    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun getAndMoveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Request location permissions here if not granted
            // You can call ActivityCompat.requestPermissions here
            return
        }

        // Use the fusedLocationClient to get the last known location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)
                    latitude = location.latitude
                    longitude = location.longitude
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                    )

                    // Create a marker at the current location
                    if (currentLocationMarker != null) {
                        currentLocationMarker!!.remove() // Remove previous marker if exists
                    }
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Current Location")
                    )
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(googleMap)
            }
        }
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onMapReady(map: GoogleMap) {
        map.let {
            googleMap = it
            if (hasLocationPermission()) {
                // Enable current location button and move to user's current location
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
               // getAndMoveToCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun loadSpinnerData() {
        val db = EmployeHandler(applicationContext)
        val labels = db.allLabels

        // Get the value to match
        val fullNameToMatch = SharedPref.mInstance.FirstName() + " " + SharedPref.mInstance.LastName()

        // Find the index of the matching value in the labels list
        val index = labels.indexOfFirst { it == fullNameToMatch }

        // Creating adapter for spinner
        empAdpater = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels)

        // Drop down layout style - list view with radio button
        empAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        empSpinner.setAdapter(empAdpater)

        // Set the selection of the spinner to the found index
        if (index != -1) {
            empSpinner.setSelection(index)
        }
    }


    private fun checkdual() {
            dialog = ProgressDialog(this@Attendance_log)
            dialog.setTitle("Loading")
            dialog.setMessage("Please wait....")
            dialog.setCancelable(false)
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            dialog.show()
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, APIClient.check_attendance_status,
                Response.Listener<String?> { response ->
                    dialog.dismiss()
                    try {
                        val array = JSONArray(response)
                        for (i in 0 until array.length()) {
                            val crmMainObject = array.getJSONObject(i)
                            val status = crmMainObject.getString("status")
                            val msg = crmMainObject.getString("data")
                            if (status.equals("success")) {
                                spinnerTxt = "IN";
                                spinner.setSelection(0) // Note: Indices are 0-based, so 1 corresponds to the second item.
                            } else {
                                spinnerTxt = "OUT";
                                spinner.setSelection(1) // Note: Indices are 0-based, so 1 corresponds to the second item.

                            }
                        }
                    } catch (e: JSONException) {
                        dialog.dismiss()
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    dialog.dismiss()
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["emp_code"] = empId
                    params["db_host"] = SharedPref.mInstance.dbHost
                    params["db_username"] = SharedPref.mInstance.dbUsername
                    params["db_password"] = SharedPref.mInstance.dbPassword
                    params["db_name"] = SharedPref.mInstance.dbName
                    return params
                }
            }
            VolleySingleton.getInstance(this@Attendance_log)
                .addToRequestQueue<String>(stringRequest)

    }

    private fun loadMap(latLng: LatLng) {
        // Load the map with the provided LatLng
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }


    private fun formatTime(hourOfDay: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(calendar.time)
    }

    private fun simpleFormatTime(hourOfDay: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(calendar.time)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button press here, e.g., finish the current activity
                finish()
                return true
            }
            // Handle other menu items if needed
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private val mServiceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals("camera_data", ignoreCase = true)) {
                //Extract your data - better to use constants...
                val url = intent.getStringExtra("url")

                try {

                    var bitmap = MediaStore.Images.Media.getBitmap(
                        this@Attendance_log.getContentResolver(),
                        Uri.parse(url)
                    )
                    bitmap = RotateBitmap(bitmap, 270f)
                    var sdf: android.icu.text.SimpleDateFormat? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sdf = android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm a")
                    }
                    val currentDateandTime = sdf!!.format(Date())
                    val newBitmap: Bitmap =
                        drawTextToBitmap(this@Attendance_log, bitmap, currentDateandTime)!!


                    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                     imageFile = File(storageDir, "my_image.jpg")

                    // Compress the rotated bitmap to JPEG format and save it to the file
                    FileOutputStream(imageFile).use { out ->
                        newBitmap?.compress(Bitmap.CompressFormat.JPEG, 10, out)
                    }

                    // Get the path of the saved image

                    selfieImg.setImageBitmap(Bitmap.createScaledBitmap(newBitmap, 100, 100, false))

                    //   imageView.setImageURI(imageUri);
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun RotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    fun drawTextToBitmap(mContext: Context, bitmap: Bitmap, mText: String?): Bitmap? {
        var bitmap = bitmap
        return try {
            val resources = mContext.resources
            val scale = resources.displayMetrics.density
            var bitmapConfig = bitmap.config
            // set default bitmap config if none
            if (bitmapConfig == null) {
                bitmapConfig = Bitmap.Config.ARGB_8888
            }
            // resource bitmaps are imutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true)
            val canvas = Canvas(bitmap)
            // new antialised Paint
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            // text color - #3D3D3D
            paint.color = Color.rgb(0, 0, 0)
            // text size in pixels
            paint.textSize = (24 * scale).toInt().toFloat()
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)

            // draw text to the Canvas center
            val bounds = Rect()
            paint.getTextBounds(mText, 10, 10, bounds)
            val x = (bitmap.width - bounds.width()) / 6
            val y = (bitmap.height + bounds.height()) / 5
            canvas.drawText(mText!!, x * scale, y * scale, paint)
            bitmap
        } catch (e: Exception) {
            // TODO: handle exception
            null
        }
    }


    private fun startLocationUpdates() {
        progressDialog.show() // Show progress dialog

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = UPDATE_INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        val settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener(this, OnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied
            // You can now request location updates
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@Attendance_log)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@OnSuccessListener
            }
            fusedLocationClient.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    // do work here
                    locationResult?.lastLocation?.let { onLocationChanged(it) }
                }
            }, Looper.myLooper())
        })

        task.addOnFailureListener(this, OnFailureListener { e ->
            progressDialog.dismiss() // Dismiss progress dialog
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                try {
                    // Show the user a dialog to enable location settings
                    e.startResolutionForResult(this@Attendance_log, 0)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        })
    }

    // Handle the result of the resolution
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // User enabled location settings
                // You can now request location updates
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@Attendance_log)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationClient.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        // do work here
                        locationResult?.lastLocation?.let { onLocationChanged(it) }
                    }
                }, Looper.myLooper())
            } else {
                // User canceled the resolution, handle it as needed
            }
        }
    }

    // Request permission for location access if not already granted
    private fun requestLocationPermission() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }

    // Handle location changes
    private fun onLocationChanged(location: Location) {
        progressDialog.dismiss() // Dismiss progress dialog


        val latLng = LatLng(location.latitude, location.longitude)
        addressText = getAddressFromLocation(location.latitude,location.longitude)

        latitude= location.latitude;
        longitude = location.longitude
        loadMap(latLng)


    }



// Inside your Attendance_log class

    // Function to get address from latitude and longitude
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addressText = ""

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()

                    // Get the address lines, typically only one for the provided location
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }

                    // Combine the address lines into a single string
                    addressText = addressParts.joinToString(separator = "\n")
                } else {
                    addressText = "No address found"
                }
            } else {
                addressText = "Addresses is null"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            addressText = "Geocoder error: ${e.message}"
        }

        return addressText
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}