package com.example.myapplication;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.models.IModelListener;
import com.futuredevs.models.ModelBase;
import com.futuredevs.models.ModelMoods;
import com.futuredevs.models.ModelMoodsFollowing;
import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.MoodPost.Emotion;
import com.futuredevs.models.items.MoodPost.SocialSituation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements IModelListener<MoodPost> {
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    // Fragments for bottom navigation
    private Fragment homeTabsFragment;
    private Fragment mapFragment;
    private Fragment searchUserFragment;
    private Fragment notificationsFragment;

    private ViewModelUserMoods userMoodsVM;
    private ViewModelFollowingMoods userFollowingMoodsVM;
    private ModelMoods moodModel;
    private ModelMoodsFollowing followingModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure your homepage.xml file is placed in res/layout/ and named correctly.
        this.setContentView(R.layout.homepage);

        // Toolbar in homepage.xml
        this.toolbar = findViewById(R.id.topAppBar);
        this.setSupportActionBar(toolbar);

        // SIGN OUT POPUP: Top-left navigation icon
        this.toolbar.setNavigationOnClickListener(view -> {
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

        this.moodModel = new ModelMoods(Database.getInstance().getCurrentUser());
        this.moodModel.addChangeListener(this);
        Intent addIntent = this.getIntent();

        if (addIntent.getExtras() != null) {
            if (addIntent.hasExtra("added_post")) {
                MoodPost post = addIntent.getParcelableExtra("mood");
                this.moodModel.addItem(post);
            }
        }

        // Hook up the FloatingActionButton to open NewMoodActivity
        FloatingActionButton fab = this.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        this.userMoodsVM = new ViewModelProvider(this).get(ViewModelUserMoods.class);
        this.userFollowingMoodsVM = new ViewModelProvider(this).get(ViewModelFollowingMoods.class);
        this.followingModel = new ModelMoodsFollowing(Database.getInstance().getCurrentUser());
        this.followingModel.addChangeListener(this);
        this.moodModel.requestData();
        this.followingModel.requestData();

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = this.findViewById(R.id.bottomNavigationView);

        // Create instances of your fragments
        Fragment firstFragment  = new HomeTabsFragment();
        Fragment secondFragment = new MapFragmentTest();
        Fragment thirdFragment  = new SearchUserFragment();
        Fragment fourthFragment = new NotificationsFragment();

        // Set default fragment to homepage
        this.setFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                currentFragment = firstFragment;
                fab.setVisibility(View.VISIBLE);
                moodModel.requestData();
                followingModel.requestData();
                toolbar.setTitle("Home");
            }
            else if (itemId == R.id.map) {
                currentFragment = secondFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Map");
            }
            else if (itemId == R.id.search) {
                currentFragment = thirdFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Search");

            }
            else if (itemId == R.id.notifications) {
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
        Database.getInstance().setCurrentUser(null);

        // Return to the login screen
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onModelChanged(ModelBase<MoodPost> theModel) {
        List<MoodPost> posts = new ArrayList<>(theModel.getModelData());

        if (theModel instanceof ModelMoods) {
            this.userMoodsVM.setMoodData(posts);
        }
        else if (theModel instanceof ModelMoodsFollowing) {
            this.userFollowingMoodsVM.setMoodData(posts);
        }
    }
}