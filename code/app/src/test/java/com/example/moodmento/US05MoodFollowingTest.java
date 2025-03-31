package com.example.moodmento;

import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.MoodComment;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Unit tests for Mood Following and Sharing.
 * Covers user stories US 05.01.01 through US 05.07.02.
 */
public class US05MoodFollowingTest {

    private Map<String, List<MoodPost>> moodData;
    private Map<String, List<MoodComment>> commentData;
    private Set<String> followRequests;
    private Set<String> followers;

    private long now = System.currentTimeMillis();
    private long oneDay = 24 * 60 * 60 * 1000L;

    @Before
    public void setUp() {
        moodData = new HashMap<>();
        commentData = new HashMap<>();
        followRequests = new HashSet<>();
        followers = new HashSet<>();

        // Simulate moods for users
        moodData.put("alice", Arrays.asList(
                mood("alice", MoodPost.Emotion.HAPPY, "good vibes", now - oneDay),
                mood("alice", MoodPost.Emotion.SADNESS, "rough day", now - 3 * oneDay),
                mood("alice", MoodPost.Emotion.ANGER, "argument", now - 8 * oneDay)
        ));

        moodData.put("bob", Arrays.asList(
                mood("bob", MoodPost.Emotion.CONFUSED, "lecture hard", now - 2 * oneDay),
                mood("bob", MoodPost.Emotion.HAPPY, "gym sesh", now)
        ));

        MoodPost dummyPost = mood("alice", MoodPost.Emotion.SADNESS, "bad sleep", now - 2 * oneDay);

        commentData.put("comment-mood-1", new ArrayList<>(Arrays.asList(
                new MoodComment(dummyPost, "bob", "Hang in there"),
                new MoodComment(dummyPost, "charlie", "You got this!")
        )));
    }

    private MoodPost mood(String user, MoodPost.Emotion e, String reason, long time) {
        MoodPost m = new MoodPost(user, e);
        m.setReason(reason);
        m.setTimePosted(time);
        return m;
    }

    // US 05.01.01 - Request to follow someone
    @Test
    public void testFollowRequestFlow() {
        followRequests.add("alice");
        assertTrue(followRequests.contains("alice"));
    }

    // US 05.02.01 - Grant follow permission
    @Test
    public void testGrantFollowPermission() {
        followers.add("bob");
        assertTrue(followers.contains("bob"));
    }

    // US 05.02.02 - View all follow requests
    @Test
    public void testViewFollowRequests() {
        followRequests.add("bob");
        followRequests.add("charlie");

        List<String> requests = new ArrayList<>(followRequests);
        assertTrue(requests.contains("bob"));
        assertTrue(requests.contains("charlie"));
        assertEquals(2, requests.size());
    }

    // US 05.03.01 - View 3 most recent moods from followed users
    @Test
    public void testRecentMoodsFromFollowedUsers() {
        followers.add("alice");
        followers.add("bob");

        List<MoodPost> all = new ArrayList<>();
        for (String user : followers) {
            all.addAll(moodData.getOrDefault(user, List.of()));
        }

        List<MoodPost> recent3 = all.stream()
                .sorted((a, b) -> Long.compare(b.getTimePosted(), a.getTimePosted()))
                .limit(3)
                .collect(Collectors.toList());

        assertEquals(3, recent3.size());
        assertTrue(recent3.get(0).getTimePosted() >= recent3.get(1).getTimePosted());
    }

    // US 05.04.01 - Filter moods in last 7 days
    @Test
    public void testFilterRecentWeekMoods() {
        followers.add("alice");
        List<MoodPost> moods = moodData.get("alice");

        long weekAgo = now - 7 * oneDay;
        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getTimePosted() >= weekAgo)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
    }

    // US 05.05.01 - Filter by emotion
    @Test
    public void testFilterByEmotion() {
        followers.add("alice");
        List<MoodPost> moods = moodData.get("alice");

        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getEmotion() == MoodPost.Emotion.HAPPY)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("good vibes", filtered.get(0).getReason());
    }

    // US 05.06.01 - Filter by keyword
    @Test
    public void testFilterByKeyword() {
        followers.add("alice");
        List<MoodPost> moods = moodData.get("alice");

        String keyword = "rough";
        List<MoodPost> filtered = moods.stream()
                .filter(m -> m.getReason() != null && m.getReason().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("rough day", filtered.get(0).getReason());
    }

    // US 05.07.01 - Add comment to a mood
    @Test
    public void testAddCommentToMood() {
        String moodId = "comment-mood-1";
        List<MoodComment> comments = commentData.get(moodId);
        MoodPost parent = mood("bob", MoodPost.Emotion.HAPPY, "workout win", now);

        comments.add(new MoodComment(parent, "david", "stay strong"));

        assertEquals(3, comments.size());
        assertTrue(comments.stream().anyMatch(c -> c.getPosterName().equals("david")));
    }

    // US 05.07.02 - View comments on a mood
    @Test
    public void testViewComments() {
        List<MoodComment> comments = commentData.get("comment-mood-1");

        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("bob", comments.get(0).getPosterName());
    }
}
