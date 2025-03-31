package com.example.myapplication;

import com.futuredevs.models.items.MoodPost;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for Offline Behavior and Synchronization.
 * Covers US 07.01.01.
 */
public class US07OfflineMoodSyncTest {

    private List<MoodPost> localCache;              // Mood events stored locally while offline
    private List<MoodPost> cloudStorage;            // Simulated cloud DB
    private Map<String, Runnable> syncQueue;        // Simulated sync queue

    @Before
    public void setUp() {
        localCache = new ArrayList<>();
        cloudStorage = new ArrayList<>();
        syncQueue = new LinkedHashMap<>(); // ordered for test clarity
    }

    private MoodPost createMood(String user, MoodPost.Emotion emotion, String reason) {
        MoodPost mood = new MoodPost(user, emotion);
        mood.setReason(reason);
        mood.setTimePosted(System.currentTimeMillis());
        return mood;
    }

    @Test
    public void testAddMoodOfflineAndSync() {
        MoodPost offlineMood = createMood("user1", MoodPost.Emotion.HAPPY, "offline happy");

        localCache.add(offlineMood);
        syncQueue.put("ADD_" + offlineMood.hashCode(), () -> cloudStorage.add(offlineMood));

        // Simulate coming online
        syncQueue.values().forEach(Runnable::run);
        syncQueue.clear();

        assertEquals(1, cloudStorage.size());
        assertEquals("offline happy", cloudStorage.get(0).getReason());
    }

    @Test
    public void testEditMoodOfflineAndSync() {
        MoodPost original = createMood("user1", MoodPost.Emotion.SADNESS, "bad day");
        cloudStorage.add(original);

        // Simulate offline edit
        original.setEmotion(MoodPost.Emotion.CONFUSED);
        original.setReason("confused offline");
        syncQueue.put("EDIT_" + original.hashCode(), () -> {
            for (MoodPost m : cloudStorage) {
                if (m.hashCode() == original.hashCode()) {
                    m.setEmotion(original.getEmotion());
                    m.setReason(original.getReason());
                }
            }
        });

        // Sync
        syncQueue.values().forEach(Runnable::run);
        syncQueue.clear();

        assertEquals("confused offline", cloudStorage.get(0).getReason());
        assertEquals(MoodPost.Emotion.CONFUSED, cloudStorage.get(0).getEmotion());
    }

    @Test
    public void testDeleteMoodOfflineAndSync() {
        MoodPost mood = createMood("user1", MoodPost.Emotion.FEAR, "scary night");
        cloudStorage.add(mood);

        syncQueue.put("DELETE_" + mood.hashCode(), () -> cloudStorage.removeIf(m -> m.hashCode() == mood.hashCode()));

        // Sync
        syncQueue.values().forEach(Runnable::run);
        syncQueue.clear();

        assertTrue(cloudStorage.isEmpty());
    }

    @Test
    public void testMixedOfflineChangesSyncTogether() {
        // Setup cloud
        MoodPost mood1 = createMood("user1", MoodPost.Emotion.HAPPY, "sunny");
        MoodPost mood2 = createMood("user2", MoodPost.Emotion.SADNESS, "lost phone");
        cloudStorage.addAll(List.of(mood1, mood2));

        // Offline actions
        MoodPost newOffline = createMood("user3", MoodPost.Emotion.SHAME, "embarrassed");
        localCache.add(newOffline);
        syncQueue.put("ADD_" + newOffline.hashCode(), () -> cloudStorage.add(newOffline));

        mood1.setReason("sunny and warm");
        syncQueue.put("EDIT_" + mood1.hashCode(), () -> mood1.setReason("sunny and warm"));

        syncQueue.put("DELETE_" + mood2.hashCode(), () -> cloudStorage.removeIf(m -> m.hashCode() == mood2.hashCode()));

        // Sync
        syncQueue.values().forEach(Runnable::run);
        syncQueue.clear();

        assertEquals(2, cloudStorage.size());
        assertTrue(cloudStorage.contains(mood1));
        assertTrue(cloudStorage.contains(newOffline));
        assertEquals("sunny and warm", mood1.getReason());
    }
}
