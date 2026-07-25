package com.example.wearcompass.presentation

import android.content.Context
import android.graphics.Paint
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import com.example.wearcompass.location.Waypoint
import com.example.wearcompass.location.WaypointLocationManager
import com.example.wearcompass.sensor.CompassSensorManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(
    sensorManager: CompassSensorManager,
    locationManager: WaypointLocationManager
) {
    val context = LocalContext.current
    val azimuth by sensorManager.azimuth.collectAsState()
    val currentLocation by locationManager.currentLocation.collectAsState()
    val waypoints by locationManager.waypoints.collectAsState()
    val activeWaypoint by locationManager.activeWaypoint.collectAsState()

    var showSaveModal by remember { mutableStateOf(false) }
    var showWaypointListModal by remember { mutableStateOf(false) }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    var lastVibeTime by remember { mutableStateOf(0L) }

    val distAndBearing = remember(azimuth, currentLocation, activeWaypoint) {
        locationManager.getDistanceAndBearingToActiveWaypoint()
    }

    // Trigger haptic vibration when wrist points straight at target waypoint (within ±5°)
    LaunchedEffect(azimuth, distAndBearing) {
        distAndBearing?.let { (_, targetBearing) ->
            val diff = abs((targetBearing - azimuth + 360) % 360)
            if (diff < 5f || diff > 355f) {
                val now = System.currentTimeMillis()
                if (now - lastVibeTime > 2000) { // Limit pulse to once every 2 sec
                    lastVibeTime = now
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Main Circular Compass Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 16.dp.toPx()

            // Outer ring
            drawCircle(
                color = Color(0xFF2C2C2E),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )

            // Inner compass ring rotating opposite to azimuth
            rotate(-azimuth, pivot = center) {
                // Draw 360 Degree Ticks & Labels
                for (deg in 0 until 360 step 15) {
                    val angleRad = Math.toRadians(deg.toDouble() - 90)
                    val isMajor = deg % 30 == 0
                    val tickLen = if (isMajor) 14.dp.toPx() else 8.dp.toPx()

                    val startX = (center.x + (radius - tickLen) * cos(angleRad)).toFloat()
                    val startY = (center.y + (radius - tickLen) * sin(angleRad)).toFloat()
                    val endX = (center.x + radius * cos(angleRad)).toFloat()
                    val endY = (center.y + radius * sin(angleRad)).toFloat()

                    drawLine(
                        color = if (deg == 0) Color.Red else if (isMajor) Color.White else Color.Gray,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 3.dp.toPx() else 1.5f.dp.toPx()
                    )
                }

                // Draw Cardinal Text Labels (N, E, S, W)
                val cardinals = listOf("N" to 0, "E" to 90, "S" to 180, "W" to 270)
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 16.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                cardinals.forEach { (text, deg) ->
                    val angleRad = Math.toRadians(deg.toDouble() - 90)
                    val textRadius = radius - 26.dp.toPx()
                    val tx = (center.x + textRadius * cos(angleRad)).toFloat()
                    val ty = (center.y + textRadius * sin(angleRad)).toFloat() + (paint.textSize / 3f)

                    paint.color = if (text == "N") android.graphics.Color.RED else android.graphics.Color.WHITE
                    drawContext.canvas.nativeCanvas.drawText(text, tx, ty, paint)
                }
            }

            // Top Fixed Indicator Pointer (North Reference)
            val pointerPath = Path().apply {
                moveTo(center.x, center.y - radius + 4.dp.toPx())
                lineTo(center.x - 8.dp.toPx(), center.y - radius - 10.dp.toPx())
                lineTo(center.x + 8.dp.toPx(), center.y - radius - 10.dp.toPx())
                close()
            }
            drawPath(pointerPath, Color.Red)

            // Target Waypoint Arrow Indicator
            distAndBearing?.let { (_, targetBearing) ->
                val relativeBearing = targetBearing - azimuth
                rotate(relativeBearing, pivot = center) {
                    val arrowPath = Path().apply {
                        moveTo(center.x, center.y - radius + 22.dp.toPx())
                        lineTo(center.x - 10.dp.toPx(), center.y - radius + 40.dp.toPx())
                        lineTo(center.x, center.y - radius + 32.dp.toPx())
                        lineTo(center.x + 10.dp.toPx(), center.y - radius + 40.dp.toPx())
                        close()
                    }
                    drawPath(arrowPath, Color(0xFF00E676)) // Glowing Green Target Arrow
                }
            }
        }

        // Center Heading Info Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val formattedAzimuth = azimuth.toInt()
            val cardinalDir = getCardinalDirection(azimuth)
            Text(
                text = "$formattedAzimuth° $cardinalDir",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            if (distAndBearing != null && activeWaypoint != null) {
                val distMeters = distAndBearing.first
                val formattedDist = if (distMeters >= 1000) {
                    "%.1f km".format(distMeters / 1000f)
                } else {
                    "${distMeters.toInt()}m"
                }
                Text(
                    text = "📍 ${activeWaypoint!!.name}: $formattedDist",
                    color = Color(0xFF00E676),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = if (currentLocation != null) "GPS Locked" else "Searching GPS...",
                    color = if (currentLocation != null) Color(0xFF81D4FA) else Color.Yellow,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Bottom Controls Bar (Save Spot, Select Waypoint)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Save Current Location Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1565C0))
                    .clickable {
                        if (currentLocation != null) {
                            showSaveModal = true
                        } else {
                            Toast.makeText(context, "Waiting for GPS fix...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("📍 Save", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Waypoints List Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .clickable { showWaypointListModal = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("📋 Saved (${waypoints.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Modal: Save Spot Presets
        if (showSaveModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFA000000))
                    .clickable { showSaveModal = false }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Save Current Waypoint",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val presetNames = listOf("Parked Car 🚗", "Hotel 🏨", "Camp Site 🏕️", "Starting Trail 🥾", "My Location 📍")
                        presetNames.forEach { name ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2C2C2E))
                                    .clickable {
                                        val saved = locationManager.saveCurrentLocationAsWaypoint(name)
                                        showSaveModal = false
                                        if (saved != null) {
                                            Toast.makeText(context, "Saved $name!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(name, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Modal: Waypoints Manager
        if (showWaypointListModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFA000000))
                    .clickable { showWaypointListModal = false }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.88f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Saved Waypoints",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        if (waypoints.isEmpty()) {
                            Text(
                                text = "No waypoints saved yet.\nTap '📍 Save' on main screen.",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                items(waypoints) { wp ->
                                    val isSelected = activeWaypoint?.id == wp.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF1565C0) else Color(0xFF2C2C2E))
                                            .clickable {
                                                locationManager.selectWaypoint(wp)
                                                showWaypointListModal = false
                                                Toast.makeText(context, "Navigating to ${wp.name}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = wp.name,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = { locationManager.deleteWaypoint(wp) },
                                            modifier = Modifier.size(22.dp),
                                            colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color(0xFFD32F2F))
                                        ) {
                                            Text("✕", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showWaypointListModal = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(26.dp)
                                .padding(top = 4.dp),
                            colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color(0xFF444446))
                        ) {
                            Text("Close", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun getCardinalDirection(azimuth: Float): String {
    return when (azimuth) {
        in 337.5..360.0, in 0.0..22.5 -> "N"
        in 22.5..67.5 -> "NE"
        in 67.5..112.5 -> "E"
        in 112.5..157.5 -> "SE"
        in 157.5..202.5 -> "S"
        in 202.5..247.5 -> "SW"
        in 247.5..292.5 -> "W"
        in 292.5..337.5 -> "NW"
        else -> "N"
    }
}
