package karpenko.diploma.taxiapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

import karpenko.diploma.taxiapp.databinding.ActivityDriverMapsBinding;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityDriverMapsBinding binding;

    public static final int CHECK_SETTINGS_CODE = 123;
    private static final int REQUEST_LOCATION_PERMISSION = 321;

    private Button settingsButton;
    private Button logOutButton;
    private boolean isLocationUpdatesActive;
    private String locationUpdateTime;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        settingsButton = findViewById(R.id.settingsButton);
        logOutButton = findViewById(R.id.logOutDriverButton);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        buildLocationRequest();
        buildLocationCallback();
        buildLocationSettingsRequest();

        startLocationUpdates();

        logOutButton.setOnClickListener(view -> {
            auth.signOut();
            signOutDriver();
        });
    }

    private void signOutDriver() {
        String driverUSerId = currentUser.getUid();
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference()
                .child("drivers");
        GeoFire geoFire = new GeoFire(drivers);
        geoFire.removeLocation(driverUSerId);

        Intent intent = new Intent(this, ChooseModeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocation != null) {
            // Add a marker in Sydney and move the camera
            LatLng driverLocation = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver is here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));
        }

    }

    private void stopLocationUpdates() {

        if (!isLocationUpdatesActive) {
            return;
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, task -> {
                    isLocationUpdatesActive = false;
                });

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        isLocationUpdatesActive = true;

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    if (ActivityCompat
                            .checkSelfPermission(DriverMapsActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat
                            .checkSelfPermission(DriverMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationClient.requestLocationUpdates(
                            locationRequest, locationCallback,
                            Looper.myLooper()
                    );
                    updateLocationUI();
                }).addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();

                    switch (statusCode) {
                        case LocationSettingsStatusCodes
                                .RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException
                                        = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(
                                        DriverMapsActivity.this,
                                        CHECK_SETTINGS_CODE
                                );
                            } catch (IntentSender.SendIntentException s) {
                                Toast.makeText(this, s.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                            break;
                        case LocationSettingsStatusCodes
                                .SETTINGS_CHANGE_UNAVAILABLE:
                            String message =
                                    "Adjust location settings in your device";
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            isLocationUpdatesActive = false;
                    }

                    updateLocationUI();

                });

    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        locationSettingsRequest = builder.build();
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();

                updateLocationUI();
            }
        };
    }

    private void updateLocationUI() {

        if (currentLocation != null) {
            LatLng driverLocation = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver is here"));

            String driverUserId = currentUser.getUid();

            DatabaseReference driversGeoFire = FirebaseDatabase.getInstance().getReference()
                    .child("driversGeoFire");

            DatabaseReference drivers = FirebaseDatabase.getInstance().getReference()
                    .child("drivers");

            drivers.setValue(true);

            GeoFire geoFire = new GeoFire(driversGeoFire);
            geoFire.setLocation(driverUserId, new GeoLocation(currentLocation.getLatitude(),
                    currentLocation.getLongitude()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_SETTINGS_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d("MainActivity", "User agreed");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("MainActivity", "User not agreed");
                        isLocationUpdatesActive = false;
                        updateLocationUI();
                        break;
                }
        }
    }

    private void buildLocationRequest() {

        locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(3000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(1000);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationUpdatesActive && checkLocationPermission()) {
            startLocationUpdates();
        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void requestLocationPermission() {

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackBar("Location permission not granted",
                    "Retry", view -> ActivityCompat.requestPermissions(DriverMapsActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            }, REQUEST_LOCATION_PERMISSION));
        } else {
            ActivityCompat.requestPermissions(DriverMapsActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_LOCATION_PERMISSION);
        }

    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {

        Snackbar.make(findViewById(android.R.id.content), mainText,
                        Snackbar.LENGTH_LONG).setAction(action, listener)
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length <= 0) {
                Log.d("MainActivity", "Request was canceled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdatesActive) {
                    startLocationUpdates();
                }
            } else {
                showSnackBar(
                        "Turn on location on settings",
                        "Settings",
                        view -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package"
                                    , BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                );
            }
        }
    }

    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}