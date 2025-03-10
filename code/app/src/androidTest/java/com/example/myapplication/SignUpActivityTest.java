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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

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
        String androidLocalhost = "10.0.2.2";  // Correct host for emulator
        int portNumber = 8080;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        UserDetails[] users = {
                new UserDetails(EXISTING_USER, "password123"),
                new UserDetails("anotherTestUser", "password456")
        };

        // Insert each user into Firestore
        for (UserDetails user : users) {
            usersRef.document(user.getUsername()).set(user);
        }

        // Wait for Firestore to sync before running tests
        SystemClock.sleep(2000);
    }

    @Test
    public void signUpWithExistingUsernameShouldShowError() {
        // Enter existing username
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(EXISTING_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText("password123"));

        // Click sign-up button
        onView(withId(R.id.signUpButton)).perform(click());

        // Verify error message appears
        onView(withId(R.id.signUpMessageTextView)).check(matches(withText("Username already exists")));
    }

    @Test
    public void signUpWithUniqueUsernameShouldSucceed() {
        // Enter new username
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(NEW_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText("newpassword"));

        // Click sign-up button
        onView(withId(R.id.signUpButton)).perform(click());

        // Verify success message appears
        onView(withId(R.id.signUpMessageTextView)).check(matches(withText("Signed up successfully")));
    }

    @After
    public void tearDown() {
        String projectId = "lab7-b1aa8";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
