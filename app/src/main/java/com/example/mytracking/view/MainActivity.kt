package com.example.mytracking.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.mytracking.ScreenOnTimeService
import com.example.mytracking.viewmodel.MainViewModel
import com.example.mytracking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start the foreground service to track screen-on time
        startScreenOnTimeService()

        // Observe ViewModel data
        viewModel.screenOnTime.observe(this, Observer { time ->
            binding.screenOnTimeTextView.text = "Screen On Time: $time"
        })

        viewModel.location.observe(this, Observer { location ->
            location?.let {
                val locationText = "Lat: ${it.latitude}, Long: ${it.longitude}"
                Toast.makeText(this, locationText, Toast.LENGTH_LONG).show()

                // Send location to API
                viewModel.sendLocationToApi(locationText)
            }
        })

        viewModel.locationSent.observe(this, Observer { success ->
            if (success == true) {
                Toast.makeText(this, "Location sent successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send location", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.locationError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        // Set up BroadcastReceiver for screen on/off events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenOnOffReceiver, filter)

        // Button click listener for location
        binding.requestLocationButton.setOnClickListener {
            requestLocationPermission()
        }

        binding.setting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startScreenOnTimeService() {
        val serviceIntent = Intent(this, ScreenOnTimeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }


    private val screenOnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    // Notify ViewModel of screen-on event
                    viewModel.resetLastScreenOnTime()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    // Notify ViewModel of screen-off event
                    viewModel.updateScreenOnTime()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.getLocation()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.getLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOnOffReceiver)
    }
}
