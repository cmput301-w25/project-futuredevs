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
import com.google.firebase.firestore.FirebaseFirestore;

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
 *   <li>Display an error message on the UI if login fails, instead of using a Toast.</li>
 *   <li>Request location permissions (if not already granted) so that users can optionally
 *       attach their current location to mood events.</li>
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

    /**
     * TextView for displaying login error messages.
     * This is reused from the sign-up layout to provide consistent error display.
     */
    private TextView signUpMessageTextView;

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
        // We are reusing the sign-up layout for login purposes.
        setContentView(R.layout.activity_sign_up);

        // Edge-to-edge display adjustments.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components.
        usernameEditText = findViewById(R.id.signUpUsernameEditText);
        passwordEditText = findViewById(R.id.signUpPasswordEditText);
        loginButton = findViewById(R.id.signUpButton);
        signUpMessageTextView = findViewById(R.id.signUpMessageTextView);
        signUpTextView = findViewById(R.id.loginTextView);

        // Because we reuse the sign-up layout for login, update some text fields.
        TextView signupTitle = findViewById(R.id.signUpTitleTextView);
        signupTitle.setText("Login");
        loginButton.setText("Login");
        signUpTextView.setText("Sign-up");

        // Click listener for "Login" button.
        loginButton.setOnClickListener(v -> loginUser());

        // Click listener for "Sign-up" text, navigates to SignUpActivity if user needs an account.
        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        // Set up location permission handling.
        locationPerm = new LocationPerm(this);
        if (!locationPerm.hasLocationPermission()) {
            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    /**
     * Requests the last known location from the device if the user has granted
     * location permissions. Called in onCreate if permissions are already granted,
     * or in onRequestPermissionsResult if the user approves the request.
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
     * otherwise, an error message is displayed in the UI.
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Clear any previous error message.
        signUpMessageTextView.setText("");
        signUpMessageTextView.setVisibility(TextView.GONE);

        // Validate that fields are not empty.
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            signUpMessageTextView.setText("Enter username & password");
            signUpMessageTextView.setVisibility(TextView.VISIBLE);
            return;
        }

        // Attempt to validate login credentials.
        UserDetails userDetails = new UserDetails(username, password);
        Database.getInstance().validateLogin(userDetails, result -> {
            switch (result) {
                case SUCCEED:
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // Set the current user in the Database for app-wide access.
                    Database.getInstance().setCurrentUser(username);
                    loadApp();
                    break;
                case INVALID_DETAILS:
                    // Instead of a Toast, show error in signUpMessageTextView.
                    signUpMessageTextView.setText("Invalid username or password");
                    signUpMessageTextView.setVisibility(TextView.VISIBLE);
                    break;
                case FAIL:
                    // Show a general error message.
                    signUpMessageTextView.setText("Error encountered! Please try again");
                    signUpMessageTextView.setVisibility(TextView.VISIBLE);
                    break;
            }
        });
    }

    /**
     * Navigates to HomeActivity after a successful login, clearing this activity from the back stack.
     */
    private void loadApp() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Callback for the result of permission requests. If location permission is granted,
     * fetch the device's last known location. Otherwise, display a message (or take alternative action).
     *
     * @param requestCode  The request code passed in requestPermissions.
     * @param permissions  The requested permissions.
     * @param grantResults The results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
