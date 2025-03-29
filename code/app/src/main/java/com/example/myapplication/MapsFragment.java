package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.models.ViewModelMoods;
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
    private LatLng userLocation = new LatLng(53.5461, -113.4938);
    private static final float DEFAULT_ZOOM = 12f;
    private final List<MoodPost> filteredMoodPosts = new ArrayList<>();
    private final List<MoodPost> originalMoodPosts = new ArrayList<>();
    private final List<MoodPost> personalMoodPosts = new ArrayList();
    private final List<MoodPost> followingMoodPosts = new ArrayList();
    private LocationPerm locationPerm;
    private String currentUserId;

    // Filter components
    private String currentMoodFilter = "ALL";
    private Spinner moodSpinner;
    private SeekBar distanceSeekBar;
    private TextView distanceTextView;
    private TextView textViewShowingPosts;
    private Button applyFilterButton;
    private int currentFilterDistance = 10;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_mood_event_layout, container, false);

        // Initialize UI components
        distanceSeekBar = view.findViewById(R.id.rangeSeekBar);
        applyFilterButton = view.findViewById(R.id.filterButton);
        distanceTextView = view.findViewById(R.id.textViewFilterRadius);
        textViewShowingPosts = view.findViewById(R.id.textViewShowingPosts);
        moodSpinner = view.findViewById(R.id.mood_filter_spinner);

        setupMoodSpinner();

        distanceSeekBar.setMax(10);
        distanceSeekBar.setProgress(currentFilterDistance);
        updateDistanceText(currentFilterDistance);
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentFilterDistance = progress + 1;
                updateDistanceText(currentFilterDistance);
                applyAllFilters();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set click listener for filter button to open FilterActivity
        applyFilterButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FilterActivity.class);
            // Pass current filter values if needed
            intent.putExtra("CURRENT_MOOD", currentMoodFilter);
            startActivityForResult(intent, 1);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            // Handle the filter results from FilterActivity
            String moodFilter = data.getStringExtra("FILTER_MOOD");
            String timeFilter = data.getStringExtra("FILTER_TIME");
            String wordFilter = data.getStringExtra("FILTER_WORD");

            // Update filters based on activity result
            updateFiltersFromActivity(moodFilter, timeFilter, wordFilter);
        }
    }

    private void updateFiltersFromActivity(String moodFilter, String timeFilter, String wordFilter) {
        // Update current mood filter and spinner
        currentMoodFilter = moodFilter;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) moodSpinner.getAdapter();
        int position = adapter.getPosition(moodFilter);
        if (position >= 0) {
            moodSpinner.setSelection(position);
        }

        // You can add handling for time and word filters here if needed

        // Re-apply all filters
        applyAllFilters();
    }

    private void setupMoodSpinner() {
        String[] moods = {"ALL", "HAPPY", "SADNESS", "ANGER", "CONFUSED",
                "FEAR", "SURPRISED", "SHAME", "DISGUSTED"};

        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                moods
        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);

        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMoodFilter = parent.getItemAtPosition(position).toString();
                applyAllFilters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDistanceText(int distance) {
        distanceTextView.setText(String.format("Filter Radius: %d km", distance));
    }

    private void applyAllFilters() {
        if (mMap == null || userLocation == null) return;

        mMap.clear();
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
            float distanceInKm = results[0] / 1000;

            boolean moodMatches = currentMoodFilter.equals("ALL") ||
                    post.getEmotion().toString().equals(currentMoodFilter);

            if (distanceInKm <= currentFilterDistance && moodMatches) {
                filteredMoodPosts.add(post);
            }
        }

        for (MoodPost post : filteredMoodPosts) {
            LatLng position = new LatLng(post.getLatitude(), post.getLongitude());
            boolean isPersonalPost = post.getUser().equals(currentUserId);

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(isPersonalPost ? "You" : post.getUser())
                    .snippet(post.getEmotion().toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            isPersonalPost ? BitmapDescriptorFactory.HUE_CYAN :
                                    getMoodColor(post.getEmotion().toString())
                    )));
        }

        mMap.addCircle(new CircleOptions()
                .center(userLocation)
                .radius(currentFilterDistance * 1000)
                .strokeColor(0x30ff0000)
                .fillColor(0x30ff0000)
                .strokeWidth(2));

        textViewShowingPosts.setText(String.format(
                "Showing %s posts within %d km",
                currentMoodFilter.equals("ALL") ? "all" : currentMoodFilter.toLowerCase(),
                currentFilterDistance
        ));
    }

    private float getMoodColor(String mood) {
        switch(mood) {
            case "HAPPY": return BitmapDescriptorFactory.HUE_GREEN;
            case "SADNESS": return BitmapDescriptorFactory.HUE_BLUE;
            case "ANGER": return BitmapDescriptorFactory.HUE_RED;
            case "CONFUSED": return BitmapDescriptorFactory.HUE_ORANGE;
            case "FEAR": return BitmapDescriptorFactory.HUE_VIOLET;
            case "SURPRISED": return BitmapDescriptorFactory.HUE_YELLOW;
            case "SHAME": return BitmapDescriptorFactory.HUE_ROSE;
            case "DISGUSTED": return BitmapDescriptorFactory.HUE_MAGENTA;
            default: return BitmapDescriptorFactory.HUE_AZURE;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationPerm = new LocationPerm(requireContext());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Get current user ID from Database
        currentUserId = Database.getInstance().getCurrentUser();

        // Personal moods
        ViewModelMoods viewModelMoods = new ViewModelProvider(requireActivity())
                .get(ViewModelMoods.class);

        viewModelMoods.getData().observe(getViewLifecycleOwner(), moods -> {
            personalMoodPosts.clear();
            for (MoodPost post : moods) {
                if (post.hasValidLocation()) {
                    personalMoodPosts.add(post);
                }
            }
            combineAndFilterPosts();
        });

        // Followed users' moods
        ViewModelMoodsFollowing viewModelFollowing = new ViewModelProvider(requireActivity())
                .get(ViewModelMoodsFollowing.class);

        viewModelFollowing.getData().observe(getViewLifecycleOwner(), posts -> {
            followingMoodPosts.clear();
            for (MoodPost post : posts) {
                if (post.hasValidLocation()) {
                    followingMoodPosts.add(post);
                }
            }
            combineAndFilterPosts();
        });
    }

    private void combineAndFilterPosts() {
        originalMoodPosts.clear();
        originalMoodPosts.addAll(personalMoodPosts);
        originalMoodPosts.addAll(followingMoodPosts);
        applyAllFilters();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
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
}