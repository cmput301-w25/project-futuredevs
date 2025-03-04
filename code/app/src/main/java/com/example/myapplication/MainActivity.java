package com.example.myapplication;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private location_perm locationPerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the location_perm class
        locationPerm = new location_perm(this);

        // Check for location permissions
        if (!locationPerm.hasLocationPermission()) {
            // Request the permission
            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLocation();
            } else {
                // Permission denied
                // Handle the case where the user denies the permission
            }
        }
    }

    private void getLocation() {
        locationPerm.getLastKnownLocation(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Use the location object
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                }
            }
        });
    }
}