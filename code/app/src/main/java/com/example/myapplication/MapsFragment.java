package com.example.myapplication;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.ViewModelMoodsFollowing;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng userLocation = new LatLng(53.5461, -113.4938); // Default to Edmonton
    private static final float DEFAULT_ZOOM = 12f;
    private final List<MoodPost> moodList = new ArrayList<>();
    private final List<MoodPost> followingList = new ArrayList<>();
    private final List<Marker> moodMarkers = new ArrayList<>();
    private LocationPerm locationPerm;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationPerm = new LocationPerm(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchUserLocation(); // Get user location before loading posts

        ViewModelMoods viewModelMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoods.class);
        viewModelMoods.getData().observe(getViewLifecycleOwner(), moods -> {
            moodList.clear();
            moodList.addAll(moods);
            loadMoodPosts();
        });

        ViewModelMoodsFollowing viewModelFollowingMoods = new ViewModelProvider(requireActivity()).get(ViewModelMoodsFollowing.class);
        viewModelFollowingMoods.getData().observe(getViewLifecycleOwner(), posts -> {
            followingList.clear();
            followingList.addAll(posts);
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
        moodMarkers.clear();

        List<MoodPost> allMoodPosts = new ArrayList<>();
        allMoodPosts.addAll(moodList);
        allMoodPosts.addAll(followingList);

        for (MoodPost post : allMoodPosts) {
            // Check if the post has valid latitude and longitude values (not 0.0)
            if (post.getLatitude() != 0.0 && post.getLongitude() != 0.0) {
                LatLng position = new LatLng(post.getLatitude(), post.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(post.getUser())
                        .snippet(post.getEmotion().toString()));

                if (marker != null) {
                    moodMarkers.add(marker);
                }
            }
        }
    }

}
