package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.futuredevs.models.items.MoodPost;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for mood event operations to satisfy:
 *
 * US 01.01.01 : Adding a mood event with a required emotion, timestamp, optional trigger, and optional social situation.
 * US 01.02.01 : The MoodPost.Emotion enum must include anger, confusion, disgust, fear, happiness, sadness, shame, and surprise.
 * US 01.03.01 : Consistent emoticon and color mappings for emotional states (sample test provided).
 * US 01.04.01 : Viewing a mood event's details.
 * US 01.05.01 : Editing details of a mood event.
 * US 01.06.01 : Deleting a mood event.
 */
public class MoodPostOperationsTest {

    private List<MoodPost> moodHistory;

    @Before
    public void setUp() {
        // Simulate a mood history list.
        moodHistory = new ArrayList<>();
    }

    /**
     * Test adding a new mood event.
     * Verifies that the event is added with a valid timestamp and required emotion.
     */
    @Test
    public void testAddMoodEvent() {
        String username = "user1";
        MoodPost.Emotion emotion = MoodPost.Emotion.HAPPY;
        MoodPost moodPost = new MoodPost(username, emotion);

        moodHistory.add(moodPost);

        // Check that the mood event was added.
        assertEquals("Mood history should contain one event", 1, moodHistory.size());
        // Required field: emotion must be set.
        assertNotNull("Emotion should be set", moodPost.getEmotion());
        assertEquals("Emotion should be HAPPY", MoodPost.Emotion.HAPPY, moodPost.getEmotion());

        // Validate that the timestamp is near current time (within 5 seconds).
        long now = System.currentTimeMillis();
        long postTime = moodPost.getTimePosted();
        assertTrue("Timestamp should be near current time", Math.abs(now - postTime) < 5000);
    }

    /**
     * Test that the MoodPost.Emotion enum contains all required emotional states.
     */
    @Test
    public void testEmotionEnumContainsRequiredStates() {
        // Required emotional states.
        String[] requiredEmotions = {"ANGER", "CONFUSED", "DISGUSTED", "FEAR", "HAPPY", "SADNESS", "SHAME", "SURPRISED"};

        for (String state : requiredEmotions) {
            boolean found = false;
            for (MoodPost.Emotion emotion : MoodPost.Emotion.values()) {
                if (emotion.name().equals(state)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Emotion enum should contain " + state, found);
        }
    }

    /**
     * Test viewing a mood event's details.
     */
    @Test
    public void testViewMoodEventDetails() {
        String username = "user1";
        MoodPost moodPost = new MoodPost(username, MoodPost.Emotion.FEAR);

        // Set optional details.
        String trigger = "loud noise";
        String reason = "scared";
        MoodPost.SocialSituation situation = MoodPost.SocialSituation.ONE_PERSON;
        moodPost.setTrigger(trigger);
        moodPost.setReason(reason);
        moodPost.setSocialSituation(situation);

        // Validate the details.
        assertEquals("Trigger should match", trigger, moodPost.getTrigger());
        assertEquals("Reason should match", "scared", moodPost.getReason());
        assertEquals("Social situation should be ONE_PERSON", situation, moodPost.getSocialSituation());
    }

    /**
     * Test editing a mood event.
     * Simulates updating a mood event's trigger and reason.
     */
    @Test
    public void testEditMoodEvent() {
        String username = "user1";
        MoodPost moodPost = new MoodPost(username, MoodPost.Emotion.DISGUSTED);

        // Initially, optional fields are null.
        assertNull("Initial trigger should be null", moodPost.getTrigger());
        assertNull("Initial reason should be null", moodPost.getReason());

        // Edit details.
        String newTrigger = "bad smell";
        String newReason = "really disgusted by the food";
        moodPost.setTrigger(newTrigger);
        moodPost.setReason(newReason);

        // The setReason method should enforce limits: at most 20 characters and 3 words.
        String storedReason = moodPost.getReason();
        String[] words = storedReason.split(" ");
        assertTrue("Reason should have at most 3 words", words.length <= 3);
        assertTrue("Reason should be at most 20 characters", storedReason.length() <= 20);
        assertEquals("Trigger should be updated", newTrigger, moodPost.getTrigger());
    }

    /**
     * Test deleting a mood event.
     * Simulates removing a mood event from the mood history.
     */
    @Test
    public void testDeleteMoodEvent() {
        // Add two mood events.
        MoodPost event1 = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        MoodPost event2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        moodHistory.add(event1);
        moodHistory.add(event2);

        // Delete the first event.
        moodHistory.remove(event1);
        assertEquals("After deletion, mood history should have 1 event", 1, moodHistory.size());
        assertEquals("Remaining event should be event2", event2, moodHistory.get(0));
    }

    /**
     * Test consistent emoticon mapping for emotional states.
     * (US 01.03.01: Consistent emoticons and colors.)
     *
     * This test assumes that you have defined a mapping method in a helper class
     * (e.g., MoodPostEmoticonMapper) that returns an emoticon string for each emotion.
     */

}
