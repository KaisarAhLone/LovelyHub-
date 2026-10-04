package com.example.lovelyhub.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

object MapLocationHelper {

    const val GOOGLE_MAPS_API_KEY = "AIzaSyDtlHpZt7yEb-EpU1rbN0y9pyshypPil3Q"

    fun openMapDirections(context: Context, locationQuery: String) {
        val query = if (locationQuery.isBlank()) "LPU Campus, Phagwara" else locationQuery
        val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val browserUri = Uri.parse("https:" + "/" + "/www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        }
    }

    fun openMapPicker(context: Context, currentQuery: String) {
        val query = if (currentQuery.isBlank()) "LPU Campus" else currentQuery
        val browserUri = Uri.parse("https:" + "/" + "/www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, browserUri)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun getCurrentGpsAddress(context: Context, onResult: (String) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val areaName = address.subLocality ?: address.locality ?: "Law Gate, LPU"
                            onResult("$areaName ($lat, $lng)")
                        } else {
                            onResult("LPU Campus ($lat, $lng)")
                        }
                    } catch (_: Exception) {
                        onResult("LPU Campus ($lat, $lng)")
                    }
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            onResult("LPU Campus (${lastLoc.latitude}, ${lastLoc.longitude})")
                        } else {
                            onResult("Law Gate, LPU Campus")
                        }
                    }.addOnFailureListener {
                        onResult("Law Gate, LPU Campus")
                    }
                }
            }.addOnFailureListener {
                onResult("Law Gate, LPU Campus")
            }
        } catch (_: Exception) {
            onResult("Law Gate, LPU Campus")
        }
    }
}
