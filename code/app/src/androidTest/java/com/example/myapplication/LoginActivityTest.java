package com.example.myapplication;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.intent.Intents;

import com.futuredevs.database.UserDetails;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for validating the login functionality in MainActivity.
 *
 * This test seeds the Firestore emulator with a valid user, then simulates user input to:
 * <ul>
 *   <li>Test a successful login (valid credentials should launch HomeActivity).</li>
 *   <li>Test an invalid login (incorrect credentials should show an error message).</li>
 * </ul>
 *
 * Ensure that your Firestore emulator is set up and your MainActivity uses the same
 * document naming convention when validating login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    private static final String VALID_USER = "validUser";
    private static final String VALID_PASSWORD = "correctPassword";
    private static final String INVALID_PASSWORD = "wrongPassword";

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

        // Seed the database with a valid user using the same naming convention as production code.
        UserDetails user = new UserDetails(VALID_USER, VALID_PASSWORD);
        usersRef.document("user_" + VALID_USER).set(user);

        // Sleep to ensure data is written to the emulator.
        SystemClock.sleep(2000);

        // Initialize Espresso Intents to monitor launched activities.
        Intents.init();
    }

    @Test
    public void loginWithValidCredentialsShouldLaunchHomeActivity() {
        // Type the valid username and password, then click login.
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(VALID_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText(VALID_PASSWORD));
        onView(withId(R.id.signUpButton)).perform(click());

        // Check that HomeActivity is launched.
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void loginWithInvalidCredentialsShouldShowErrorMessage() {
        // Type the valid username with an invalid password.
        onView(withId(R.id.signUpUsernameEditText)).perform(typeText(VALID_USER));
        onView(withId(R.id.signUpPasswordEditText)).perform(typeText(INVALID_PASSWORD));
        onView(withId(R.id.signUpButton)).perform(click());

        // Check that the error message "Invalid username or password" is displayed.
        onView(withId(R.id.signUpMessageTextView))
                .check(matches(withText("Invalid username or password")));
    }

    @After
    public void tearDown() {
        // Clean up the Firestore database.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        try {
            com.google.firebase.firestore.QuerySnapshot querySnapshot =
                    Tasks.await(usersRef.get(), 5, TimeUnit.SECONDS);
            for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                Tasks.await(usersRef.document(document.getId()).delete(), 5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            Log.e("Firestore Error", "Error deleting users: " + e.getMessage());
        }

        // Release Espresso Intents resources.
        Intents.release();
    }
}
