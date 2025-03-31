package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.futuredevs.database.Database;
import com.futuredevs.database.DatabaseResult;
import com.futuredevs.models.ViewModelMoods;
import com.futuredevs.models.ViewModelMoods.ViewModelMoodsFactory;
import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.ViewModelMoodsFollowing.ViewModelMoodsFollowingFactory;
import com.futuredevs.models.ViewModelNotifications;
import com.futuredevs.models.ViewModelNotifications.ViewModelNotificationsFactory;
import com.futuredevs.models.ViewModelUserProfile;
import com.futuredevs.models.ViewModelUserProfile.ViewModelUserProfileFactory;
import com.futuredevs.models.items.MoodPost;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private HomeTabsFragment homeTabsFragment; // Store your HomeTabsFragment here

    private ViewModelMoods viewModelMoods;
    private ViewModelMoodsFollowing viewModelMoodsFollowing;
    private ViewModelNotifications viewModelNotifications;
	private ViewModelUserProfile viewModelUserProfile;
    private BadgeDrawable notifBadge;

    private boolean showFilterIconFlag = true;
    private static final int FILTER_REQUEST_CODE = 1001;

    private FilterCriteria currentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.homepage);
        EdgeToEdge.enable(this);

        this.toolbar = findViewById(R.id.topAppBar);
        this.setSupportActionBar(this.toolbar);

        this.toolbar.setNavigationOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.user_profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_view_profile) {
                    Intent intent = new Intent(HomeActivity.this, ViewMoodUserActivity.class);
                    intent.putExtra("user_profile", true);
                    intent.putExtra("name", Database.getInstance().getCurrentUser());
                    startActivity(intent);
                }
                else if (item.getItemId() == R.id.menu_sign_out) {
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
		ViewModelUserProfileFactory profileFactory = new ViewModelUserProfileFactory(username);
		this.viewModelUserProfile = new ViewModelProvider(this, profileFactory).get(ViewModelUserProfile.class);
        Intent addIntent = this.getIntent();

        if (addIntent.getExtras() != null) {
            MoodPost post = addIntent.getParcelableExtra("mood");

            if (addIntent.hasExtra("added_post")) {
                this.viewModelMoods.addMood(post, r -> {
                    if (r == DatabaseResult.SUCCESS) {
                        Toast.makeText(HomeActivity.this, "Mood added", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(HomeActivity.this, "Failed to add mood", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (addIntent.hasExtra("edit_post")) {
                this.viewModelMoods.updateMood(post, r -> {
                    if (r == DatabaseResult.SUCCESS) {
                        if (homeTabsFragment != null) {
                            MoodHistoryFragment historyFragment = homeTabsFragment.getMoodHistoryFragment();
                            if (historyFragment != null) {
                                historyFragment.updateMoodInList(post);
                            }
                        }
                        Toast.makeText(HomeActivity.this, "Updated mood", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(HomeActivity.this, "Failed to update mood", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (addIntent.hasExtra("delete_post")) {
                this.viewModelMoods.removeMood(post, r -> {
                    if (r == DatabaseResult.SUCCESS) {
                        Toast.makeText(HomeActivity.this, "Deleted mood", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(HomeActivity.this, "Failed to delete mood", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddEditMoodActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = this.findViewById(R.id.bottomNavigationView);
        this.notifBadge = bottomNavigationView.getOrCreateBadge(R.id.notifications);
        this.notifBadge.setVisible(false);
        this.viewModelNotifications.getData().observe(this, notifs -> {
            int num = notifs.size();

            if (num == 0) {
                notifBadge.setVisible(false);
                notifBadge.clearNumber();
            }
            else {
                notifBadge.setVisible(true);
                notifBadge.setNumber(num);
            }
        });

        // Initialize and set HomeTabsFragment
        homeTabsFragment = new HomeTabsFragment();
        Fragment mapsFragment = new MapsFragment();
        Fragment searchUserFragment = new SearchUserFragment();
        Fragment notificationsFragment = new NotificationsFragment();
        this.setFragment(this.homeTabsFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                Log.i("HOME", "Requesting mood posts for user and following");
                currentFragment = homeTabsFragment;
                fab.setVisibility(View.VISIBLE);
                viewModelMoods.requestData();
                viewModelMoodsFollowing.requestData();
                toolbar.setTitle("Home");
                setShowFilterIcon(true);
            }
            else if (itemId == R.id.map) {
                currentFragment = mapsFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Map");
                setShowFilterIcon(false);
            }
            else if (itemId == R.id.search) {
                currentFragment = searchUserFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Search");
                setShowFilterIcon(false);
            }
            else if (itemId == R.id.notifications) {
                currentFragment = notificationsFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Notifications");
                viewModelNotifications.requestData();
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

    /**
     * Sets the fragment in the home's frame layout to the given
     * {@code fragment}.
     *
     * @param fragment the fragment to place in the home frame
     */
    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .setReorderingAllowed(true)
                .commit();
    }

    /**
     * Shows an alert dialog popup asking the user for sign-out confirmation.
     */
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
        this.getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
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
            if (this.homeTabsFragment != null) {
                int currentTab = this.homeTabsFragment.getCurrentTabPosition();

                boolean isNoFilter = (emotion == null || emotion.equals("Select mood")) &&
                        (timeRange == null || timeRange.equals("All time")) &&
                        (filterWord == null || filterWord.isEmpty());

                if (currentTab == 0) {  // Your History tab
                    if (isNoFilter) {
                        this.homeTabsFragment.clearAllFilters();
                    }
                    else {
                        FilterCriteria filter = new FilterCriteria(emotion, timeRange, filterWord);
                        this.homeTabsFragment.applyEmotionFilter(filter);
                    }
                }
                else if (currentTab == 1) {  // Following History tab
                    if (isNoFilter) {
                        this.homeTabsFragment.clearFollowingFilter();
                    }
                    else {
                        FilterCriteria filter = new FilterCriteria(emotion, timeRange, filterWord);
                        this.homeTabsFragment.applyFollowingFilter(filter);
                    }
                }
            }
        }
    }

    /**
     * Sends an intent to move the user back to the login screen and logs
     * them out.
     */
    private void signOut() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Sets the visibility of the filter button based on {@code show}.
     *
     * @param show whether to show the filter button or not
     */
    public void setShowFilterIcon(boolean show) {
        this.showFilterIconFlag = show;
        invalidateOptionsMenu();
    }
}
