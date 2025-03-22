package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.ViewModelMoods.ViewModelMoodsFactory;
import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.ViewModelMoodsFollowing.ViewModelMoodsFollowingFactory;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private HomeTabsFragment homeTabsFragment; // Store your HomeTabsFragment here

    private ViewModelMoods viewModelMoods;
    private ViewModelMoodsFollowing viewModelMoodsFollowing;

    private boolean showFilterIconFlag = true;
    private static final int FILTER_REQUEST_CODE = 1001;
    private FilterCriteria currentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);
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

        String username = Database.getInstance().getCurrentUser();
        ViewModelMoodsFactory userFactory = new ViewModelMoodsFactory(username);
        ViewModelMoodsFollowingFactory followingFactory = new ViewModelMoodsFollowingFactory(username);
        viewModelMoods = new ViewModelProvider(this, userFactory).get(ViewModelMoods.class);
        viewModelMoodsFollowing = new ViewModelProvider(this, followingFactory).get(ViewModelMoodsFollowing.class);

        Intent addIntent = getIntent();
        if (addIntent.getExtras() != null && addIntent.hasExtra("added_post")) {
            MoodPost post = addIntent.getParcelableExtra("mood");
            viewModelMoods.addMood(post);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Initialize and set HomeTabsFragment
        homeTabsFragment = new HomeTabsFragment();
        Fragment mapFragment = new MapFragmentTest();
        Fragment searchUserFragment = new SearchUserFragment();
        Fragment notificationsFragment = new NotificationsFragment();

        setFragment(homeTabsFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                currentFragment = homeTabsFragment;
                fab.setVisibility(View.VISIBLE);
                viewModelMoods.requestData();
                viewModelMoodsFollowing.requestData();
                toolbar.setTitle("Home");
                setShowFilterIcon(true);
            } else if (itemId == R.id.map) {
                currentFragment = mapFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Map");
                setShowFilterIcon(false);
            } else if (itemId == R.id.search) {
                currentFragment = searchUserFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Search");
                setShowFilterIcon(false);
            } else if (itemId == R.id.notifications) {
                currentFragment = notificationsFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Notifications");
                setShowFilterIcon(false);
            }

            if (currentFragment != null) {
                setFragment(currentFragment);
                return true;
            }
            return false;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (currentFragment instanceof ViewProfileFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you wish to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> signOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        if (filterItem != null) {
            filterItem.setVisible(showFilterIconFlag);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            Intent intent = new Intent(this, FilterActivity.class);
            startActivityForResult(intent, FILTER_REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
            String emotion = data.getStringExtra("FILTER_MOOD");
            String timeRange = data.getStringExtra("FILTER_TIME");
            String filterWord = data.getStringExtra("FILTER_WORD");

            // Use the stored homeTabsFragment instance instead of findFragmentById(...)
            if (homeTabsFragment != null) {
                int currentTab = homeTabsFragment.getCurrentTabPosition();

                boolean isNoFilter = (emotion == null || emotion.equals("Select mood")) &&
                        (timeRange == null || timeRange.equals("All time")) &&
                        (filterWord == null || filterWord.isEmpty());

                if (currentTab == 0) {  // Your History tab
                    if (isNoFilter) {
                        homeTabsFragment.clearAllFilters();
                    } else {
                        FilterCriteria filter = new FilterCriteria(emotion, timeRange, filterWord);
                        homeTabsFragment.applyEmotionFilter(filter);
                    }
                } else if (currentTab == 1) {  // Following History tab
                    if (isNoFilter) {
                        homeTabsFragment.clearFollowingFilter();
                    } else {
                        FilterCriteria filter = new FilterCriteria(emotion, timeRange, filterWord);
                        homeTabsFragment.applyFollowingFilter(filter);
                    }
                }
            }
        }
    }

    private void signOut() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void setShowFilterIcon(boolean show) {
        showFilterIconFlag = show;
        invalidateOptionsMenu();
    }
}
