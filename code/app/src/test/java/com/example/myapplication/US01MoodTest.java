package com.example.myapplication;

import com.futuredevs.models.items.MoodPost;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit Tests for Moods.
 * Covers user stories US 01.01.01 - US 01.07.01.
 */
public class US01MoodTest {

    private MoodPost mood;
    private Date now;

    @Before
    public void setUp() {
        now = new Date();
        mood = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        mood.setTimePosted(now.getTime());
    }

    // US 01.01.01 - Add mood with all supported fields
    @Test
    public void testAddMoodEventWithAllFields() {
        mood.setReason("Finished assignment");
        mood.setSocialSituation(MoodPost.SocialSituation.CROWD);
        mood.setPrivateStatus(true);

        assertEquals("user1", mood.getUser());
        assertEquals(MoodPost.Emotion.HAPPY, mood.getEmotion());
        assertEquals("Finished assignment", mood.getReason());
        assertEquals(MoodPost.SocialSituation.CROWD, mood.getSocialSituation());
        assertEquals(now.getTime(), mood.getTimePosted());
        assertTrue(mood.isPrivate());
    }

    // US 01.01.01 - Add mood with required fields only
    @Test
    public void testAddMoodWithOnlyRequiredFields() {
        mood = new MoodPost("user2", MoodPost.Emotion.SADNESS);
        mood.setTimePosted(System.currentTimeMillis());

        assertEquals("user2", mood.getUser());
        assertEquals(MoodPost.Emotion.SADNESS, mood.getEmotion());
        assertNull(mood.getReason());
        assertNull(mood.getSocialSituation());
    }

    // US 01.02.01 - Validate all supported emotional states
    @Test
    public void testAllValidEmotionalStates() {
        List<MoodPost.Emotion> expected = Arrays.asList(
                MoodPost.Emotion.ANGER, MoodPost.Emotion.CONFUSED,
                MoodPost.Emotion.DISGUSTED, MoodPost.Emotion.FEAR,
                MoodPost.Emotion.HAPPY, MoodPost.Emotion.SADNESS,
                MoodPost.Emotion.SHAME, MoodPost.Emotion.SURPRISED
        );

        for (MoodPost.Emotion emotion : expected) {
            mood.setEmotion(emotion);
            assertEquals(emotion, mood.getEmotion());
        }
    }

    // US 01.03.01 - Validate emoji and color consistency
    @Test
    public void testEmotionEmojiAndColorMapping() {
        Map<MoodPost.Emotion, String> expectedEmojis = Map.of(
                MoodPost.Emotion.ANGER, "😡",
                MoodPost.Emotion.CONFUSED, "😕",
                MoodPost.Emotion.DISGUSTED, "🤢",
                MoodPost.Emotion.FEAR, "😨",
                MoodPost.Emotion.HAPPY, "😊",
                MoodPost.Emotion.SADNESS, "😭",
                MoodPost.Emotion.SHAME, "😳",
                MoodPost.Emotion.SURPRISED, "😮"
        );

        Map<MoodPost.Emotion, String> expectedColors = Map.of(
                MoodPost.Emotion.ANGER, "🔴",
                MoodPost.Emotion.CONFUSED, "🟠",
                MoodPost.Emotion.DISGUSTED, "🟢",
                MoodPost.Emotion.FEAR, "⚫",
                MoodPost.Emotion.HAPPY, "🟡",
                MoodPost.Emotion.SADNESS, "🔵",
                MoodPost.Emotion.SHAME, "⚪️",
                MoodPost.Emotion.SURPRISED, "🟣"
        );

        for (MoodPost.Emotion emotion : MoodPost.Emotion.values()) {
            assertEquals(expectedEmojis.get(emotion), emotion.getEmoji());
            assertEquals(expectedColors.get(emotion), emotion.getColour());
        }
    }

    // US 01.04.01 - View all mood details
    @Test
    public void testViewAllMoodDetailsDirectly() {
        MoodPost mood = new MoodPost("tester", MoodPost.Emotion.SHAME);
        long timestamp = System.currentTimeMillis();
        mood.setTimePosted(timestamp);
        mood.setReason("missed deadline");
        mood.setSocialSituation(MoodPost.SocialSituation.ALONE);
        mood.setPrivateStatus(true);
        mood.setImageData("fakeBase64ImageData");
        mood.setLocation(53.5461, -113.4938); // Edmonton coords
        mood.setNumTopLevelComments(3);
        mood.setEdited(true);

        // Validate all details via getters
        assertEquals("tester", mood.getUser());
        assertEquals(MoodPost.Emotion.SHAME, mood.getEmotion());
        assertEquals("missed deadline", mood.getReason());
        assertEquals(MoodPost.SocialSituation.ALONE, mood.getSocialSituation());
        assertEquals(timestamp, mood.getTimePosted(),5);
        assertTrue(mood.isPrivate());
        assertEquals("fakeBase64ImageData", mood.getImageData());
        assertEquals(53.5461, mood.getLatitude(), 0.0001);
        assertEquals(-113.4938, mood.getLongitude(), 0.0001);
        assertEquals(3, mood.getNumTopLevelComments());
        assertTrue(mood.hasBeenEdited());
    }


    // US 01.05.01 - Edit mood details
    @Test
    public void testEditMoodDetails() {
        mood.setEmotion(MoodPost.Emotion.FEAR);
        mood.setReason("presentation stress");
        mood.setSocialSituation(MoodPost.SocialSituation.ALONE);

        assertEquals(MoodPost.Emotion.FEAR, mood.getEmotion());
        assertEquals("presentation stress", mood.getReason());
        assertEquals(MoodPost.SocialSituation.ALONE, mood.getSocialSituation());

        // Edit fields again
        mood.setEmotion(MoodPost.Emotion.SURPRISED);
        mood.setReason("won a prize");
        mood.setSocialSituation(MoodPost.SocialSituation.MULTIPLE_PEOPLE);

        assertEquals(MoodPost.Emotion.SURPRISED, mood.getEmotion());
        assertEquals("won a prize", mood.getReason());
        assertEquals(MoodPost.SocialSituation.MULTIPLE_PEOPLE, mood.getSocialSituation());
    }

    // US 01.06.01 - Simulate deleting mood from a list
    @Test
    public void testDeleteMoodEventFromList() {
        List<MoodPost> moodHistory = new ArrayList<>();
        moodHistory.add(mood);

        assertEquals(1, moodHistory.size());
        moodHistory.remove(mood);
        assertEquals(0, moodHistory.size());
    }

    // US 01.07.01 - Toggle public/private visibility
    @Test
    public void testPublicPrivateToggle() {
        mood.setPrivateStatus(true);
        assertTrue(mood.isPrivate());

        mood.setPrivateStatus(false);
        assertFalse(mood.isPrivate());
    }

    // Edge Case - Null reason/situation handling
    @Test
    public void testNullFieldsHandled() {
        mood.setReason(null);
        mood.setSocialSituation(null);

        assertNull(mood.getReason());
        assertNull(mood.getSocialSituation());
    }
}
