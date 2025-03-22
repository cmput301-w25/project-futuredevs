package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SeekBar distanceSeekBar;
    private ImageView locationIcon;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        distanceSeekBar = view.findViewById(R.id.distanceSeekBar);
        locationIcon = view.findViewById(R.id.locationIcon);

        // Load Google Map
        //SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        //if (mapFragment != null) {
         //   mapFragment.getMapAsync(this);
       // }

        // Handle slider movement
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMapRadius(progress + 1); // Scale from 1 to 10 km
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateMapRadius(int radiusKm) {
        // TODO: Implement logic to filter locations on the map based on radius
        System.out.println("Selected Radius: " + radiusKm + " km");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // You can now use the mMap object to customize the map
        // For example, set the initial camera position, add markers, etc.
    }
}