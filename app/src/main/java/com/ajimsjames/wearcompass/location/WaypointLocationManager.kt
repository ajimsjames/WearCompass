package com.ajimsjames.wearcompass.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

data class Waypoint(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class WaypointLocationManager(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val prefs: SharedPreferences = context.getSharedPreferences("wear_compass_waypoints", Context.MODE_PRIVATE)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _waypoints = MutableStateFlow<List<Waypoint>>(emptyList())
    val waypoints: StateFlow<List<Waypoint>> = _waypoints

    private val _activeWaypoint = MutableStateFlow<Waypoint?>(null)
    val activeWaypoint: StateFlow<Waypoint?> = _activeWaypoint

    init {
        loadWaypoints()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    2f,
                    this
                )
            }
            
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = lastGps ?: lastNet
            if (best != null) {
                _currentLocation.value = best
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    fun saveCurrentLocationAsWaypoint(name: String): Waypoint? {
        val loc = currentLocation.value ?: return null
        val waypoint = Waypoint(
            id = System.currentTimeMillis().toString(),
            name = name,
            latitude = loc.latitude,
            longitude = loc.longitude
        )
        val currentList = _waypoints.value.toMutableList()
        currentList.add(0, waypoint)
        _waypoints.value = currentList
        _activeWaypoint.value = waypoint
        saveWaypointsToPrefs()
        return waypoint
    }

    fun selectWaypoint(waypoint: Waypoint?) {
        _activeWaypoint.value = waypoint
    }

    fun deleteWaypoint(waypoint: Waypoint) {
        val currentList = _waypoints.value.toMutableList()
        currentList.removeIf { it.id == waypoint.id }
        _waypoints.value = currentList
        if (_activeWaypoint.value?.id == waypoint.id) {
            _activeWaypoint.value = currentList.firstOrNull()
        }
        saveWaypointsToPrefs()
    }

    private fun loadWaypoints() {
        val jsonStr = prefs.getString("waypoints_json", null) ?: return
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<Waypoint>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Waypoint(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _waypoints.value = list
            _activeWaypoint.value = list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveWaypointsToPrefs() {
        val jsonArray = JSONArray()
        _waypoints.value.forEach { wp ->
            val obj = JSONObject().apply {
                put("id", wp.id)
                put("name", wp.name)
                put("latitude", wp.latitude)
                put("longitude", wp.longitude)
                put("timestamp", wp.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("waypoints_json", jsonArray.toString()).apply()
    }

    fun getDistanceAndBearingToActiveWaypoint(): Pair<Float, Float>? {
        val loc = currentLocation.value ?: return null
        val wp = activeWaypoint.value ?: return null

        val results = FloatArray(2)
        Location.distanceBetween(
            loc.latitude,
            loc.longitude,
            wp.latitude,
            wp.longitude,
            results
        )
        val distanceMeters = results[0]
        var bearing = results[1]
        if (bearing < 0) bearing += 360f

        return Pair(distanceMeters, bearing)
    }
}
