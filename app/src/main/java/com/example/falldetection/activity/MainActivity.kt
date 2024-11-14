package com.example.falldetection.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetection.R


class MainActivity : AppCompatActivity() {

    private lateinit var btnMobileNumberSetting: Button
    private lateinit var btnIpSetting: Button
    private lateinit var btnDetection: Button
    private lateinit var btnSocket2Socket: Button
    private lateinit var btnGPS: Button
    private lateinit var switchToggleDetection: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize buttons
        btnMobileNumberSetting = findViewById(R.id.btnMobileNumberSetting)
        btnIpSetting = findViewById(R.id.btnIpSetting)
        btnDetection = findViewById(R.id.btnDetection)
        btnSocket2Socket = findViewById(R.id.btnSocket2Socket)
        btnGPS = findViewById(R.id.btnGPS)
        switchToggleDetection = findViewById(R.id.switchToggleDetection)


        // Set onClickListeners for buttons
        btnMobileNumberSetting.setOnClickListener {
            // Navigate to MobileNumberActivity
            val intent = Intent(this, MobileNumberActivity::class.java)
            startActivity(intent)
        }

        btnIpSetting.setOnClickListener {
            // Navigate to IpSettingActivity
            val intent = Intent(this, IpSettingActivity::class.java)
            startActivity(intent)
        }

        btnDetection.setOnClickListener {
            // Navigate to DetectionActivity
            val intent = Intent(this, DetectionActivity::class.java)
            startActivity(intent)
        }

        btnSocket2Socket.setOnClickListener {
            // Navigate to Socket2Socket
            val intent = Intent(this, Socket2Socket::class.java)
            startActivity(intent)
        }

        btnGPS.setOnClickListener {
            // Navigate to Socket2Socket
            val intent = Intent(this, GPSActivity::class.java)
            startActivity(intent)
        }
    }
}
