package com.example.taller2_sophiemejia_estebanblanco.util


import com.example.taller2_sophiemejia_estebanblanco.geocoder
import com.google.android.gms.maps.model.LatLng

fun findAddress(location: LatLng): String? {
    try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 2)
        if (addresses != null && !addresses.isEmpty()) {
            val addr = addresses.get(0)
            val locname = addr.getAddressLine(0)
            return locname
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun findLocation(address: String): LatLng? {
    try {
        val addresses = geocoder.getFromLocationName(address, 2)
        if (addresses != null && !addresses.isEmpty()) {
            val addr = addresses.get(0)
            val location = LatLng(addr.latitude, addr.longitude)
            return location
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}