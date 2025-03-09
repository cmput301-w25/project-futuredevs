package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocationPerm  {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    // Constructor

    // Constructor
    public LocationPerm(Context context) {
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
    @SuppressLint("MissingPermission")
    public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        // Check if the location permission is granted
        if (hasLocationPermission()) {
            // Permissions are granted, fetch the location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(e -> {
                        // Handle failure to fetch location, such as no location available
                        Log.e("LocationPerm", "Error getting last known location", e);
                    });
        } else {
            // Permissions are not granted, request them
            requestLocationPermission(1001);  // You can use your own request code here
        }
    }
}