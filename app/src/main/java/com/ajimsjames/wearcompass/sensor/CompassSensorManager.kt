package com.ajimsjames.wearcompass.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var currentAzimuth = 0f
    private val alpha = 0.15f // Low pass filter factor

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            magnetometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (degree < 0) degree += 360f

            smoothAzimuth(degree)
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                var degree = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (degree < 0) degree += 360f

                smoothAzimuth(degree)
            }
        }
    }

    private fun smoothAzimuth(targetDegree: Float) {
        // Handle 359° -> 1° wrap-around smoothing
        var diff = targetDegree - currentAzimuth
        while (diff < -180) diff += 360
        while (diff > 180) diff -= 360

        currentAzimuth += diff * alpha
        if (currentAzimuth < 0) currentAzimuth += 360f
        if (currentAzimuth >= 360) currentAzimuth -= 360f

        _azimuth.value = currentAzimuth
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
