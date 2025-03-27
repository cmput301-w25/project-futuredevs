package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import com.futuredevs.models.ViewModelNotifications;
import com.futuredevs.models.ViewModelNotifications.ViewModelNotificationsFactory;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends NetworkActivity /*implements INotificationListener*/ {
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private HomeTabsFragment homeTabsFragment; // Store your HomeTabsFragment here

    private ViewModelMoods viewModelMoods;
    private ViewModelMoodsFollowing viewModelMoodsFollowing;
    private ViewModelNotifications viewModelNotifications;
    private BadgeDrawable notifBadge;

    private boolean showFilterIconFlag = true;
    private static final int FILTER_REQUEST_CODE = 1001;
    public static final int EDIT_MOOD_REQUEST_CODE = 2001;

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
        this.viewModelMoods = new ViewModelProvider(this, userFactory).get(ViewModelMoods.class);
        this.viewModelMoodsFollowing = new ViewModelProvider(this, followingFactory)
                                            .get(ViewModelMoodsFollowing.class);
        ViewModelNotificationsFactory notificationsFactory = new ViewModelNotificationsFactory(username);
        this.viewModelNotifications = new ViewModelProvider(this, notificationsFactory).get(ViewModelNotifications.class);
        Intent addIntent = this.getIntent();

        if (addIntent.getExtras() != null) {
            if (addIntent.hasExtra("added_post")) {
                MoodPost post = addIntent.getParcelableExtra("mood");
                this.viewModelMoods.addMood(post);
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = this.findViewById(R.id.bottomNavigationView);
        this.notifBadge = bottomNavigationView.getOrCreateBadge(R.id.notifications);
        this.notifBadge.setVisible(false);
        this.viewModelNotifications.getData().observe(this, notifs -> {
            int num = notifs.size();

            if (num == 0) {
                this.notifBadge.setVisible(false);
                this.notifBadge.clearNumber();
            }
            else {
                this.notifBadge.setVisible(true);
                this.notifBadge.setNumber(num);
            }
        });

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
                this.viewModelNotifications.requestData();
                setShowFilterIcon(false);
            }

            if (currentFragment != null) {
                setFragment(currentFragment);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("HOME", "Resuming home activity");
        this.viewModelMoods.requestData();
        this.viewModelMoodsFollowing.requestData();
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
        } else if (requestCode == EDIT_MOOD_REQUEST_CODE && resultCode == RESULT_OK) {
            boolean wasEdited = data.getBooleanExtra("mood_edited", false);
            if (wasEdited) {
                MoodPost edited = data.getParcelableExtra("mood");
                viewModelMoods.updateMood(edited);
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
