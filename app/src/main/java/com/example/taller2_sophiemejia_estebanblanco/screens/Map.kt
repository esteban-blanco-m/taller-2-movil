package com.example.taller2_sophiemejia_estebanblanco.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.taller2_sophiemejia_estebanblanco.lightSensor
import com.example.taller2_sophiemejia_estebanblanco.sensorManager
import androidx.navigation.NavController
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.taller2_sophiemejia_estebanblanco.BuildConfig
import com.example.taller2_sophiemejia_estebanblanco.R
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import com.google.maps.android.compose.*
import com.example.taller2_sophiemejia_estebanblanco.util.findAddress
import com.example.taller2_sophiemejia_estebanblanco.util.findLocation
import java.util.Locale

data class MyMarker(
    var position: LatLng = LatLng(4.627293, -74.063228),
    var title: String = "Marker",
    var snippet: String ="Desc"
)

fun guardarUbi(context: Context, location: Location) {
    try {
        val file = File(context.filesDir, "locations.json")
        val jsonArray = if (file.exists()) JSONArray(file.readText()) else JSONArray()

        val jsonObject = JSONObject()
        jsonObject.put("lat", location.latitude)
        jsonObject.put("lng", location.longitude)
        jsonObject.put("time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        jsonArray.put(jsonObject)
        file.writeText(jsonArray.toString())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



fun cargarUbi(context: Context): List<LatLng> {
    val list = mutableListOf<LatLng>()
    try {
        val file = File(context.filesDir, "locations.json")
        if (file.exists()) {
            val jsonArray = JSONArray(file.readText())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(LatLng(obj.getDouble("lat"), obj.getDouble("lng")))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

suspend fun rutasApiGoogle(origin: LatLng, destination: LatLng, apiKey: String): List<LatLng>? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"
            val response = URL(url).readText()
            val jsonObject = JSONObject(response)

            val status = jsonObject.getString("status")
            if (status != "OK") {
                Log.e("MAPS_DEBUG", "Error de API: ${jsonObject.optString("error_message")}")
                return@withContext null
            }

            val routesArray = jsonObject.getJSONArray("routes")
            if (routesArray.length() > 0) {
                val route = routesArray.getJSONObject(0)
                val polylineEncoded = route.getJSONObject("overview_polyline").getString("points")
                return@withContext PolyUtil.decode(polylineEncoded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController? = null) {
    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val lightMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.lightmap)
    val darkMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.darkmap)
    var currentMapStyle by remember { mutableStateOf(lightMapStyle) }
    val bogota = LatLng(4.627293, -74.063228)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bogota, 18f)
    }

    val searchMarker = rememberMarkerState(position = bogota)
    var searchMarkerTitle by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf(bogota) }
    var lastSavedLocation by remember { mutableStateOf<Location?>(null) }
    var searchPlace by remember { mutableStateOf("") }


    var currentRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var historyRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }


    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(30f).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    currentLocation = LatLng(location.latitude, location.longitude)

                    if (lastSavedLocation == null || lastSavedLocation!!.distanceTo(location) >= 30f) {
                        guardarUbi(context, location)
                        lastSavedLocation = location
                    }
                }


            }

        }


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
                    lastSavedLocation = it
                }
            }
        }
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values[0]
                    Log.i("MapApp", lux.toString())
                    currentMapStyle = if (lux < 2000) darkMapStyle else lightMapStyle
                }
            }
        }
    }

    DisposableEffect(Unit) {
        lightSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    fun calculoDistancia(targetLocation: LatLng) {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude, currentLocation.longitude,
            targetLocation.latitude, targetLocation.longitude,
            results
        )
        Toast.makeText(context, "Distancia: ${results[0].toInt()} metros", Toast.LENGTH_LONG).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = currentMapStyle, isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true, compassEnabled = true),

            onMapLongClick = { position ->
                val address = findAddress(position)
                address?.let {
                    Log.i("TAG", it)
                    searchMarker.position = position
                    searchMarkerTitle = it
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(position, 15F)

                    calculoDistancia(position)

                    coroutineScope.launch {
                        val route = rutasApiGoogle(currentLocation, position, BuildConfig.MAPS_API_KEY)
                        if (route != null && route.isNotEmpty()) {
                            currentRoutePoints = route
                        } else {
                            currentRoutePoints = listOf(currentLocation, position)
                            Toast.makeText(context, "No se pudo trazar ruta por calles", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        ) {


            Marker(
                state = searchMarker,
                title = searchMarkerTitle,
            )

            if (currentRoutePoints.isNotEmpty()) {
                Polyline(points = currentRoutePoints, color = Color.Blue, width = 12f)
            }
            if (historyRoutePoints.isNotEmpty()) {
                Polyline(points = historyRoutePoints, color = Color.Red, width = 12f)
            }
        }

        TextField(
            value = searchPlace,
            onValueChange = { searchPlace = it },
            label = { Text("Ingresa una dirección") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 48.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    val locationFound = findLocation(searchPlace)
                    if (locationFound != null) {
                        searchMarker.position = locationFound
                        searchMarkerTitle = searchPlace
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(locationFound, 15f)

                        calculoDistancia(locationFound)

                        coroutineScope.launch {
                            val route = rutasApiGoogle(currentLocation, locationFound, BuildConfig.MAPS_API_KEY)
                            if (route != null && route.isNotEmpty()) {
                                currentRoutePoints = route
                            } else {
                                currentRoutePoints = listOf(currentLocation, locationFound)
                                Toast.makeText(context, "No se pudo trazar ruta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
            )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Button(onClick = {
                historyRoutePoints = cargarUbi(context)
                if (historyRoutePoints.isEmpty()) {
                    Toast.makeText(context, "No hay historial > 30 metros guardado", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Historial")
            }
        }
    }
}