package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class location_perm {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    // Constructor
    public location_perm(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Check if location permissions are granted
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permissions
    public void requestLocationPermission(int requestCode) {
        ActivityCompat.requestPermissions((android.app.Activity) context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode);
    }

    // Get the last known location
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        if (hasLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener((android.app.Activity) context, onSuccessListener);
        } else {
            // Handle the case where permissions are not granted
            // You can throw an exception or log an error
        }
    }
}
