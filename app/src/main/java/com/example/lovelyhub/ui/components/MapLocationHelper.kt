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
                            val subLoc = address.subLocality ?: ""
                            val local = address.locality ?: ""
                            val adminArea = address.adminArea ?: ""
                            val fullLine = address.getAddressLine(0) ?: ""

                            val formatted = when {
                                subLoc.isNotBlank() && local.isNotBlank() -> "$subLoc, $local"
                                local.isNotBlank() && adminArea.isNotBlank() -> "$local, $adminArea"
                                fullLine.isNotBlank() -> fullLine
                                else -> "Lat: $lat, Lng: $lng"
                            }
                            onResult(formatted)
                        } else {
                            onResult("Lat: $lat, Lng: $lng")
                        }
                    } catch (_: Exception) {
                        onResult("Lat: $lat, Lng: $lng")
                    }
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            val lat = lastLoc.latitude
                            val lng = lastLoc.longitude
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(lat, lng, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val subLoc = address.subLocality ?: ""
                                    val local = address.locality ?: ""
                                    val fullLine = address.getAddressLine(0) ?: ""
                                    val formatted = when {
                                        subLoc.isNotBlank() && local.isNotBlank() -> "$subLoc, $local"
                                        fullLine.isNotBlank() -> fullLine
                                        else -> "Lat: $lat, Lng: $lng"
                                    }
                                    onResult(formatted)
                                } else {
                                    onResult("Lat: $lat, Lng: $lng")
                                }
                            } catch (_: Exception) {
                                onResult("Lat: $lat, Lng: $lng")
                            }
                        } else {
                            onResult("Please ensure GPS location is turned ON")
                        }
                    }.addOnFailureListener {
                        onResult("Please ensure GPS location is turned ON")
                    }
                }
            }.addOnFailureListener {
                onResult("Please ensure GPS location is turned ON")
            }
        } catch (_: Exception) {
            onResult("Please ensure GPS location is turned ON")
        }
    }
}
