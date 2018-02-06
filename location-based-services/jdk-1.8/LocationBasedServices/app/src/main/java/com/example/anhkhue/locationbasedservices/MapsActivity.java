package com.example.anhkhue.locationbasedservices;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getCanonicalName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2204;
    private static final float DEFAULT_ZOOM = 18f;

    private GoogleMap mMap;
    private EditText editSearch;
    private ImageView iconGps;
    private ImageView iconTheme;

    private boolean mLocationPermissionGranted = false;
    private boolean isDark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editSearch = findViewById(R.id.editSearch);
        iconGps = findViewById(R.id.iconGps);
        iconTheme = findViewById(R.id.iconTheme);

        askLocationPermission();
    }

    private void askLocationPermission() {
        Log.d(TAG, "Ask location permission.");

        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(this,
                                               Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                                               Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                                              permissions,
                                              LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Request permission result.");

        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            break;
                        }
                    }
                    mLocationPermissionGranted = true;
                    onMapReady(mMap);
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map ready.");

        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            updateMapUI();
        } else {
            askLocationPermission();
        }
    }

    private void updateMapUI() {
        try {
            mMap.setMyLocationEnabled(true);

            enableMapComponents();

//            mMap.getUiSettings().setCompassEnabled(true);

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        }
    }

    private void enableMapComponents() {
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                event.getAction() == KeyEvent.KEYCODE_ENTER ||
                event.getAction() == KeyEvent.ACTION_DOWN) {
                searchLocation();
            }
            return false;
        });

        iconGps.setOnClickListener(v -> getDeviceLocation());

        iconTheme.setOnClickListener(v -> {
            if (!isDark) {
                changeTheme(R.raw.dark_theme, R.drawable.ic_gps_dark, R.drawable.ic_light);
            } else {
                changeTheme(R.raw.light_theme, R.drawable.ic_gps, R.drawable.ic_dark);
            }
        });
    }

    private void changeTheme(int theme, int gps, int mode) {
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, theme));

            if (success) {
                Log.d(TAG, "Change Theme");

                iconGps.setImageResource(gps);
                iconTheme.setImageResource(mode);

                isDark = true;
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resources.NotFoundException: " + e.getMessage());
        }
    }

    private void searchLocation() {
        Log.d(TAG, "Search location.");

        String searchValue = editSearch.getText().toString();

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocationName(searchValue, 1);

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);

                Log.d(TAG, "Location found: " + address.toString());

                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                           DEFAULT_ZOOM,
                           address.getAddressLine(0));
            } else {
                Log.d(TAG, "No location found.");

                Toast.makeText(this,
                               "Unable to find location with keyword '" + searchValue + "'",
                               Toast.LENGTH_SHORT)
                     .show();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "Get device location.");

        FusedLocationProviderClient mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        try {
            Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();

            locationTask.addOnCompleteListener(task -> {
                if (task.isComplete()) {
                    Log.d(TAG, "Device's location found.");

                    Location location = task.getResult();

                    moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                               DEFAULT_ZOOM,
                               "Your location");
                } else {
                    Log.d(TAG, "Device's location not found.");

                    Toast.makeText(getBaseContext(),
                                   "Unable to locate your device.",
                                   Toast.LENGTH_SHORT)
                         .show();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }
}
