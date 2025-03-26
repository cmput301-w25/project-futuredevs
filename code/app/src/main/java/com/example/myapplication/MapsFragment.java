package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.LocationPerm;
import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.items.MoodPost;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng userLocation = new LatLng(53.5461, -113.4938); // Default to Edmonton
    private static final float DEFAULT_ZOOM = 12f;
    private final List<MoodPost> filteredMoodPosts = new ArrayList<>();
    private final List<MoodPost> originalMoodPosts = new ArrayList<>();
    private LocationPerm locationPerm;

    // New filter components
    private SeekBar distanceSeekBar;
    private TextView distanceTextView;
    private Button applyFilterButton;
    private int currentFilterDistance = 10; // Default 10 km

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_layout, container, false);

        // Initialize filter components
        distanceSeekBar = view.findViewById(R.id.rangeSeekBar);
        applyFilterButton = view.findViewById(R.id.filterButton);
        distanceTextView = view.findViewById(R.id.distanceTextView);  // 🔹 Add this line


        // Setup seek bar
        distanceSeekBar.setMax(10); // 1-10 km
        distanceSeekBar.setProgress(currentFilterDistance);
        updateDistanceText(currentFilterDistance);

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFilterDistance = progress + 1; // Adjust to 1-10 range
                updateDistanceText(currentFilterDistance);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Setup filter button
        applyFilterButton.setOnClickListener(v -> applyDistanceFilter());

        return view;
    }

    private void updateDistanceText(int distance) {
        distanceTextView.setText(String.format("Filter Radius: %d km", distance));
    }

    private void applyDistanceFilter() {
        if (mMap == null || userLocation == null) return;

        // Clear previous markers and filter visualization
        mMap.clear();

        // Filter mood posts based on distance
        filteredMoodPosts.clear();
        for (MoodPost post : originalMoodPosts) {
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    post.getLatitude(),
                    post.getLongitude(),
                    results
            );

            // Convert meters to kilometers
            float distanceInKm = results[0] / 1000;

            if (distanceInKm <= currentFilterDistance) {
                filteredMoodPosts.add(post);
            }
        }

        // Add markers for filtered posts
        for (MoodPost post : filteredMoodPosts) {
            LatLng position = new LatLng(post.getLatitude(), post.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(post.getUser())
                    .snippet(post.getEmotion().toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        // Draw circle to show filter radius
        mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(currentFilterDistance * 1000) // Convert km to meters
                .strokeColor(0x30ff0000)
                .fillColor(0x30ff0000)
                .strokeWidth(2));

        // Adjust camera to show the entire filter radius
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, getZoomLevel(currentFilterDistance * 1000)));
    }

    // Calculate appropriate zoom level based on radius
    private float getZoomLevel(int radiusInMeters) {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;
        final int screenMin = Math.min(screenWidth, screenHeight);

        double equatorLength = 40075004.0; // in meters
        double widthInPixels = screenMin;
        double metersPerPixel = equatorLength / 256;
        int zoomLevel = 1;
        while ((metersPerPixel * widthInPixels) > radiusInMeters * 2) {
            metersPerPixel /= 2;
            zoomLevel++;
        }
        return zoomLevel;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationPerm = new LocationPerm(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ViewModelMoodsFollowing viewModel = new ViewModelProvider(requireActivity())
                .get(ViewModelMoodsFollowing.class);

        viewModel.getData().observe(getViewLifecycleOwner(), posts -> {
            originalMoodPosts.clear();
            for (MoodPost post : posts) {
                if (post.getLatitude() != 0.0 && post.getLongitude() != 0.0) {
                    originalMoodPosts.add(post);
                }
            }
            loadMoodPosts();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fetchUserLocation();
    }

    private void fetchUserLocation() {
        if (!locationPerm.hasLocationPermission()) {
            locationPerm.requestLocationPermission();
            return;
        }

        locationPerm.getLastKnownLocation(location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
                }
            }
        });
    }

    private void loadMoodPosts() {
        if (mMap == null) return;
        mMap.clear();

        for (MoodPost post : originalMoodPosts) {
            LatLng position = new LatLng(post.getLatitude(), post.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(post.getUser())
                    .snippet(post.getEmotion().toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}