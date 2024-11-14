// utils/LocationUtils.kt

package com.example.falldetection.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

object LocationUtils {

    @SuppressLint("MissingPermission") // Handle permissions before calling this method
    fun getLastKnownLocation(
        fusedLocationClient: FusedLocationProviderClient,
        context: Context,
        callback: (Location?) -> Unit
    ) {
        try {
            val locationTask: Task<Location> = fusedLocationClient.lastLocation
            locationTask.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location) // Return location via callback
                } else {
                    callback(null) // Return null if no location is available
                    Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                callback(null)
                Toast.makeText(context, "Error retrieving location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            callback(null)
            Toast.makeText(context, "Permission not granted to access location.", Toast.LENGTH_SHORT).show()
        }
    }
}
