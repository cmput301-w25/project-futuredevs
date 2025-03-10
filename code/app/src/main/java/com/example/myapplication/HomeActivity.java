package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    // Fragments for bottom navigation
    private Fragment homeTabsFragment;
    private Fragment mapFragment;
    private Fragment searchUserFragment;
    private Fragment notificationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure your homepage.xml file is placed in res/layout/ and named correctly.
        setContentView(R.layout.homepage);

        // Toolbar in homepage.xml
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // SIGN OUT POPUP: Top-left navigation icon
        toolbar.setNavigationOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);
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
            Intent intent = new Intent(HomeActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Create instances of your fragments
        Fragment firstFragment  = new HomeTabsFragment();
        Fragment secondFragment = new MapFragmentTest();
        Fragment thirdFragment  = new SearchUserFragment();
        Fragment fourthFragment = new NotificationsFragment();

        // Set default fragment to homepage
        setFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                currentFragment = firstFragment;
                fab.setVisibility(View.VISIBLE);
                toolbar.setTitle("Home");

            } else if (itemId == R.id.map) {
                currentFragment = secondFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Map");

            } else if (itemId == R.id.search) {
                currentFragment = thirdFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Search");

            } else if (itemId == R.id.notifications) {
                currentFragment = fourthFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Notifications");

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
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}