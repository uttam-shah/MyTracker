package com.example.mytracking

import android.app.Application
import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mytracking.retrofit.LocationRequest
import com.example.mytracking.retrofit.RetrofitController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MainRepository(application)

    private val _screenOnTime = MutableLiveData<String>()
    val screenOnTime: LiveData<String> get() = _screenOnTime

    private val _location = MutableLiveData<Location?>()
    val location: MutableLiveData<Location?> get() = _location

    private val retrofitController = RetrofitController.getInstance()

    private val _locationSent = MutableLiveData<Boolean>()
    val locationSent: LiveData<Boolean> get() = _locationSent

    private val _locationError = MutableLiveData<String>()
    val locationError: LiveData<String> get() = _locationError

    // SharedPreferences for storing data
    private val sharedPreferences = getApplication<Application>().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    // Update screen-on time and store it in SharedPreferences
    fun updateScreenOnTime() {
        val timeInSeconds = repository.updateScreenOnTime()
        _screenOnTime.value = formatTime(timeInSeconds)
    }

    // Reset last screen-on time
    fun resetLastScreenOnTime() {
        repository.resetLastScreenOnTime()
    }

    // Retrieve and set the current location, and store it in SharedPreferences
    fun getLocation() {
        val location = repository.getLocation()
        _location.value = location

        // Convert location to a readable string format
        val locationString = location?.let { "${it.latitude}, ${it.longitude}" } ?: "No Location Available"

        // Store the location string in SharedPreferences
        editor.putString("last_location", locationString)
        editor.apply()
    }

    // Format time from seconds to hours and minutes
    private fun formatTime(seconds: Long): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        return String.format("%02d hours, %02d minutes", hours, minutes)
    }


    // Send the location to the API and handle the response
    fun sendLocationToApi(locationString: String) {
        val locationRequest = LocationRequest(location = locationString)
        retrofitController.getApiService().sendLocation(locationRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        _locationSent.value = true
                        Toast.makeText(getApplication(), "Location Sent", Toast.LENGTH_SHORT).show()
                    } else {
                        _locationSent.value = false
                        _locationError.value = "Failed to send location: ${response.message()}"
                        Toast.makeText(getApplication(), "Failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    _locationSent.value = false
                    _locationError.value = "Network error: ${t.message}"
                    Toast.makeText(getApplication(), "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Function to retrieve the screen-on time from SharedPreferences
    fun getStoredScreenOnTime(): String {
        val timeInSeconds = sharedPreferences.getLong("screen_on_time_seconds", 0L)
        return formatTime(timeInSeconds)
    }

    // Function to retrieve the last known location from SharedPreferences
    fun getStoredLocation(): String? {
        return sharedPreferences.getString("last_location", "No Location Available")
    }
}
