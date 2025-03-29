package com.example.myapplication;

import com.futuredevs.models.items.MoodPost;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for deleting MoodPost objects from a mood list.
 * Simulates realistic user operations from a mobile mood-tracking app.
 */
public class DeleteMoodTest {

    private List<MoodPost> moodList;
    private MoodPost mood1, mood2, mood3;

    @Before
    public void setUp() {
        // Set up a list of moods as would be shown in a mood history
        moodList = new ArrayList<>();

        mood1 = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        mood1.setReason("walked in the sun");

        mood2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood2.setReason("missed a deadline");

        mood3 = new MoodPost("user1", MoodPost.Emotion.FEAR);
        mood3.setReason("presentation stress");

        moodList.add(mood1);
        moodList.add(mood2);
        moodList.add(mood3);
    }

    @Test
    public void testDeleteExistingMood() {
        // Delete an existing mood
        boolean removed = moodList.remove(mood2);

        // Assertions
        assertTrue("Mood2 should be removed", removed);
        assertEquals("List should contain 2 moods after deletion", 2, moodList.size());
        assertFalse("Mood2 should not exist in the list", moodList.contains(mood2));
    }

    @Test
    public void testDeleteNonExistingMood() {
        // Try deleting a mood that isn't in the list
        MoodPost newMood = new MoodPost("user2", MoodPost.Emotion.ANGER);
        newMood.setReason("argument with teammate");

        boolean removed = moodList.remove(newMood);

        assertFalse("Non-existing mood should not be removed", removed);
        assertEquals("List size should remain unchanged", 3, moodList.size());
    }

    @Test
    public void testDeleteNullMood() {
        // Try deleting a null reference
        boolean removed = moodList.remove(null);

        assertFalse("Null mood cannot be removed", removed);
        assertEquals("List should remain unchanged", 3, moodList.size());
    }

    @Test
    public void testDeleteFirstMood() {
        // Simulate deleting the most recent mood (e.g., top of list in UI)
        MoodPost first = moodList.get(0);
        moodList.remove(first);

        assertEquals(2, moodList.size());
        assertFalse(moodList.contains(first));
    }

    @Test
    public void testDeleteLastMood() {
        // Simulate deleting the oldest mood (e.g., bottom of list in UI)
        MoodPost last = moodList.get(moodList.size() - 1);
        moodList.remove(last);

        assertEquals(2, moodList.size());
        assertFalse(moodList.contains(last));
    }

    @Test
    public void testDeleteAllMoodsOneByOne() {
        // Delete moods one by one
        assertTrue(moodList.remove(mood1));
        assertTrue(moodList.remove(mood2));
        assertTrue(moodList.remove(mood3));

        assertEquals("All moods should be deleted", 0, moodList.size());
    }

    @Test
    public void testDeleteByLoopSimulatingUserSwipe() {
        // Simulate user deleting all moods from a list (e.g., swipe-to-delete in RecyclerView)
        List<MoodPost> toRemove = new ArrayList<>(moodList); // Copy to avoid mutation during iteration
        for (MoodPost mood : toRemove) {
            moodList.remove(mood);
        }

        assertTrue(moodList.isEmpty());
    }

    @Test
    public void testMoodListAfterMultipleDeletes() {
        // Delete two out of three moods
        moodList.remove(mood1);
        moodList.remove(mood3);

        // Assertions
        assertEquals(1, moodList.size());
        assertTrue(moodList.contains(mood2));
        assertFalse(moodList.contains(mood1));
        assertFalse(moodList.contains(mood3));
    }

    @Test
    public void testDuplicateMoodDeleteOnce() {
        // Add a duplicate of mood2 (same content but different object)
        MoodPost duplicateMood2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        duplicateMood2.setReason("missed a deadline");
        moodList.add(duplicateMood2);

        // Now list has 4 moods, 2 of them "equal"
        boolean removed = moodList.remove(duplicateMood2); // remove the duplicate

        assertTrue(removed);
        assertEquals(3, moodList.size());
        assertTrue("Original mood2 should still be in the list", moodList.contains(mood2));
    }

    @Test
    public void testRemoveFromEmptyList() {
        // Clear the list first
        moodList.clear();

        MoodPost mood = new MoodPost("user1", MoodPost.Emotion.SURPRISED);
        mood.setReason("unexpected gift");

        boolean removed = moodList.remove(mood);

        assertFalse("Removing from an empty list should return false", removed);
        assertTrue("List should still be empty", moodList.isEmpty());
    }
}
