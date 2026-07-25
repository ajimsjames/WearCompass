package com.example.wearcompass

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.wearcompass.location.WaypointLocationManager
import com.example.wearcompass.presentation.CompassScreen
import com.example.wearcompass.sensor.CompassSensorManager

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: CompassSensorManager
    private lateinit var locationManager: WaypointLocationManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            locationManager.startLocationUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = CompassSensorManager(this)
        locationManager = WaypointLocationManager(this)

        checkAndRequestPermissions()

        setContent {
            CompassScreen(
                sensorManager = sensorManager,
                locationManager = locationManager
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation != PackageManager.PERMISSION_GRANTED && coarseLocation != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            locationManager.startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.start()
        locationManager.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.stop()
        locationManager.stopLocationUpdates()
    }
}
