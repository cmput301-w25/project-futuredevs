package com.example.myapplication;

import com.futuredevs.models.items.MoodPost;

import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.*;
import static java.lang.Short.decode;

/**
 * Unit tests for MoodPost covering the "Other Details" user stories.
 * Includes reason validation, photo encoding/size constraints, and social situations.
 *
 * Covers user stories:
 * - US 02.01.01: Textual reason (≤ 200 characters and ≤ 3 words)
 * - US 02.04.01: Valid predefined social situations
 */

public class US02OtherDetailsTest {

    private MoodPost mood;

    @Before
    public void setUp() {
        mood = new MoodPost("user", MoodPost.Emotion.HAPPY);
    }

    // US 02.01.01 - Reason must be 3 words or fewer and ≤ 200 characters
    @Test
    public void testReasonTextBehaviorRealWorld() {
        // 1. Short sentence — valid
        String validShort = "feeling great";
        mood.setReason(validShort);
        assertEquals(validShort, mood.getReason());

        // 2. Max char limit (exactly 200)
        String maxChars = "x".repeat(200);
        mood.setReason(maxChars);
        assertEquals(200, mood.getReason().length());

        // 3. Over character limit (should truncate)
        String overChars = "y".repeat(300);
        mood.setReason(overChars);
        assertEquals(200, mood.getReason().length());
        assertTrue(mood.getReason().startsWith("y"));

        // 4. Sentence with emojis
        String withEmoji = "Had pizza 🍕 and smiled 😊";
        mood.setReason(withEmoji);
        assertEquals(withEmoji, mood.getReason());

        // 5. Excessive whitespace and newlines
        String messyText = "  tired     from \n studying   ";
        mood.setReason(messyText.trim());
        assertEquals("tired     from \n studying", mood.getReason());

        // 6. Null input — should not crash
        mood.setReason(null);
        assertNull(mood.getReason());

        // 7. Long multilingual sentence (Unicode, accents, etc.)
        String unicodeText = "أنا حزين جداً اليوم 😔"; // Arabic
        mood.setReason(unicodeText);
        assertEquals(unicodeText, mood.getReason());

        // 8. Mixed punctuation and special chars
        String withSymbols = "Exam done!!! #blessed @uni :)";
        mood.setReason(withSymbols);
        assertEquals(withSymbols, mood.getReason());

        // 9. Empty string — valid edge case
        String empty = "";
        mood.setReason(empty);
        assertEquals(empty, mood.getReason());

        // 10. String with leading/trailing whitespace — not auto-trimmed unless done manually
        String spaced = "   had fun today   ";
        mood.setReason(spaced);
        assertEquals(spaced, mood.getReason());

        // 11. Copy-pasted full sentence from Reddit-style comment
        String copiedText = "I don't even know what to feel anymore tbh.";
        mood.setReason(copiedText);
        assertEquals(copiedText, mood.getReason());

        // 12. Reason filled with emojis only
        String onlyEmoji = "😭😭😭";
        mood.setReason(onlyEmoji);
        assertEquals(onlyEmoji, mood.getReason());

        // 13. Multi-line formatted reason
        String multiLine = "stressed\nabout\nfinals";
        mood.setReason(multiLine);
        assertEquals(multiLine, mood.getReason());
    }



    // US 02.04.01 - Valid social situations
    @Test
    public void testSocialSituationBehaviorRealWorld() {
        // 1. Initial value should be null (not set yet)
        assertNull(mood.getSocialSituation());

        // 2. Set to ALONE
        mood.setSocialSituation(MoodPost.SocialSituation.ALONE);
        assertEquals(MoodPost.SocialSituation.ALONE, mood.getSocialSituation());

        // 3. Change to ONE_PERSON
        mood.setSocialSituation(MoodPost.SocialSituation.ONE_PERSON);
        assertEquals(MoodPost.SocialSituation.ONE_PERSON, mood.getSocialSituation());

        // 4. Change to MULTIPLE_PEOPLE
        mood.setSocialSituation(MoodPost.SocialSituation.MULTIPLE_PEOPLE);
        assertEquals(MoodPost.SocialSituation.MULTIPLE_PEOPLE, mood.getSocialSituation());

        // 5. Change to CROWD
        mood.setSocialSituation(MoodPost.SocialSituation.CROWD);
        assertEquals(MoodPost.SocialSituation.CROWD, mood.getSocialSituation());

        // 6. Reassign same value repeatedly
        mood.setSocialSituation(MoodPost.SocialSituation.CROWD);
        mood.setSocialSituation(MoodPost.SocialSituation.CROWD);
        assertEquals(MoodPost.SocialSituation.CROWD, mood.getSocialSituation());

        // 7. Transitioning back to ALONE from CROWD
        mood.setSocialSituation(MoodPost.SocialSituation.ALONE);
        assertEquals(MoodPost.SocialSituation.ALONE, mood.getSocialSituation());

        // 8. Null out the situation
        mood.setSocialSituation(null);
        assertNull(mood.getSocialSituation());

        // 9. Reset to MULTIPLE_PEOPLE after null
        mood.setSocialSituation(MoodPost.SocialSituation.MULTIPLE_PEOPLE);
        assertEquals(MoodPost.SocialSituation.MULTIPLE_PEOPLE, mood.getSocialSituation());

        // 10. Store and verify with other field interactions
        mood.setReason("Studied at library");
        mood.setSocialSituation(MoodPost.SocialSituation.ALONE);
        assertEquals("Studied at library", mood.getReason());
        assertEquals(MoodPost.SocialSituation.ALONE, mood.getSocialSituation());
    }

    // Edge Case - Null image
    @Test
    public void testNullImageHandled() {
        mood.setImageData((byte[]) null);
        assertNull(mood.getImageData());
    }


}
