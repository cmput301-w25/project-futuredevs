package com.example.myapplication;

import com.futuredevs.models.items.MoodPost;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit tests for Geolocation and Maps features.
 * Covers user stories US 06.01.01 through US 06.04.01.
 */
public class US06MoodMapTest {

    private List<MoodPost> moodHistory;
    private List<MoodPost> moodFollowing;
    private double currentLat = 53.5461;
    private double currentLon = -113.4938;

    @Before
    public void setUp() {
        moodHistory = new ArrayList<>();
        moodFollowing = new ArrayList<>();

        // History mood events (simulate mood history for one user)
        moodHistory.add(createMood("user1", MoodPost.Emotion.HAPPY, 53.5461, -113.4938)); // Edmonton (valid)
        moodHistory.add(createMood("user1", MoodPost.Emotion.SADNESS, 0.0, 0.0)); // Invalid location
        moodHistory.add(createMood("user1", MoodPost.Emotion.ANGER, 53.55, -113.5)); // Nearby

        // Following mood events
        moodFollowing.add(createMood("follower1", MoodPost.Emotion.FEAR, 53.5462, -113.4939)); // Nearby
        moodFollowing.add(createMood("follower2", MoodPost.Emotion.SURPRISED, 54.0, -113.0)); // Far
        moodFollowing.add(createMood("follower3", MoodPost.Emotion.CONFUSED, 53.5440, -113.4900)); // Nearby
    }

    private MoodPost createMood(String user, MoodPost.Emotion emotion, double lat, double lon) {
        MoodPost mood = new MoodPost(user, emotion);
        mood.setTimePosted(System.currentTimeMillis());
        mood.setLocation(lat, lon);  // Uses custom method (not Android)
        return mood;
    }

    // US 06.01.01 - Attach current location to mood
    @Test
    public void testAttachLocationToMood() {
        MoodPost mood = new MoodPost("userX", MoodPost.Emotion.HAPPY);
        mood.setLocation(currentLat, currentLon);
        assertTrue(mood.hasValidLocation());
        assertEquals(currentLat, mood.getLatitude(), 0.0001);
        assertEquals(currentLon, mood.getLongitude(), 0.0001);
    }

    // US 06.02.01 - Map of filtered mood history with valid locations
    @Test
    public void testMapFilteredMoodHistoryWithLocation() {
        List<MoodPost> valid = moodHistory.stream()
                .filter(MoodPost::hasValidLocation)
                .collect(Collectors.toList());

        assertEquals(2, valid.size());  // Should ignore the invalid one
        assertTrue(valid.stream().allMatch(MoodPost::hasValidLocation));
    }

    // US 06.03.01 - Map of followed moods showing emotion and user
    @Test
    public void testMapFollowedMoodsWithLocationAndUser() {
        List<MoodPost> filtered = moodFollowing.stream()
                .filter(MoodPost::hasValidLocation)
                .collect(Collectors.toList());

        assertEquals(3, filtered.size());

        for (MoodPost m : filtered) {
            assertNotNull(m.getUser());
            assertNotNull(m.getEmotion());
            assertTrue(m.hasValidLocation());
        }
    }

    // US 06.04.01 - Most recent mood within 5km of user location
    @Test
    public void testRecentNearbyMoodWithin5Km() {
        Map<String, MoodPost> latestMoods = new HashMap<>();

        // Get the most recent post for each followed user
        for (MoodPost mood : moodFollowing) {
            String user = mood.getUser();
            if (!latestMoods.containsKey(user) ||
                    mood.getTimePosted() > latestMoods.get(user).getTimePosted()) {
                latestMoods.put(user, mood);
            }
        }

        List<MoodPost> nearby = latestMoods.values().stream()
                .filter(MoodPost::hasValidLocation)
                .filter(m -> distanceKm(currentLat, currentLon, m.getLatitude(), m.getLongitude()) <= 5.0)
                .collect(Collectors.toList());

        for (MoodPost m : nearby) {
            double dist = distanceKm(currentLat, currentLon, m.getLatitude(), m.getLongitude());
            assertTrue(dist <= 5.0);
        }
    }

    // Helper: Compute distance between two lat/lon points (Haversine)
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) *
                        Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
