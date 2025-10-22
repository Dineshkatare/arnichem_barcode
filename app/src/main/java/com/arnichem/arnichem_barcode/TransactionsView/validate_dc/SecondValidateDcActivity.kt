package com.arnichem.arnichem_barcode.TransactionsView.validate_dc

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.arnichem.arnichem_barcode.Barcode.LaserScannerActivity
import com.arnichem.arnichem_barcode.Barcode.NewScanner
import com.arnichem.arnichem_barcode.R
import com.arnichem.arnichem_barcode.digital_signature.ActivityDigitalSignature
import com.arnichem.arnichem_barcode.util.SharedPref
import com.arnichem.arnichem_barcode.util.Util
import java.io.File
import java.lang.reflect.Type

class CylinderAdapter(
    private val context: Context,
    private val cylinderList: List<CylinderData>,
    private val onValidate: (String) -> Unit
) : RecyclerView.Adapter<CylinderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cylinderText: TextView = itemView.findViewById(android.R.id.text1)
        val tickImage: ImageView = itemView.findViewById(android.R.id.icon)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cylinder = cylinderList[position]
        holder.cylinderText.text = cylinder.cyl_code
        holder.tickImage.visibility = if (cylinder.isValidated) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener { onValidate(cylinder.cyl_code) }
    }

    override fun getItemCount() = cylinderList.size
}

class SecondValidateDcActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dcNoTextView: TextView
    private lateinit var invoiceNoTextView: TextView
    private lateinit var customerNameTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var uploadSign: Button
    private lateinit var constraintSigned: ConstraintLayout
    private lateinit var signedImg: ImageView
    private lateinit var closeImg: ImageView
    private var dialog: ProgressDialog? = null
    private var cylinderList: List<CylinderData> = emptyList()
    private var digitalSignPath = ""
    private val TAG = "SecondValidateDcActivity"
    private var isAllFabsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second_validate_dc)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Validate DC"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        dcNoTextView = findViewById(R.id.dcNoTextView)
        invoiceNoTextView = findViewById(R.id.invoiceNoTextView)
        customerNameTextView = findViewById(R.id.customerNameTextView)
        recyclerView = findViewById(R.id.recyclerView)
        submitButton = findViewById(R.id.submitButton)
        uploadSign = findViewById(R.id.uploadSign)
        constraintSigned = findViewById(R.id.constraintSigned)
        signedImg = findViewById(R.id.signedImg)
        closeImg = findViewById(R.id.closeImg)

        val addFab = findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.add_fab)
        val cameraScanFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.camera_scan)
        val barcodeScanFab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.barcode_scan)

        cameraScanFab.visibility = View.GONE
        barcodeScanFab.visibility = View.GONE

        addFab.shrink()

        addFab.setOnClickListener {
            if (!isAllFabsVisible) {
                cameraScanFab.show()
                barcodeScanFab.show()
                addFab.extend()
                isAllFabsVisible = true
            } else {
                cameraScanFab.hide()
                barcodeScanFab.hide()
                addFab.shrink()
                isAllFabsVisible = false
            }
        }

        cameraScanFab.setOnClickListener {
            startActivity(Intent(this, NewScanner::class.java).apply { putExtra("type", "validate") })
        }

        barcodeScanFab.setOnClickListener {
            startActivity(Intent(this, LaserScannerActivity::class.java).apply { putExtra("type", "validate") })
        }

        val intent = intent
        val dcno = intent.getStringExtra("dcno")
        val invoiceNo = intent.getStringExtra("invoiceNo")
        val customerName = intent.getStringExtra("customerName")
        val cylinderNumbersJson = intent.getStringExtra("cylinderNumbers")

        val listType: Type = object : TypeToken<List<CylinderData>>() {}.type
        cylinderList = Gson().fromJson(cylinderNumbersJson, listType) ?: emptyList()

        dcNoTextView.text = "Dc No: $dcno"
        invoiceNoTextView.text = "Invoice No: $invoiceNo"
        customerNameTextView.text = "Customer Name: $customerName"

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CylinderAdapter(this, cylinderList) { scannedBarcode ->
            validateCylinder(scannedBarcode)
        }

        findViewById<TextView>(R.id.no_data).visibility = if (cylinderList.isEmpty()) View.VISIBLE else View.GONE

        submitButton.setOnClickListener {
            dialog = ProgressDialog(this).apply {
                setTitle("Submitting")
                setMessage("Please wait....")
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                show()
            }
            // Simulate submission (replace with actual API call)
            dialog?.dismiss()
            Toast.makeText(this, "Validation submitted", Toast.LENGTH_SHORT).show()
        }

        uploadSign.setOnClickListener {
            startActivity(Intent(this, ActivityDigitalSignature::class.java).apply { putExtra("type", "validate") })
        }

        closeImg.setOnClickListener {
            SharedPref.getInstance(this).setSign("")
            constraintSigned.visibility = View.GONE
        }
    }

    private fun validateCylinder(scannedBarcode: String) {
        cylinderList.forEach { cylinder ->
            if (cylinder.cyl_code == scannedBarcode || cylinder.barcode_no == scannedBarcode) {
                cylinder.isValidated = true
                recyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(this, "Cylinder $scannedBarcode validated", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this, "No matching cylinder found for $scannedBarcode", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceReceiver, IntentFilter("digital_sign"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceReceiver)
    }

    private val mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "digital_sign") {
                val signed = intent.getStringExtra("Signed")
                digitalSignPath = intent.getStringExtra("path") ?: ""
                if (signed == "true") {
                    constraintSigned.visibility = View.VISIBLE
                    val imgFile = File(digitalSignPath)
                    if (imgFile.exists()) {
                        val myBitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                        signedImg.setImageBitmap(myBitmap)
                    }
                }
            }
        }
    }
}