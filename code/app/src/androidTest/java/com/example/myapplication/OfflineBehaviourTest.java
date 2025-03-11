package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.MoodPost.Emotion;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulated tests for offline behavior.
 * These tests assume that when offline, mood events are queued rather than sent immediately.
 */
public class OfflineBehaviourTest {

    // Simulated offline queue.
    private List<MoodPost> offlineQueue;

    @Before
    public void setUp() {
        offlineQueue = new ArrayList<>();
    }

    @Test
    public void testQueueMoodEventOffline() {
        MoodPost event = new MoodPost("user1", Emotion.HAPPY);
        // Simulate being offline by adding the event to the offline queue.
        offlineQueue.add(event);
        assertEquals("Offline queue should have one event", 1, offlineQueue.size());
    }

    @Test
    public void testSyncOfflineMoodEvent() {
        MoodPost event = new MoodPost("user1", Emotion.SADNESS);
        offlineQueue.add(event);
        // Simulate syncing: copy all events then clear the queue.
        List<MoodPost> eventsToSync = new ArrayList<>(offlineQueue);
        offlineQueue.clear();

        assertTrue("Offline queue should be empty after sync", offlineQueue.isEmpty());
        assertEquals("Synced events should contain one event", 1, eventsToSync.size());
    }
}
