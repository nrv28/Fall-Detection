package com.example.falldetection.activity

import android.Manifest
import android.content.Context
import com.example.falldetection.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.CountDownTimer
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.widget.Button
import com.example.falldetection.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.Socket


class DetectionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var switchToggleDetection: Switch
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewIP: TextView
    private lateinit var lineChart: LineChart

    private var lastZ: Double = 0.0
    private var isFalling: Boolean = false
    private var stableCount: Int = 0
    private val stabilityThreshold = 10
    private val threshold = 15.0
    private val beepDuration = 500 // Duration of each beep sound
    private val beepInterval = 1000 // Interval between beeps (1 second)
    private val totalBeepDuration = 10000 // Total duration to beep (10 seconds)
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val entriesX = mutableListOf<Entry>()
    private val entriesY = mutableListOf<Entry>()
    private val entriesZ = mutableListOf<Entry>()
    private var index = 0f

    private var isDetectionOn = false
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var clientSocket: Socket? = null
    private var out: PrintWriter? = null

    companion object {
        private const val CALL_REQUEST_CODE = 101
        const val sharedPreferencesName = "FallDetectionPreferences"
        private const val phoneNumberKey = "PHONE_NUMBER"
        const val ipAddressKey = "IP_ADDRESS"
        private lateinit var latitudeTextView: TextView
        private lateinit var longitudeTextView: TextView
        private const val LATITUDE_KEY = "LATITUDE"
        private const val LONGITUDE_KEY = "LONGITUDE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)

        // Initialize UI elements
        switchToggleDetection = findViewById(R.id.switchToggleDetection)
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber)
        textViewIP = findViewById(R.id.textViewIP)
        lineChart = findViewById(R.id.lineChart)
        latitudeTextView = findViewById(R.id.latitude)
        longitudeTextView = findViewById(R.id.longitude)

        handler = Handler()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Toggle detection switch listener
        switchToggleDetection.setOnCheckedChangeListener { _, isChecked ->
            isDetectionOn = isChecked
            if (!isChecked) {
                stopDetection() // Stop detection when toggled off
            } else {
                startDetection()
                connectToServer()
                displayCurrentLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        displaySavedPhoneNumber()
        displaySavedIPAddress()
        displayCurrentLocation()
    }

// ------------------------------------------------------------------------------------------------------------------------------

    private fun displaySavedIPAddress() {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedIP = sharedPreferences.getString(ipAddressKey, null)

        if (!savedIP.isNullOrEmpty()) {
            textViewIP.text = "Saved IP: $savedIP"
        } else {
            textViewIP.text = "No IP address saved"
        }
    }

    private fun getSavedPhoneNumber(): String? {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(phoneNumberKey, null)
    }

    private fun displaySavedPhoneNumber() {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getString(phoneNumberKey, null)

        if (!savedNumber.isNullOrEmpty()) {
            textViewPhoneNumber.text = "Emergency Contact: $savedNumber"
        } else {
            textViewPhoneNumber.text = "No emergency contact saved"
        }
    }

    private fun connectToServer() {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val ipAddress = sharedPreferences.getString(ipAddressKey, null)
        if (ipAddress != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    clientSocket = Socket(ipAddress, 12345) // Replace with your server's port
                    out = PrintWriter(clientSocket!!.getOutputStream(), true)
                    Log.d("Socket", "Connected to server: $ipAddress")
                } catch (e: Exception) {
                    Log.e("Socket", "Error connecting to server: ${e.message}")
                }
            }
        } else {
            Log.e("Socket", "IP address is not saved.")
        }
    }


//-----------------------------------------------------------------------------------------------------------------------------



    private fun startDetection() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        Toast.makeText(this, "Detection started", Toast.LENGTH_SHORT).show()
    }

    private fun stopDetection() {
        sensorManager.unregisterListener(this)
        Toast.makeText(this, "Detection stopped", Toast.LENGTH_SHORT).show()
        entriesX.clear()
        entriesY.clear()
        entriesZ.clear()
        index = 0f
        lineChart.clear()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && isDetectionOn) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()
            detectFall(z)
//            logSensorData(x, y, z) // Log accelerometer data
            updateChart(x, y, z) // Update the graph with the new data
        }
    }

//    private fun logSensorData(x: Double, y: Double, z: Double) {
//        Log.d("FallDetection", "X: $x, Y: $y, Z: $z")
//    }

    private fun updateChart(x: Double, y: Double, z: Double) {
        entriesX.add(Entry(index, x.toFloat()))
        entriesY.add(Entry(index, y.toFloat()))
        entriesZ.add(Entry(index, z.toFloat()))
        index++
        val dataSetX = LineDataSet(entriesX, "X Axis")
        val dataSetY = LineDataSet(entriesY, "Y Axis")
        val dataSetZ = LineDataSet(entriesZ, "Z Axis")
        dataSetX.color = android.graphics.Color.RED
        dataSetY.color = android.graphics.Color.GREEN
        dataSetZ.color = android.graphics.Color.BLUE
        val lineData = LineData(dataSetX, dataSetY, dataSetZ)
        lineChart.data = lineData
        lineChart.invalidate()
    }

    private fun detectFall(z: Double) {
        if (z - lastZ > threshold) {
            isFalling = true
            stableCount = 0
            handleFallDetected()
        } else if (isFalling) {
            stableCount++
            if (stableCount > stabilityThreshold) {
                isFalling = false
            }
        }
        lastZ = z
    }

    private fun handleFallDetected() {
        runOnUiThread {
            Toast.makeText(this, "Fall Detected!", Toast.LENGTH_SHORT).show()
            startBeeping() // Start beeping for 10 seconds

            // Get the phone number
            val phoneNumber = getSavedPhoneNumber()
            if (!phoneNumber.isNullOrEmpty() && PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
                Toast.makeText(this, "Fetched Phone Number: $phoneNumber", Toast.LENGTH_SHORT).show()
                // Start a countdown timer to delay the phone call until beeping is finished
                object : CountDownTimer(totalBeepDuration.toLong(), totalBeepDuration.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        // No action needed here
                    }

                    override fun onFinish() {
                        makePhoneCall(phoneNumber) // Make the call after beeping finishes
                    }
                }.start()
            } else {
                Toast.makeText(this, "No valid phone number found!", Toast.LENGTH_SHORT).show()
            }

            // Fetch server IP from SharedPreferences
            val serverIp = getSavedServerIP()
            if (!serverIp.isNullOrEmpty()) {
                sendBeepMessage(serverIp) // Send beep message to the server
            } else {
                Toast.makeText(this, "No valid server IP found!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playBeepSound() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 30)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_LOW_L, beepDuration)
    }

    private fun startBeeping() {
        runnable = object : Runnable {
            var elapsedTime = 0 // Track elapsed time

            override fun run() {
                if (elapsedTime < totalBeepDuration) {
                    playBeepSound() // Play beep sound
                    elapsedTime += beepInterval // Increment elapsed time
                    handler.postDelayed(this, beepInterval.toLong()) // Repeat every second
                }
            }
        }
        handler.post(runnable) // Start the beeping
    }

    private fun sendBeepMessage(ipAddress: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (clientSocket != null && out != null) {
                    out!!.println("beep") // Send beep message
                    Log.d("Socket", "Beep message sent to server: $ipAddress")

                    // Fetch location from SharedPreferences
                    val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
                    val savedLatitude = sharedPreferences.getString(LATITUDE_KEY, null)
                    val savedLongitude = sharedPreferences.getString(LONGITUDE_KEY, null)

                    // If location is available, use it. Otherwise, send a default location.
                    val locationMessage = if (savedLatitude != null && savedLongitude != null) {
                        "latitude-$savedLatitude,longitude-$savedLongitude"
                    } else {
                        "latitude-21.032925,longitude-76.678810" // Default location
                    }

                    out!!.println(locationMessage) // Send location message
                    Log.d("location", "Location sent to server: $ipAddress")

                } else {
                    Log.e("Socket", "Socket is not connected.")
                }
            } catch (e: Exception) {
                Log.e("Socket", "Error sending message: ${e.message}")
            }
        }
    }


    private fun makePhoneCall(phoneNumber: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), CALL_REQUEST_CODE)
        } else {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(callIntent)
        }
    }


    private fun getSavedServerIP(): String? {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(ipAddressKey, null)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection() // Ensure detection is stopped
        clientSocket?.close() // Close socket on destroy
    }



    //---------------LOCATION PART-----------------------------------------------------------------------------------------

    private fun displayCurrentLocation() {
        CoroutineScope(Dispatchers.Main).launch {
            // Use the utility method to fetch location and return it via callback
            LocationUtils.getLastKnownLocation(fusedLocationClient, this@DetectionActivity) { location ->
                if (location != null) {
                    // Update TextViews with location data
                    latitudeTextView.text = "Latitude - ${location.latitude}"
                    longitudeTextView.text = "Longitude - ${location.longitude}"

                    Toast.makeText(this@DetectionActivity, "Location fetched!", Toast.LENGTH_SHORT).show()

                    // Save the location for future use
                    saveLocation(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this@DetectionActivity, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveLocation(latitude: Double, longitude: Double) {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(LATITUDE_KEY, latitude.toString())
        editor.putString(LONGITUDE_KEY, longitude.toString())
        editor.apply() // Save changes asynchronously
    }


    private fun getSavedLocation(): Pair<Double, Double>? {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val savedLatitude = sharedPreferences.getString(LATITUDE_KEY, null)
        val savedLongitude = sharedPreferences.getString(LONGITUDE_KEY, null)

        return if (savedLatitude != null && savedLongitude != null) {
            Pair(savedLatitude.toDouble(), savedLongitude.toDouble())
        } else {
            null
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
}
