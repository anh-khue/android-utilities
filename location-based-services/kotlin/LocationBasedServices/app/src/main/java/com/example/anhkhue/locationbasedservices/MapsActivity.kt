package com.example.anhkhue.locationbasedservices

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val TAG = "MapsActivity"
        const val LOCATION_PERMISSION_REQUEST_CODE = 2204
        const val DEFAULT_ZOOM = 18f
    }

    private lateinit var mMap: GoogleMap

    private var locationPermissionGranted = false
    private var isDark = false

    private lateinit var editSearch: EditText
    private lateinit var iconGps: ImageView
    private lateinit var iconTheme: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        editSearch = findViewById(R.id.editSearch)
        iconGps = findViewById(R.id.ic_gps)
        iconTheme = findViewById(R.id.ic_theme)

        askLocationPermission()
    }

    private fun askLocationPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                  Manifest.permission.ACCESS_COARSE_LOCATION)

        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                                              permissions,
                                              LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (!grantResults.isEmpty()) {
                    for (grantResult in grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false
                            break
                        }
                    }
                    locationPermissionGranted = true
                    onMapReady(mMap)
                } else {
                    askLocationPermission()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (locationPermissionGranted) {
            getDeviceLocation()

            initMapComponents()
        }
    }

    private fun initMapComponents() {
        editSearch.setOnEditorActionListener { _, actionId, event ->
            var handle = false

            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                event.action == KeyEvent.ACTION_DOWN ||
                event.action == KeyEvent.KEYCODE_ENTER) {

                searchLocation()
                handle = true
            }
            handle
        }

        iconGps.setOnClickListener { getDeviceLocation() }

        iconTheme.setOnClickListener {
            if (!isDark) {
                changeTheme(R.raw.dark_theme, R.drawable.ic_light, R.drawable.ic_gps_dark)
                isDark = true
            } else {
                changeTheme()
                isDark = false
            }
        }
    }

    private fun changeTheme(theme: Int = R.raw.light_theme,
                            themeIcon: Int = R.drawable.ic_dark,
                            gpsIcon: Int = R.drawable.ic_gps) {
        try {
            val changed = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, theme))
            if (changed) {
                Log.i(TAG, "Theme changed.")
                iconGps.setImageResource(gpsIcon)
                iconTheme.setImageResource(themeIcon)
            } else {
                Log.i(TAG, "Unable to parse theme.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Resources.NotFoundException: " + e.message)
        }
    }

    private fun searchLocation() {
        val searchValue = editSearch.text.toString()

        val geocoder = Geocoder(this)

        try {
            val addresses = geocoder.getFromLocationName(searchValue, 1)
            if (!addresses.isEmpty()) {
                val address = addresses[0]
                Log.i(TAG, address.toString())

                moveCamera(LatLng(address.latitude, address.longitude),
                           DEFAULT_ZOOM,
                           address.getAddressLine(0))
            } else {
                Log.d(TAG, "Location not found.")

                Toast.makeText(this,
                               "Unable to find location.",
                               Toast.LENGTH_SHORT)
                        .show()
            }
        } catch (e: IOException) {

        }
    }

    private fun getDeviceLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            val locationTask = fusedLocationProviderClient.lastLocation

            locationTask.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Location found.")

                    val location = it.result
                    moveCamera(LatLng(location.latitude, location.longitude))
                } else {
                    Log.d(TAG, "Location not found.")
                    Toast.makeText(this,
                                   "Unable to find location",
                                   Toast.LENGTH_SHORT)
                            .show()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: " + e.message)
        }
    }

    private fun moveCamera(latLng: LatLng,
                           zoom: Float = DEFAULT_ZOOM,
                           title: String = "Your location") {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        mMap.addMarker(MarkerOptions().position(latLng).title(title))
    }
}
