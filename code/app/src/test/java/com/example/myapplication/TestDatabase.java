package com.example.myapplication;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.futuredevs.database.Database;
import com.futuredevs.database.IAuthenticator;
import com.futuredevs.database.UserDetails;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Database tests, which will execute on the development machine (host).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestDatabase
{
    /** The IP address used to connect to the Android emulator. */
    private static final String ANDROID_HOST_IP = "10.0.2.2";
    /** Port used for Firestore connection. */
    private static final int DATABASE_PORT = 8080;
    private static final long ACTION_TIMOUT_TIME = 2000L;
    private IAuthenticator.AuthenticationResult signUpSignInResult;

    @BeforeClass
    public static void enableDatabaseEmulator() {
        FirebaseFirestore.getInstance().useEmulator(ANDROID_HOST_IP, DATABASE_PORT);
    }

    /**
     * <p>Tests signing up a new user and attempting to sign up a user when
     * a user with the same name already exists along with logging in
     * the user after signup and checking if login details are properly
     * validated.</p>
     *
     * Adapted from: https://stackoverflow.com/a/1829949<br>
     * Authored by: Martin<br>
     * Adapted by: Spencer Schmidt<br>
     * Adapted on: Mar 9, 2025
     */
    @Test
    public void testCreateUserAndSignIn() throws InterruptedException {
        UserDetails details = new UserDetails("John", "testpassword123");
        List<IAuthenticator.AuthenticationResult> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Database.getInstance().attemptSignup(details, r -> signUpSignInResult = r);
        assertTrue(latch.await(ACTION_TIMOUT_TIME, TimeUnit.MILLISECONDS));
		assertSame(signUpSignInResult, IAuthenticator.AuthenticationResult.SUCCEED);

        Database.getInstance().attemptSignup(details, r -> signUpSignInResult = r);
        latch = new CountDownLatch(1);
        assertTrue(latch.await(ACTION_TIMOUT_TIME, TimeUnit.MILLISECONDS));
        assertSame(signUpSignInResult, IAuthenticator.AuthenticationResult.USERNAME_TAKEN);

        Database.getInstance().validateLogin(details, r -> signUpSignInResult = r);
        latch = new CountDownLatch(1);
        assertTrue(latch.await(ACTION_TIMOUT_TIME, TimeUnit.MILLISECONDS));
        assertSame(signUpSignInResult, IAuthenticator.AuthenticationResult.SUCCEED);

        UserDetails invalidDetails = new UserDetails("John", "wrongpassword");
        Database.getInstance().validateLogin(invalidDetails, r -> signUpSignInResult = r);
        latch = new CountDownLatch(1);
        assertTrue(latch.await(ACTION_TIMOUT_TIME, TimeUnit.MILLISECONDS));
        assertSame(signUpSignInResult, IAuthenticator.AuthenticationResult.INVALID_DETAILS);
    }

    @After
    public void tearDown() {
        String projectId = "futuredevs-cc525";
        URL url = null;

        try {
            //"http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents"
            String projectURL = "http://%s:%d/emulator/v1/projects/%s/databases/(default)/documents";
            String filledURL = String.format(projectURL, ANDROID_HOST_IP, DATABASE_PORT, projectId);
            url = new URL(filledURL);
        }
        catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }

        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        }
        catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void threadSleep(long timeMs) {
        try { Thread.sleep(2000L); }
        catch (Exception ignored) {}
    }
}