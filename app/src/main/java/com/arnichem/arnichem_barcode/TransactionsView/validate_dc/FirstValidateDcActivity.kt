package com.arnichem.arnichem_barcode.TransactionsView.validate_dc

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.arnichem.arnichem_barcode.R
import com.arnichem.arnichem_barcode.Reset.APIClient
import com.arnichem.arnichem_barcode.util.SharedPref
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date

class FirstValidateDcActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private var dialog: android.app.ProgressDialog? = null
    private val TAG = "FirstValidateDcActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first_validate_dc)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Validate Dc"

        requestQueue = Volley.newRequestQueue(this)

        val etvDcEmpNo = findViewById<EditText>(R.id.etvDcEmpNo)
        val getInfoButton = findViewById<Button>(R.id.getInfo)

        // Set current date and time

        getInfoButton.setOnClickListener {
            val dcno = etvDcEmpNo.text.toString().trim()
            if (dcno.isNotEmpty()) {
                fetchInvoiceData(dcno)
            } else {
                Toast.makeText(this, "Please enter a DC number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchInvoiceData(dcno: String) {
        dialog = android.app.ProgressDialog(this).apply {
            setTitle("Data Fetching")
            setMessage("Please wait....")
            setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER)
            show()
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, APIClient.get_invoice_php,
            Response.Listener { response ->
                try {
                    if (response.trim().isNotEmpty() && response.trim() != "Could") {
                        val jsonObject = JSONObject(response)
                        val data = jsonObject.getJSONObject("data")
                        val status = jsonObject.getString("status")
                        val msg = jsonObject.getString("msg")

                        if (status == "Success") {
                            val invoiceNo = data.getString("invoiceNo")
                            val customerName = data.getString("customerName")
                            val cylinderNumbersJson = data.getJSONArray("cylinderNumbers").toString()

                            val intent = Intent(this@FirstValidateDcActivity, SecondValidateDcActivity::class.java).apply {
                                putExtra("invoiceNo", invoiceNo)
                                putExtra("customerName", customerName)
                                putExtra("cylinderNumbers", cylinderNumbersJson)
                                putExtra("dcno", dcno)
                            }
                            startActivity(intent)
                            Toast.makeText(this@FirstValidateDcActivity, msg, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@FirstValidateDcActivity, msg, Toast.LENGTH_LONG).show()
                        }

                        Log.e("JSON", "> $status $msg")
                    } else {
                        Log.e(TAG, "Invalid response: $response")
                        Toast.makeText(this@FirstValidateDcActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                    }
                    dialog?.dismiss()
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Error: ${e.message}, Response: $response")
                    dialog?.dismiss()
                    Toast.makeText(this@FirstValidateDcActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Volley Error: ${error.message}")
                dialog?.dismiss()
                Toast.makeText(this@FirstValidateDcActivity, "Server error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["db_host"] = SharedPref.mInstance.dbHost
                params["db_username"] = SharedPref.mInstance.dbUsername
                params["db_password"] = SharedPref.mInstance.dbPassword
                params["db_name"] = SharedPref.mInstance.dbName
                params["dcno"] = dcno
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}