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

//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
//    private LocationPerm locationPerm;

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

//        // Initialize the location_perm class
//        locationPerm = new LocationPerm(this);
//
//        // Check for location permissions
//        if (!locationPerm.hasLocationPermission()) {
//            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
//        } else {
//            getLocation();
//        }
    }


//// Handle location permission result
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, fetch the location
//                getLocation();
//            } else {
//                // Permission denied, show a message
//                Toast.makeText(this, "Location permission is required to access the location.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    // Method to get the last known location
//    private void getLocation() {
//        locationPerm.getLastKnownLocation(location -> {
//            if (location != null) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                Log.d("MainActivity", "Location: " + latitude + ", " + longitude);
//
//                // You can use the location object here, like showing it in UI
////                Toast.makeText(MainActivity.this, "Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
//            } else {
//                Log.d("MainActivity", "Location is null.");
//            }
//        });
//    }

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
    }

    /**
     * Loads the homepage (homepage.xml) after login.
     */
    private void loadApp() {
        // Launch HomeActivity after successful login and finish MainActivity
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}