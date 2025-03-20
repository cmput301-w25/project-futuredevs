package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.futuredevs.models.items.MoodPost;

import org.junit.Test;

/**
 * Unit tests for the MoodPost class to verify:
 * - The brief textual reason is correctly truncated to 20 characters or 3 words (US 02.01.01).
 * - The social situation is set and retrieved correctly (US 02.04.01).
 */
public class MoodPostTest {

    @Test
    public void testSetReasonTruncatesTo20CharsAnd3Words() {
        // Create a MoodPost instance for testing.
        MoodPost moodPost = new MoodPost("dummyUser", MoodPost.Emotion.HAPPY);

        // This long reason exceeds 20 characters and more than 3 words.
        String longReason = "This explanation is definitely far too long for our needs";
        moodPost.setReason(longReason);

        String truncatedReason = moodPost.getReason();

        // Verify the truncated reason is at most 20 characters.
        assertTrue("Reason should be at most 20 characters", truncatedReason.length() <= 20);

        // Verify that there are at most 3 words.
        String[] words = truncatedReason.split(" ");
        assertTrue("Reason should have at most 3 words", words.length <= 3);
    }

    @Test
    public void testSetSocialSituation() {
        // Create a MoodPost instance.
        MoodPost moodPost = new MoodPost("dummyUser", MoodPost.Emotion.HAPPY);

        // Set the social situation to ONE_PERSON.
        moodPost.setSocialSituation(MoodPost.SocialSituation.ONE_PERSON);

        // Verify that the social situation is correctly stored.
        assertEquals("Social situation should be ONE_PERSON", MoodPost.SocialSituation.ONE_PERSON, moodPost.getSocialSituation());
    }
}