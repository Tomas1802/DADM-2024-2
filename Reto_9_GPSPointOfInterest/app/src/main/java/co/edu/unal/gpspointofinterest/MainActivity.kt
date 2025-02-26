package co.edu.unal.gpspointofinterest

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var searchButton: Button
    private lateinit var radiusInput: EditText
    private val defaultLocation = LatLng(0.0, 0.0) // Replace with your default location
    private val minRadiusMeters = 500.0 // Minimum allowed radius (in meters)
    private val maxRadiusMeters = 5000.0 // Maximum allowed radius (in meters)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableUserLocation()
        } else {
            Toast.makeText(this, "Location permission is required to show your location.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        searchButton = findViewById(R.id.searchButton)
        radiusInput = findViewById(R.id.radiusInput)

        searchButton.setOnClickListener {
            val radiusText = radiusInput.text.toString()
            val radiusKm = radiusText.toDoubleOrNull()
            if (radiusKm != null && radiusKm > 0) {
                val radiusMeters = radiusKm * 1000
                if (radiusMeters in minRadiusMeters..maxRadiusMeters) {
                    fetchNearbyPlacesWithRadius(radiusMeters)
                } else {
                    Toast.makeText(
                        this,
                        "Radius must be between ${minRadiusMeters / 1000} km and ${maxRadiusMeters / 1000} km.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid radius in kilometers.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableUserLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Location permission is required for this app.", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            centerMapOnUserLocation()
        } else {
            Toast.makeText(this, "Location permission is not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    mMap.addMarker(
                        MarkerOptions()
                            .position(userLocation)
                            .title("You are here")
                    )
                } else {
                    Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchNearbyPlacesWithRadius(radius: Double) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    val apiKey = "AIzaSyCamsOARsVwfMJpWuBuIDz7u6JT5HEHOcU"
                    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?location=${userLatLng.latitude},${userLatLng.longitude}" +
                            "&radius=${radius.toInt()}" +
                            "&key=$apiKey"

                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val body = response.body()?.string()
                            if (!body.isNullOrEmpty()) {
                                val json = JSONObject(body)
                                val results = json.optJSONArray("results")

                                if (results != null && results.length() > 0) {
                                    runOnUiThread {
                                        mMap.clear()
                                        for (i in 0 until results.length()) {
                                            val place = results.getJSONObject(i)
                                            val name = place.optString("name", "Unknown Place")
                                            val location = place.getJSONObject("geometry").getJSONObject("location")
                                            val lat = location.getDouble("lat")
                                            val lng = location.getDouble("lng")

                                            mMap.addMarker(
                                                MarkerOptions()
                                                    .position(LatLng(lat, lng))
                                                    .title(name)
                                            )
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "No places found within the specified radius.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    })
                } else {
                    Toast.makeText(this, "Unable to get current location.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }
}
