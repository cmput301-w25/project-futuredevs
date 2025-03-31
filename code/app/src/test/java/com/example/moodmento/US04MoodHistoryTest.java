package com.example.moodmento;

import com.futuredevs.models.items.MoodPost;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit Tests for MoodPost filtering and sorting.
 * Covers user stories US 04.01.01 - US 04.04.01.
 */
public class US04MoodHistoryTest {

    private List<MoodPost> testMoods;
    private long now;

    @Before
    public void setUp() {
        now = System.currentTimeMillis();
        testMoods = new ArrayList<>();

        // Creating sample moods for realistic testing scenarios
        MoodPost mood1 = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        mood1.setReason("Studied for final exam");
        mood1.setTimePosted(now - (1 * 24 * 60 * 60 * 1000L)); // 1 day ago
        testMoods.add(mood1);

        MoodPost mood2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood2.setReason("missed lecture");
        mood2.setTimePosted(now - (5 * 24 * 60 * 60 * 1000L)); // 5 days ago
        testMoods.add(mood2);

        MoodPost mood3 = new MoodPost("user1", MoodPost.Emotion.FEAR);
        mood3.setReason("presentation stress");
        mood3.setTimePosted(now - (10 * 24 * 60 * 60 * 1000L)); // 10 days ago
        testMoods.add(mood3);

        MoodPost mood4 = new MoodPost("user1", MoodPost.Emotion.ANGER);
        mood4.setReason("group project conflict");
        mood4.setTimePosted(now); // now
        testMoods.add(mood4);
    }

    @Test
    public void testSortMoodHistoryDescending() {
        // Tests US 04.01.01: Ensure moods are sorted by time (most recent first)
        List<MoodPost> shuffled = new ArrayList<>(testMoods);
        shuffled.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));

        // Validate descending order and that the latest mood is first
        assertTrue(shuffled.get(0).getTimePosted() >= shuffled.get(1).getTimePosted());
        assertTrue(shuffled.get(1).getTimePosted() >= shuffled.get(2).getTimePosted());
        assertTrue(shuffled.get(2).getTimePosted() >= shuffled.get(3).getTimePosted());
        assertEquals(MoodPost.Emotion.ANGER, shuffled.get(0).getEmotion());
    }

    @Test
    public void testFilterMoodsInLast7Days() {
        // Tests US 04.02.01: Filter moods that occurred in the last 7 days
        long sevenDaysAgo = now - (7L * 24 * 60 * 60 * 1000L);

        List<MoodPost> recentMoods = testMoods.stream()
                .filter(m -> m.getTimePosted() >= sevenDaysAgo)
                .collect(Collectors.toList());

        // Validate that only moods within the last week are included
        assertEquals(3, recentMoods.size());
        assertTrue(recentMoods.stream().anyMatch(m -> m.getEmotion() == MoodPost.Emotion.HAPPY));
        assertTrue(recentMoods.stream().anyMatch(m -> m.getEmotion() == MoodPost.Emotion.SADNESS));
        assertTrue(recentMoods.stream().anyMatch(m -> m.getEmotion() == MoodPost.Emotion.ANGER));
    }

    @Test
    public void testFilterMoodsOlderThan7DaysReturnsEmpty() {
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L;

        // Create a list of moods all older than 7 days
        List<MoodPost> oldMoods = new ArrayList<>();

        MoodPost mood1 = new MoodPost("user1", MoodPost.Emotion.FEAR);
        mood1.setReason("exam stress");
        mood1.setTimePosted(now - (8 * 24 * 60 * 60 * 1000L)); // 8 days ago
        oldMoods.add(mood1);

        MoodPost mood2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood2.setReason("assignment late");
        mood2.setTimePosted(now - (15 * 24 * 60 * 60 * 1000L)); // 15 days ago
        oldMoods.add(mood2);

        // Apply time filter for moods within last 7 days
        List<MoodPost> filtered = oldMoods.stream()
                .filter(m -> m.getTimePosted() >= (now - sevenDaysMillis))
                .collect(Collectors.toList());

        // Expect no moods to match — all are older than 7 days
        assertEquals(0, filtered.size());
    }


    @Test
    public void testFilterBySpecificEmotion() {
        // Tests US 04.03.01: Filter moods by a specific emotional state
        String targetEmotion = "SADNESS";

        List<MoodPost> filtered = testMoods.stream()
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase(targetEmotion))
                .collect(Collectors.toList());

        // Validate only SADNESS moods are included
        assertEquals(1, filtered.size());
        assertEquals(MoodPost.Emotion.SADNESS, filtered.get(0).getEmotion());
    }

    @Test
    public void testFilterByNonExistentEmotionReturnsEmpty() {
        // Emotion NOT present in the testMoods list
        String targetEmotion = "SURPRISED";

        List<MoodPost> filtered = testMoods.stream()
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase(targetEmotion))
                .collect(Collectors.toList());

        // Expect no moods matching "SURPRISED"
        assertEquals(0, filtered.size());
    }

    @Test
    public void testFilterByReasonKeyword() {
        // Tests US 04.04.01: Filter moods whose reason contains a given word
        String keyword = "exam";

        List<MoodPost> filtered = testMoods.stream()
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // Validate only moods containing "exam" in reason are included
        assertEquals(1, filtered.size());
        assertTrue(filtered.get(0).getReason().toLowerCase().contains("exam"));
    }

    @Test
    public void testFilterByNonMatchingKeyword() {
        // Negative test: No moods contain the keyword "vacation"
        String keyword = "vacation";

        List<MoodPost> filtered = testMoods.stream()
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // Validate no matches are found
        assertEquals(0, filtered.size());
    }

    @Test
    public void testEmptyMoodHistory() {
        // Handle empty mood history
        List<MoodPost> emptyList = new ArrayList<>();

        // Sorting an empty list should not crash
        emptyList.sort((p1, p2) -> Long.compare(p2.getTimePosted(), p1.getTimePosted()));
        assertTrue(emptyList.isEmpty());

        // Filtering an empty list should return empty result
        List<MoodPost> filtered = emptyList.stream()
                .filter(m -> m.getEmotion() == MoodPost.Emotion.HAPPY)
                .collect(Collectors.toList());
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testReasonKeywordCaseInsensitivePartialMatch() {
        // Tests case-insensitive and partial match in reason keyword filtering
        String keyword = "ExAm";  // Mixed case input

        List<MoodPost> filtered = testMoods.stream()
                .filter(m -> m.getReason() != null &&
                        m.getReason().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // Validate match regardless of case or position
        assertEquals(1, filtered.size());
        assertTrue(filtered.get(0).getReason().toLowerCase().contains("exam"));
    }

    @Test
    public void testNullReasonHandled() {
        // Ensure null reason does not crash keyword filter
        MoodPost mood = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        mood.setTimePosted(now);  // No reason set

        List<MoodPost> moods = new ArrayList<>();
        moods.add(mood);

        String keyword = "exam";
        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getReason() != null &&
                        m.getReason().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        // Validate mood with null reason is skipped without crash
        assertEquals(0, filtered.size());
    }

    @Test
    public void testFilterByTimeEmotionKeywordCombined() {
        // Real-world usage: Apply all filters (time + emotion + keyword) together
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L;

        // Setup test moods with combinations
        MoodPost mood1 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood1.setReason("midterm stress");
        mood1.setTimePosted(now - (2 * 24 * 60 * 60 * 1000L)); // Matches all filters

        MoodPost mood2 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood2.setReason("missed lecture");
        mood2.setTimePosted(now - (1 * 24 * 60 * 60 * 1000L)); // Missing keyword

        MoodPost mood3 = new MoodPost("user1", MoodPost.Emotion.ANGER);
        mood3.setReason("midterm was hard");
        mood3.setTimePosted(now - (3 * 24 * 60 * 60 * 1000L)); // Wrong emotion

        MoodPost mood4 = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood4.setReason("midterm stress");
        mood4.setTimePosted(now - (10 * 24 * 60 * 60 * 1000L)); // Too old

        List<MoodPost> moods = List.of(mood1, mood2, mood3, mood4);

        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getTimePosted() >= (now - sevenDaysMillis))
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase("SADNESS"))
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains("midterm"))
                .collect(Collectors.toList());

        // Validate only mood1 matches all filters
        assertEquals(1, filtered.size());
        assertEquals("midterm stress", filtered.get(0).getReason());
    }

    @Test
    public void testLargeMoodListFilteringPerformance() {
        // Performance: Simulate large dataset (1000 moods) and filter recent ones
        List<MoodPost> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            MoodPost mood = new MoodPost("user", MoodPost.Emotion.HAPPY);
            mood.setReason("routine day");
            mood.setTimePosted(now - (i * 60 * 60 * 1000L)); // Every hour
            largeList.add(mood);
        }

        // Filter moods from last 24 hours
        long twentyFourHours = 24L * 60 * 60 * 1000L;
        List<MoodPost> filtered = largeList.stream()
                .filter(m -> m.getTimePosted() >= (now - twentyFourHours))
                .collect(Collectors.toList());

        // Expect up to 24 moods in 24 hours
        assertTrue(filtered.size() <= 25);
    }

    @Test
    public void testCombinedFiltersKeywordMissingReturnsEmpty() {
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L;

        // Mood matches time and emotion, but NOT keyword
        MoodPost mood = new MoodPost("user1", MoodPost.Emotion.SADNESS);
        mood.setReason("missed lecture");  // Keyword "midterm" missing
        mood.setTimePosted(now - (2 * 24 * 60 * 60 * 1000L)); // 2 days ago

        List<MoodPost> moods = List.of(mood);

        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getTimePosted() >= (now - sevenDaysMillis)) // Time match
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase("SADNESS")) // Emotion match
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains("midterm")) // Keyword FAIL
                .collect(Collectors.toList());

        // Should return empty because keyword did not match
        assertEquals(0, filtered.size());
    }

    @Test
    public void testCombinedFiltersAllFailReturnsEmpty() {
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L;

        // Mood too old, wrong emotion, keyword missing
        MoodPost mood = new MoodPost("user1", MoodPost.Emotion.HAPPY);
        mood.setReason("routine day");  // Keyword "midterm" missing
        mood.setTimePosted(now - (10 * 24 * 60 * 60 * 1000L)); // 10 days ago (too old)

        List<MoodPost> moods = List.of(mood);

        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getTimePosted() >= (now - sevenDaysMillis)) // Time FAIL
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase("SADNESS")) // Emotion FAIL
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains("midterm")) // Keyword FAIL
                .collect(Collectors.toList());

        // No conditions matched — result must be empty
        assertEquals(0, filtered.size());
    }


    @Test
    public void testKeywordMatchesButTimeAndEmotionFailReturnsEmpty() {
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L;

        // Mood: keyword matches, but too old and wrong emotion
        MoodPost mood = new MoodPost("user1", MoodPost.Emotion.HAPPY);  // Emotion FAIL (expected SADNESS)
        mood.setReason("midterm stress");  // Keyword matches ✅
        mood.setTimePosted(now - (10 * 24 * 60 * 60 * 1000L));  // 10 days ago — Time FAIL

        List<MoodPost> moods = List.of(mood);

        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getTimePosted() >= (now - sevenDaysMillis)) // Time FAIL
                .filter(m -> m.getEmotion().toString().equalsIgnoreCase("SADNESS")) // Emotion FAIL
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains("midterm")) // Keyword MATCH
                .collect(Collectors.toList());

        // Time and Emotion filters failed, so result must be empty
        assertEquals(0, filtered.size());
    }




}
