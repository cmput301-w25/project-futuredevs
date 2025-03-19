package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
    private static final String LOC_PERM_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String LOC_PERM_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    /**
     * Creates a {@code LocationPerm} object within the given {@code context}
     * which is used to obtain the current location permissions and to grant
     * the user prompts for permissions.
     *
     * @param context the context to obtain permissions from
     */
    public LocationPerm(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Checks if the user has the required fine location permissions.
     */
    public boolean hasLocationPermission() {
        boolean hasFinePerm = this.hasLocationPerm(LOC_PERM_FINE);
        boolean hasCoarsePerm = this.hasLocationPerm(LOC_PERM_COARSE);
        return hasFinePerm || hasCoarsePerm;
    }

    private boolean hasLocationPerm(String perm) {
        return ContextCompat.checkSelfPermission(this.context, perm)
                    == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests the location permissions for fine locating.
     */
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions((Activity) this.context,
                new String[] {LOC_PERM_FINE, LOC_PERM_COARSE},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Retrieves the last known position of the user and if the user has
     * given the required permissions and the location can be obtained, then
     * the given {@code onSuccessListener} is invoked. If the user has not
     * given permission for the location, then this will instead request
     * location permissions.
     *
     * @param onSuccessListener the callback to invoke on successfully obtaining
     *                          the user's position
     */
    @SuppressLint("MissingPermission")
	public void getLastKnownLocation(OnSuccessListener<Location> onSuccessListener) {
        if (hasLocationPermission()) {
			this.fusedLocationClient.getLastLocation()
							   .addOnSuccessListener(onSuccessListener)
							   .addOnFailureListener(e -> {
                        Log.e("LocationPerm", "Error getting last known location", e);
                    });
        }
        else {
            requestLocationPermission();
        }
    }
}