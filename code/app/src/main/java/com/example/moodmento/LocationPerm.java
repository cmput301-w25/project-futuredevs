package com.example.moodmento;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

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
        boolean hasCoarsePerm = this.hasLocationPerm(LOC_PERM_COARSE);
        boolean hasFinePerm = this.hasLocationPerm(LOC_PERM_FINE);
        return hasCoarsePerm || hasFinePerm;
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
                new String[] {LOC_PERM_COARSE, LOC_PERM_FINE},
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
        if (this.hasLocationPermission()) {
			this.fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(l -> {
                    if (l == null) {
                        fusedLocationClient
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(onSuccessListener)
                        .addOnFailureListener(e -> {
                            Toast.makeText(this.context, "Couldn't get your location", Toast.LENGTH_SHORT)
                                 .show();

                            Log.e("LocationPerm", "Error getting current location", e);
                        });
                    }
                    else {
                        onSuccessListener.onSuccess(l);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this.context, "Couldn't get your last location", Toast.LENGTH_SHORT)
                         .show();

                    Log.e("LocationPerm", "Error getting current location", e);
                });

        }
        else {
            this.requestLocationPermission();
        }
    }
}