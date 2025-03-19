package com.example.myapplication;

import static org.junit.Assert.assertTrue;

import com.futuredevs.models.items.MoodPost;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for verifying that the MoodPost.Emotion enum contains all required emotional states.
 *
 * This test ensures the enum includes at least:
 * anger, confusion, disgust, fear, happiness, sadness, shame, and surprise.
 *
 * US 01.02.01 : As a participant, I want the emotional states to include at least:
 * anger, confusion, disgust, fear, happiness, sadness, shame, and surprise.
 */
public class MoodPostEmotionTest {

    @Test
    public void testEmotionEnumContainsRequiredStates() {
        // Required emotional states as per the user story.
        Set<String> requiredEmotions = new HashSet<>(Arrays.asList(
                "ANGER", "CONFUSED", "DISGUSTED", "FEAR", "HAPPY", "SADNESS", "SHAME", "SURPRISED"));

        // Collect the names of the emotions defined in the enum.
        Set<String> actualEmotions = new HashSet<>();
        for (MoodPost.Emotion emotion : MoodPost.Emotion.values()) {
            actualEmotions.add(emotion.name());
        }

        // Verify each required emotion is present in the actual set.
        for (String required : requiredEmotions) {
            assertTrue("Missing required emotion: " + required, actualEmotions.contains(required));
        }
    }
}