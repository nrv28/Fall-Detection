package com.example.falldetection.activity

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetection.R
import com.example.falldetection.utils.LocationUtils // Import LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay


class GPSActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var fetchLocationButton: Button
    private lateinit var mapView: MapView

    companion object {
        private const val sharedPreferencesName = "FallDetectionPreferences"
        private const val latitudeKey = "LATITUDE"
        private const val longitudeKey = "LONGITUDE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE))

        setContentView(R.layout.activity_gps)

        // Initialize views
        latitudeTextView = findViewById(R.id.latitude_value)
        longitudeTextView = findViewById(R.id.longitude_value)
        fetchLocationButton = findViewById(R.id.btn_fetch_location)
        mapView = findViewById(R.id.map_view)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

        // Enable multi-touch controls for zooming and panning
        mapView.setMultiTouchControls(true)

        // Add compass overlay (optional)
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        // Set button click listener to fetch location
        fetchLocationButton.setOnClickListener {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        // Use the utility method to fetch location and return it via callback
        LocationUtils.getLastKnownLocation(fusedLocationClient, this) { location ->
            if (location != null) {
                // Save location to SharedPreferences
                saveLocationToPreferences(location)

                // Update TextViews with location data
                latitudeTextView.text = "Latitude - ${location.latitude}"
                longitudeTextView.text = "Longitude - ${location.longitude}"

                // Display location on the map
                displayLocationOnMap(location)

                Toast.makeText(this, "Location fetched!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocationToPreferences(location: Location) {
        with(sharedPreferences.edit()) {
            putFloat(latitudeKey, location.latitude.toFloat())
            putFloat(longitudeKey, location.longitude.toFloat())
            apply()
        }
    }

    private fun displayLocationOnMap(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        // Set zoom level and center on the location
        mapView.controller.setZoom(15.0) // Zoom level can be adjusted
        mapView.controller.setCenter(geoPoint)

        // Add a marker at the location
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "You are here"
        mapView.overlays.clear() // Clear existing overlays
        mapView.overlays.add(marker)

        mapView.invalidate() // Refresh the map
    }

    override fun onResume() {
        super.onResume()
        // Refresh the map
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Pause the map
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up map resources
        mapView.onDetach()
    }
}
