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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getCanonicalName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2204;
    private static final float DEFAULT_ZOOM = 18f;
    private static final String CURRENT_LOCATION = "Your location";

    private GoogleMap mMap;

    private EditText editSearch;
    private ImageView iconGps;
    private ImageView iconTheme;

    private boolean mLocationPermissionGranted = false;
    private boolean darkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Create Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        editSearch = findViewById(R.id.editSearch);
        iconGps = findViewById(R.id.ic_gps);
        iconTheme = findViewById(R.id.ic_theme);

        getLocationPermission();

        initComponents();
    }

    private void initComponents() {
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event.getAction() == KeyEvent.ACTION_DOWN ||
                    event.getAction() == KeyEvent.KEYCODE_ENTER)

                    searchLocation();

                return false;
            }
        });

        iconGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        iconTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean success;
                    if (!darkTheme) {
                        success = isDark();
                    } else {
                        success = isLight();
                    }

                    if (!success) {
                        Log.e(TAG, "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Can't find style. Error: ", e);
                }
            }

            private boolean isLight() {
                boolean success;
                darkTheme = false;
                success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        getBaseContext(), R.raw.light_json));
                iconGps.setImageResource(R.drawable.ic_gps);
                iconTheme.setImageResource(R.drawable.ic_theme);
                return success;
            }

            private boolean isDark() {
                boolean success;
                darkTheme = true;
                success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        getBaseContext(), R.raw.dark_json));
                iconGps.setImageResource(R.drawable.ic_gps_light);
                iconTheme.setImageResource(R.drawable.ic_theme_light);
                return success;
            }
        });
    }

    private void searchLocation() {
        String searchValue = editSearch.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocationName(searchValue, 1);

            if (addresses.size() > 0) {
                Log.d(TAG, "Location found on keyword '" + searchValue + "'.");

                Address address = addresses.get(0);
                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                           DEFAULT_ZOOM,
                           address.getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
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
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            updateLocationUi();
        } else {
            getLocationPermission();
        }

    }

    private void updateLocationUi() {
        try {
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception: " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            final Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();

            locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Current location found.");

                        Location location = locationTask.getResult();
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                                   DEFAULT_ZOOM,
                                   CURRENT_LOCATION);
                    } else {
                        Log.d(TAG, "Cannot find current location.");

                        Toast.makeText(getBaseContext(),
                                       "Unable to find location.",
                                       Toast.LENGTH_SHORT)
                             .show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals(CURRENT_LOCATION))
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }
}
