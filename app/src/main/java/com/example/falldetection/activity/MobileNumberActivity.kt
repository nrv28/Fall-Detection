package com.example.falldetection.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetection.R

class MobileNumberActivity : AppCompatActivity() {

    private lateinit var editTextPhoneNumber: EditText
    private lateinit var btnSave: Button
    private val sharedPreferencesName = "FallDetectionPreferences"
    private val phoneNumberKey = "PHONE_NUMBER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_number)

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber)
        btnSave = findViewById(R.id.btnSave)

        // Load saved phone number
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getString(phoneNumberKey, "")
        editTextPhoneNumber.setText(savedNumber)

        // Save the updated number
        btnSave.setOnClickListener {
            val newPhoneNumber = editTextPhoneNumber.text.toString()
            if (newPhoneNumber.isNotEmpty()) {
                with(sharedPreferences.edit()) {
                    putString(phoneNumberKey, newPhoneNumber)
                    apply()
                }
                Toast.makeText(this, "Phone number saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
