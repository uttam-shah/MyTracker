package com.example.mytracking

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.core.content.edit

class MainRepository(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Track screen-on time
    private var screenOnTime: Long
    private var lastScreenOnTime: Long

    init {
        // Retrieve the stored screen-on time from SharedPreferences
        screenOnTime = sharedPreferences.getLong("screenOnTime", 0)
        lastScreenOnTime = System.currentTimeMillis()
    }

    fun updateScreenOnTime(): Long {
        val currentTime = System.currentTimeMillis()
        screenOnTime += (currentTime - lastScreenOnTime) / 1000
        lastScreenOnTime = currentTime
        storeScreenOnTime(screenOnTime)
        return screenOnTime
    }

    fun resetLastScreenOnTime() {
        lastScreenOnTime = System.currentTimeMillis()
    }

    fun getLocation(): Location? {
        // Check if the permission is granted
        return if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // Get the last known location using GPS_PROVIDER
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            // Return null if the permission is not granted
            null
        }
    }

    private fun storeScreenOnTime(timeInSeconds: Long) {
        // Store the screen-on time in SharedPreferences
        editor.putLong("screenOnTime", timeInSeconds)
        editor.apply()
    }

    fun getStoredScreenOnTime(): Long {
        // Retrieve the screen-on time from SharedPreferences
        return sharedPreferences.getLong("screenOnTime", 0)
    }
}
