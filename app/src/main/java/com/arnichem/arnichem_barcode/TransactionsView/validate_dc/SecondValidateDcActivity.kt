package com.arnichem.arnichem_barcode.TransactionsView.validate_dc

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.arnichem.arnichem_barcode.R
import com.arnichem.arnichem_barcode.Reset.APIClient
import com.arnichem.arnichem_barcode.databinding.ActivitySecondValidateDcBinding
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature
import com.arnichem.arnichem_barcode.util.SharedPref
import com.arnichem.arnichem_barcode.util.SharedPref.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import java.io.File


class SecondValidateDcActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondValidateDcBinding
    private var dialog: ProgressDialog? = null
    private var cylinderList: MutableList<CylinderData> = mutableListOf()
    private var digitalSignPath: String = ""
    private var inputHolder: String = ""
    private val TAG = "SecondValidateDcActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        binding = ActivitySecondValidateDcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Validate DC"

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        closeKeypad()

        val dcno = intent.getStringExtra("dcno")
        val invoiceNo = intent.getStringExtra("invoiceNo")
        val customerName = intent.getStringExtra("customerName")
        val cylinderNumbersJson = intent.getStringExtra("cylinderNumbers")

        Log.d(TAG, "Received intent extras - dcno: $dcno, invoiceNo: $invoiceNo, customerName: $customerName")

        try {
            val listType = object : TypeToken<List<CylinderData>>() {}.type
            cylinderList = (Gson().fromJson(cylinderNumbersJson, listType) ?: emptyList<CylinderData>()).toMutableList()
            Log.d(TAG, "Parsed ${cylinderList.size} cylinders from JSON")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cylinder JSON: ${e.message}", e)
            Toast.makeText(this, "Error loading cylinder data", Toast.LENGTH_SHORT).show()
        }

        binding.dcNoTextView.text = "Dc No: $dcno"
        binding.invoiceNoTextView.text = "Invoice No: $invoiceNo"
        binding.customerNameTextView.text = "Customer Name: $customerName"
        binding.edit.requestFocus()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CylinderAdapter(this, cylinderList, ::validateCylinder)
        binding.recyclerView.adapter = adapter

        binding.noData.visibility = if (cylinderList.isEmpty()) View.VISIBLE else View.GONE
        Log.d(TAG, "No data view visibility: ${binding.noData.visibility == View.VISIBLE}")

        binding.submitButton.setOnClickListener {
            Log.d(TAG, "Submit button clicked")
        //    if (cylinderList.all { it.isValidated }) {
                dialog = ProgressDialog(this).apply {
                    setTitle("Submitting")
                    setMessage("Please wait....")
                    setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    setCancelable(false)
                    show()
                }
                Log.d(TAG, "All cylinders validated, showing progress dialog")

                // Prepare data for API
                val validatedQty = cylinderList.size
                val cylNos = cylinderList.joinToString(",") { it.cyl_code }
                val email = SharedPref.mInstance.email
                val transType = "DEL"
                val url = APIClient.delivery_validation // Replace with your API URL

                // Convert digital signature to base64
                val signBase64 = try {
                    val imgFile = File(digitalSignPath)
                    if (imgFile.exists()) {
                        val bytes = imgFile.readBytes()
                        android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    } else {
                        Log.e(TAG, "Digital signature file does not exist: $digitalSignPath")
                        Toast.makeText(this, "Digital signature file not found", Toast.LENGTH_SHORT).show()
                        dialog?.dismiss()
                        return@setOnClickListener
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error encoding digital signature: ${e.message}", e)
                    Toast.makeText(this, "Error encoding signature", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                    return@setOnClickListener
                }

                // Make API call
                val requestQueue = Volley.newRequestQueue(this)
                val stringRequest = object : StringRequest(
                    Method.POST, url,
                    { response ->
                        dialog?.dismiss()
                        try {
                            val jsonResponse = JSONArray(response)
                            val data = jsonResponse.getJSONObject(0)
                            val status = data.getString("status")
                            val message = data.getString("msg")
                            Log.d(TAG, "API response: $status, $message")
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            if (status == "success") {
                                finish() // Close activity on success
                            }
                        } catch (e: JSONException) {
                            Log.e(TAG, "Error parsing API response: ${e.message}", e)
                            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show()
                        }
                    },
                    { error ->
                        dialog?.dismiss()
                        Log.e(TAG, "API error: ${error.message}", error)
                        Toast.makeText(this, "Failed to submit: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "db_host" to SharedPref.mInstance.dbHost, // Replace with actual DB host
                            "db_username" to SharedPref.mInstance.dbUsername, // Replace with actual DB username
                            "db_password" to SharedPref.mInstance.dbPassword, // Replace with actual DB password
                            "db_name" to SharedPref.mInstance.dbName, // Replace with actual DB name
                            "dcno" to dcno.orEmpty(),
                            "validated_qty" to validatedQty.toString(),
                            "cyl_nos" to cylNos,
                            "email" to email,
                            "trans_type" to transType,
                            "sign" to signBase64
                        )
                    }
                }
                requestQueue.add(stringRequest)
//            } else {
//                Log.w(TAG, "Submit attempted with unvalidated cylinders")
//                Toast.makeText(this, "Please validate all cylinders before submitting", Toast.LENGTH_SHORT).show()
//            }
        }

        binding.uploadSign.setOnClickListener {
            Log.d(TAG, "Upload signature button clicked")
            startActivity(
                Intent(this, ActivityDigitalSignature::class.java).putExtra(
                    "type", "delivery"
                )
            )
        }

        binding.closeImg.setOnClickListener {
            Log.d(TAG, "Close signature image clicked")
            SharedPref.getInstance(this).setSign("")
            binding.constraintSigned.visibility = View.GONE
        }

        // Remove the previous setOnKeyListener since dispatchKeyEvent handles input
        binding.edit.setOnKeyListener(null)
    }

    private fun validateCylinder(scannedBarcode: String, fromHandler: Boolean = false) {
        Log.d(TAG, "Validating cylinder with barcode: $scannedBarcode, fromHandler: $fromHandler")

        if (scannedBarcode.isEmpty()) {
            Log.w(TAG, "Empty barcode scanned")
            return
        }

        // Log all cylinder barcodes for comparison
        Log.d(TAG, "Total cylinders in list: ${cylinderList.size}")
        cylinderList.forEachIndexed { index, cyl ->
            Log.d(TAG, "Checking [${index + 1}/${cylinderList.size}] → cyl_code=${cyl.cyl_code}, barcode_no=${cyl.barcode_no}, match=${cyl.barcode_no == scannedBarcode}")
        }

        val cylinder = cylinderList.find { it.barcode_no == scannedBarcode }

        if (cylinder != null) {
            Log.d(TAG, "✅ Cylinder matched → cyl_code=${cylinder.cyl_code}, barcode=${cylinder.barcode_no}")
            cylinder.isValidated = true
            binding.recyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(this, "Cylinder $scannedBarcode validated", Toast.LENGTH_SHORT).show()
        } else {
            Log.w(TAG, "❌ Cylinder not found for barcode: $scannedBarcode")
            showMarathiErrorDialog(scannedBarcode)
        }
    }

    private fun showMarathiErrorDialog(scannedBarcode: String) {
        Log.d(TAG, "Showing error dialog for invalid barcode: $scannedBarcode")
        AlertDialog.Builder(this)
            .setTitle("त्रुटी")
            .setMessage("स्कॅन केलेला बारकोड '$scannedBarcode' यादीत आढळला नाही.")
            .setPositiveButton("ठीक") { dialog, _ ->
                dialog.dismiss()
                binding.edit.setText("")
                binding.edit.requestFocus()
            }
            .setCancelable(false)
            .show()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) {
            Log.w(TAG, "Null KeyEvent received")
            return false
        }

        val action = event.action
        try {
            if (action == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "KEYDOWN: ${event.keyCode}")
            } else if (action == KeyEvent.ACTION_UP) {
                val pressedKey = event.unicodeChar.toChar()
                Log.d(TAG, "pressedKey != 0: $pressedKey")
                if (pressedKey != 0.toChar()) {
                    if (pressedKey == ',' || pressedKey == '\n') {
                        Log.d(TAG, "pressedKey == ',' or '\\n': inputHolder $inputHolder")
                        if (inputHolder.isNotEmpty()) {
                            validateCylinder(inputHolder.trim(), false)
                            inputHolder = ""
                        }
                    } else {
                        inputHolder += pressedKey
                        Log.d(TAG, "pressedKey: $pressedKey, current inputHolder: $inputHolder")
                    }
                }
                Log.d(TAG, "KEYUP: ${event.keyCode}")
            }

            Log.d(TAG, "load: ${binding.edit.text.toString()}")
            val handler = Handler(Looper.getMainLooper())
            val runnable = Runnable {
                val text = binding.edit.text.toString()
                if (text.isNotEmpty()) {
                    Log.d(TAG, "Handler processing text: $text")
                    validateCylinder(text, true)
                    Log.d(TAG, "load1: ${binding.edit.text.toString()} + $text")
                }
                binding.edit.setText("")
                binding.edit.requestFocus()
            }

            handler.postDelayed(runnable, 500)
        } catch (e: Exception) {
            Log.e(TAG, "An exception occurred in dispatchKeyEvent: ${e.message}", e)
            e.printStackTrace()
        }

        Log.d(TAG, "KEY: ${event.keyCode}")
        return false
    }

    private fun closeKeypad() {
        Log.d(TAG, "Closing keypad")
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mServiceReceiver, IntentFilter("digital_sign")
        )
        binding.edit.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver)
    }

    private val mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Broadcast received: ${intent.action}")
            if (intent.action == "digital_sign") {
                val signed = intent.getStringExtra("Signed")
                digitalSignPath = intent.getStringExtra("path") ?: ""
                Log.d(TAG, "Signature status: $signed, path: $digitalSignPath")
                if (signed == "true") {
                    binding.constraintSigned.visibility = View.VISIBLE
                    val imgFile = File(digitalSignPath)
                    if (imgFile.exists()) {
                        Log.d(TAG, "Loading signature image from: $digitalSignPath")
                        val myBitmap =
                            android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                        binding.signedImg.setImageBitmap(myBitmap)
                    } else {
                        Log.e(TAG, "Signature image file does not exist: $digitalSignPath")
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Log.d(TAG, "Back button pressed")
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class CylinderAdapter(
    private val context: Context,
    private val cylinderList: MutableList<CylinderData>,
    private val onValidate: (String) -> Unit
) : RecyclerView.Adapter<CylinderAdapter.ViewHolder>() {

    private val TAG = "CylinderAdapter"

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cylinderText: TextView = itemView.findViewById(R.id.cylinderText)
        val tickImage: ImageView = itemView.findViewById(R.id.tickImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "Creating view holder")
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_validate_cylinder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cylinder = cylinderList[position]
        Log.d(TAG, "Binding cylinder at position $position: ${cylinder.cyl_code}")
        holder.cylinderText.text = cylinder.cyl_code
        holder.tickImage.visibility = if (cylinder.isValidated) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Cylinder clicked: ${cylinder.cyl_code}")
            onValidate(cylinder.cyl_code)
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "Cylinder list size: ${cylinderList.size}")
        return cylinderList.size
    }
}
