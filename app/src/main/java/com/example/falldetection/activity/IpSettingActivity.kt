package com.example.falldetection.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetection.R
import com.google.zxing.integration.android.IntentIntegrator

class IpSettingActivity : AppCompatActivity() {

    private lateinit var editTextIpAddress: EditText
    private lateinit var btnSaveIp: Button
    private lateinit var btnScanQr: Button
    private val sharedPreferencesName = "FallDetectionPreferences"
    private val ipAddressKey = "IP_ADDRESS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip_setting)

        editTextIpAddress = findViewById(R.id.editTextIpAddress)
        btnSaveIp = findViewById(R.id.btnSaveIp)
        btnScanQr = findViewById(R.id.btnScanQr) // New button for QR scanning

        // Load saved IP address
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedIp = sharedPreferences.getString(ipAddressKey, "")
        editTextIpAddress.setText(savedIp)

        // Save the updated IP address
        btnSaveIp.setOnClickListener {
            val newIpAddress = editTextIpAddress.text.toString()
            if (newIpAddress.isNotEmpty()) {
                saveIpAddress(newIpAddress)
            } else {
                Toast.makeText(this, "Please enter a valid IP address", Toast.LENGTH_SHORT).show()
            }
        }

        // Scan QR Code to get the IP address
        btnScanQr.setOnClickListener {
//            val integrator = IntentIntegrator(this)
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
//            integrator.setPrompt("Scan a QR code containing the IP address")
//            integrator.setCameraId(0)
//            integrator.setOrientationLocked(true)
//            integrator.initiateScan()
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan a QR code containing the IP address")
            integrator.setOrientationLocked(true)
            integrator.setCaptureActivity(CustomScannerActivity::class.java) // Set custom scanner
            integrator.initiateScan()
        }
    }

    // Save IP Address Method
    private fun saveIpAddress(ipAddress: String) {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(ipAddressKey, ipAddress)
            apply()
        }
        Toast.makeText(this, "IP Address saved!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // Handle QR scan result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val scannedIpAddress = result.contents
                editTextIpAddress.setText(scannedIpAddress) // Set scanned IP address
                saveIpAddress(scannedIpAddress)
            } else {
                Toast.makeText(this, "No IP address found in the QR code", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
