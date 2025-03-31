package com.example.moodmento;

import com.futuredevs.models.items.UserProfile;
import com.futuredevs.models.items.UserSearchResult;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for Profile features.
 * Covers user stories US 03.01.01 through US 03.03.01.
 */
public class US03UserProfileTest {

    private Map<String, UserProfile> userDatabase;

    @Before
    public void setUp() {
        userDatabase = new HashMap<>();
        userDatabase.put("alice", new UserProfile("alice"));
        userDatabase.put("bob", new UserProfile("bob"));
        userDatabase.put("charlie", new UserProfile("charlie"));
    }

    // US 03.01.01: User profile should have a unique username
    @Test
    public void testUniqueUsernames() {
        Set<String> usernames = new HashSet<>();
        for (UserProfile user : userDatabase.values()) {
            assertTrue("Duplicate username found!", usernames.add(user.getUsername()));
        }
    }

    // US 03.02.01: Search for other users by name substring
    @Test
    public void testSearchUsers() {
        String query = "a"; // should match "alice" and "charlie"
        List<UserSearchResult> results = new ArrayList<>();

        for (String username : userDatabase.keySet()) {
            if (username.contains(query)) {
                boolean isFollowing = false;
                boolean isPending = false;
                results.add(new UserSearchResult(username, isPending, isFollowing));
            }
        }

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getUsername().equals("alice")));
        assertTrue(results.stream().anyMatch(r -> r.getUsername().equals("charlie")));
    }

    // US 03.03.01: View other users’ profile data
    @Test
    public void testViewOtherUserProfile() {
        UserProfile bobProfile = userDatabase.get("bob");
        assertNotNull(bobProfile);
        assertEquals("bob", bobProfile.getUsername());
        assertTrue(bobProfile.getFollowers().isEmpty());
        assertTrue(bobProfile.getFollowing().isEmpty());
        assertTrue(bobProfile.getPending().isEmpty());
    }
}
