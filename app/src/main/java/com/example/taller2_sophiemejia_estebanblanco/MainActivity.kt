package com.example.taller2_sophiemejia_estebanblanco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taller2_sophiemejia_estebanblanco.navigation.Navigation
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Geocoder
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.taller2_sophiemejia_estebanblanco.navigation.Navigation

lateinit var sensorManager: SensorManager
var lightSensor: Sensor? = null
lateinit var geocoder: Geocoder

class MainActivity : ComponentActivity() {

    // Launcher para pedir permisos de ubicación
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (!fineLocationGranted && !coarseLocationGranted) {
            Toast.makeText(this, "Los permisos de ubicación son necesarios", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de servicios
        geocoder = Geocoder(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        enableEdgeToEdge()

        // Verificamos y solicitamos permisos al iniciar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        setContent {
            // Llamamos a la navegación principal
            Navigation()
        }
    }
}