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
import androidx.appcompat.app.AlertDialog;  // For sign-out confirmation dialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;  // For popup menu
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;  // For the FAB
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;

    // Firestore instance
    private FirebaseFirestore db;
    private MaterialToolbar toolbar;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private LocationPerm locationPerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find Views (Login screen)
        usernameEditText = findViewById(R.id.signUpUsernameEditText);
        passwordEditText = findViewById(R.id.signUpPasswordEditText);
        loginButton      = findViewById(R.id.signUpButton);
        signUpTextView   = findViewById(R.id.loginTextView);
        signUpTextView.setText("Sign-up");
        TextView signupTitle =findViewById(R.id.signUpTitleTextView);
        signupTitle.setText("Login");
        loginButton.setText("Login");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up Login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to SignUpActivity when "Sign-up" is clicked
        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        // Initialize the location_perm class
        locationPerm = new LocationPerm(this);

        // Check for location permissions
        if (!locationPerm.hasLocationPermission()) {
            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }


// Handle location permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch the location
                getLocation();
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Location permission is required to access the location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to get the last known location
    private void getLocation() {
        locationPerm.getLastKnownLocation(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("MainActivity", "Location: " + latitude + ", " + longitude);

                // You can use the location object here, like showing it in UI
//                Toast.makeText(MainActivity.this, "Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
            } else {
                Log.d("MainActivity", "Location is null.");
            }
        });
    }

    /**
     * Checks Firestore for user credentials and logs in if valid.
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        UserDetails userDetails = new UserDetails(username, password);

        // Basic validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        Database.getInstance().validateLogin(userDetails, r -> {
            switch (r) {
                case SUCCEED:
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // when user logs in
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

        // Firestore lookup
//        db.collection("users").document(username).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String storedPassword = documentSnapshot.getString("password");
//                        if (storedPassword != null && storedPassword.equals(password)) {
//                            // Login successful
//                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
//                            loadApp();
//                        } else {
//                            Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
    }

    /**
     * Loads the homepage (homepage.xml) after login.
     */
    private void loadApp() {
        setContentView(R.layout.homepage);

        // Toolbar in homepage.xml
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // SIGN OUT POPUP: Top-left navigation icon
        toolbar.setNavigationOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            // This menu should have a "Sign Out" item
            // e.g., res/menu/user_profile_menu.xml with <item android:id="@+id/menu_sign_out" ... />
            popupMenu.getMenuInflater().inflate(R.menu.user_profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_sign_out) {
                    showSignOutConfirmation();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Hook up the FloatingActionButton to open NewMoodActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Create instances of your fragments
        Fragment firstFragment  = new HomeTabsFragment();
        Fragment secondFragment = new mapfragmenttest();
        Fragment thirdFragment  = new SearchUserFragment();
        Fragment fourthFragment = new NotificationsFragment();

        // Set default fragment to homepage
        setFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                currentFragment = firstFragment;
            } else if (itemId == R.id.map) {
                currentFragment = secondFragment;
            } else if (itemId == R.id.search) {
                currentFragment = thirdFragment;
            } else if (itemId == R.id.notifications) {
                currentFragment = fourthFragment;
            }

            if (currentFragment != null) {
                setFragment(currentFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Replaces the current fragment with the selected one.
     */
    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .setReorderingAllowed(true)
//                .addToBackStack(null)
                .commit();
    }

    /**
     * Shows a confirmation dialog before signing out.
     */
    private void showSignOutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you wish to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> signOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Clears session data if needed, then returns to the login screen.
     */
    private void signOut() {
        // Clear any stored data, e.g., SharedPreferences or FirebaseAuth signOut()

        // Return to the login screen
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}