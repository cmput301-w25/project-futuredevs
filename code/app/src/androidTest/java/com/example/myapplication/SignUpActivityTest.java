package com.example.myapplication;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;

import android.os.SystemClock;
import android.util.Log;

import com.futuredevs.database.UserDetails;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpActivityTest {

    @Rule
    public ActivityScenarioRule<SignUpActivity> scenario =
            new ActivityScenarioRule<>(SignUpActivity.class);

    private static final String EXISTING_USER = "testuser";
    private static final String NEW_USER = "newuser123";

    @BeforeClass
    public static void setupFirestoreEmulator() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);
        Log.i("Firestore", "Using Firestore emulator at 10.0.2.2:8080");
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        UserDetails[] users = {
                new UserDetails(EXISTING_USER, "password123"),
                new UserDetails("anotherTestUser", "password456")
        };

        for (UserDetails user : users) {
            // Use the same document naming convention as production code
            usersRef.document("user_" + user.getUsername()).set(user);
        }
        // Wait a bit to ensure data is written (could be improved with proper async handling)
        SystemClock.sleep(2000);
    }

    @Test
    public void signUpWithExistingUsernameShouldShowError() {
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(EXISTING_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText("password123"));
        onView(withId(R.id.signUpButton)).perform(click());
        onView(withId(R.id.signUpMessageTextView))
                .check(matches(withText("Username already exists")));
    }

    @Test
    public void signUpWithEmptyFieldsShouldShowError() {
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(""));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText(""));
        onView(withId(R.id.signUpButton)).perform(click());
        onView(withId(R.id.signUpMessageTextView)).check(matches(withText("Enter username & password")));
    }

    @Test
    public void signUpWithUniqueUsernameShouldSucceed() {
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(NEW_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText("newpassword"));
        onView(withId(R.id.signUpButton)).perform(click());
        onView(withId(R.id.signUpMessageTextView))
                .check(matches(withText("Signed up successfully")));
    }

    @After
    public void tearDown() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        try {
            // Await the deletion task to ensure clean state for each test run
            com.google.firebase.firestore.QuerySnapshot querySnapshot =
                    Tasks.await(usersRef.get(), 5, TimeUnit.SECONDS);
            for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                Tasks.await(usersRef.document(document.getId()).delete(), 5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            Log.e("Firestore Error", "Error deleting users: " + e.getMessage());
        }
    }
}