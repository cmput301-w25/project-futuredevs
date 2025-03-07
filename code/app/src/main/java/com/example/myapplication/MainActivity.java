package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
=======
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
>>>>>>> c4296e875824adabc9f2057796a502d85d3554b9

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

<<<<<<< HEAD
    private ListView listView;
    private List<MoodEvent> moodEventList;
=======
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;

    // Firestore instance
    private FirebaseFirestore db;
    private MaterialToolbar toolbar;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private location_perm locationPerm;
>>>>>>> c4296e875824adabc9f2057796a502d85d3554b9

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

<<<<<<< HEAD
                //  ListView
        listView = findViewById(R.id.moodEventListView);


                // this Sorts the mood events for timestamp in reverse chronological order with most recent first, like in user story 5.03.01
        Collections.sort(moodEventList, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

                    // this gets data ready for the ListView
        List<String> displayList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            // this makes  list of strings to show in ListView, including mood and timestamp
        for (MoodEvent event : moodEventList) {
            String displayText = event.getMood() + " - " + dateFormat.format(event.getTimestamp());
            displayList.add(displayText);
        }

            // this makes an ArrayAdapter to join  string list to  ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);

                        // this set  adapter to the ListView
        listView.setAdapter(adapter);
    }
=======
//         Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        // Find Views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up Login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to SignUpActivity when "Sign-up" is clicked
        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        // Initialize the location_perm class
        locationPerm = new location_perm(this);

        // Check for location permissions
        if (!locationPerm.hasLocationPermission()) {
            // Request the permission
            locationPerm.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLocation();
            } else {
                // Permission denied
                // Handle the case where the user denies the permission
            }
        }
    }

    private void getLocation() {
        locationPerm.getLastKnownLocation(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Use the location object
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                }
            }
        });
    }


    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Firestore if the user exists and the password matches
        db.collection("users").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String storedPassword = documentSnapshot.getString("password");
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Login successful
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to HomeActivity (which loads homepage.xml)
                            loadApp();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


// This method loads the main app (homepage) after login
    private void loadApp() {
//      loads the homepage xml file
        setContentView(R.layout.homepage);

//      Set up the persistent Toolbar
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

//      Create instances of the fragments used in the app
        Fragment firstFragment = new homefrragmenttest();
        Fragment secondFragment = new mapfragmenttest();
        Fragment thirdFragment = new SearchUserFragment();
        Fragment fourthFragment = new NotificationsFragment();

//      set default fragment to homepage
        setFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

//          Checks which button on navbar is clicked and assign the corresponding fragment
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

//  This helper method replaces the current fragment with the clicked fragment
    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }
>>>>>>> c4296e875824adabc9f2057796a502d85d3554b9
}

