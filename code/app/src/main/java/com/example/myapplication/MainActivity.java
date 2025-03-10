package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * <p><strong>MainActivity</strong> serves as the entry point for users who want to log in to
 * the app. It checks their credentials against Firestore via the {@link Database} class, and,
 * if valid, navigates them to the {@link HomeActivity} where they can access all core features.
 * </p>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Collect and validate the user's login credentials (username and password).</li>
 *   <li>Handle any login errors or success messages (e.g., invalid password, user not found).</li>
 *   <li>Request location permissions (if not already granted) so that users can optionally
 *       attach their current location to mood events (related to US 06.01.01).</li>
 *   <li>Store the logged-in user’s username in {@code Database} to identify the current user
 *       throughout the app.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Although there is no explicit user story strictly for "logging in," this activity underpins
 * the requirement for a user to have a unique profile (US 03.01.01) by ensuring only valid,
 * registered users can proceed.
 * </p>
 *
 * @author
 *   [Your Name / Team Name]
 * @version
 *   1.0
 */
public class MainActivity extends AppCompatActivity {

    /** Field for entering the username when logging in. */
    private EditText usernameEditText;

    /** Field for entering the password when logging in. */
    private EditText passwordEditText;

    /** Button to attempt a login with the provided credentials. */
    private Button loginButton;

    /** Text link to switch to the sign-up activity if the user does not have an account. */
    private TextView signUpTextView;

    /** A helper class for requesting and checking location permissions. */
    private LocationPerm locationPerm;

    /** A constant to identify the location permission request in onRequestPermissionsResult. */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    /**
     * Called when the activity is created. Initializes the login UI fields,
     * checks location permissions, and sets up button listeners for login
     * and sign-up navigation.
     *
     * @param savedInstanceState The previously saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Layout used for the login screen

        // Edge-to-edge display adjustments for modern Android devices.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components.
        usernameEditText = findViewById(R.id.signUpUsernameEditText);
        passwordEditText = findViewById(R.id.signUpPasswordEditText);
        loginButton      = findViewById(R.id.signUpButton);
        signUpTextView   = findViewById(R.id.loginTextView);

        // Because we reuse the sign-up layout for login, we adapt some text fields here.
        TextView signupTitle = findViewById(R.id.signUpTitleTextView);
        signupTitle.setText("Login");
        loginButton.setText("Login");
        signUpTextView.setText("Sign-up");

        // Set up the database instance for login checks (user credential validation).
        // Also see Database.validateLogin(...) which checks the Firestore doc for matching credentials.
        Database db = Database.getInstance();

        // Click listener for "Login" button.
        loginButton.setOnClickListener(v -> loginUser());

        // Click listener for "Sign-up" text, navigates to SignUpActivity if user needs an account.
        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        // Set up location permission handling to support US 06.01.01:
        // "As a participant, I want to optionally attach my current location to a mood event."
        locationPerm = new LocationPerm(this);
        if (!locationPerm.hasLocationPermission()) {
            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    /**
     * Requests the last known location from the device if the user has granted
     * location permissions. Called in {@link #onCreate} if permissions are already granted,
     * or in {@link #onRequestPermissionsResult} if the user approves the request.
     */
    private void getLocation() {
        locationPerm.getLastKnownLocation(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("MainActivity", "Location: " + latitude + ", " + longitude);
                    // This location can be used if the user decides to attach it to a mood event.
                } else {
                    Log.d("MainActivity", "Location is null.");
                }
            }
        });
    }

    /**
     * Collects the username and password from the UI and attempts to validate them
     * using the {@link Database}. If valid, the user is navigated to {@link HomeActivity};
     * otherwise, an error message is displayed (e.g., "Invalid username or password").
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic check for empty fields.
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the Database class to validate credentials. If they match,
        // we set the current user and launch HomeActivity.
        UserDetails userDetails = new UserDetails(username, password);
        Database.getInstance().validateLogin(userDetails, result -> {
            switch (result) {
                case SUCCEED:
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // Mark this username as the "current user" in the Database so other
                    // activities know who is logged in.
                    Database.getInstance().setCurrentUser(username);
                    loadApp();
                    break;

                case INVALID_DETAILS:
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    break;

                case FAIL:
                    Toast.makeText(MainActivity.this, "Error encountered! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates to the {@link HomeActivity} after a successful login, clearing
     * the current activity from the stack so the user cannot return to the login screen
     * via the back button.
     */
    private void loadApp() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Callback for the result from requesting permissions. If the user grants location
     * permission, fetch the device's last known location immediately. Otherwise, show a
     * message stating the importance of location for certain features (if desired).
     *
     * @param requestCode  The integer request code originally supplied to
     *                     requestPermissions, allowing you to identify which request
     *                     is being returned.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     *                     This is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If location permission is granted, fetch location. Otherwise, inform the user
        // that location features will be limited.
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission is required to access the location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
