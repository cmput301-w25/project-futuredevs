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

    // Fragments for bottom navigation
    private Fragment homeTabsFragment;
    private Fragment mapFragment;
    private Fragment searchUserFragment;
    private Fragment notificationsFragment;

    private ViewModelMoods viewModelMoods;
    private ViewModelMoodsFollowing viewModelMoodsFollowing;

    private boolean showFilterIconFlag = true;

    private static final int FILTER_REQUEST_CODE = 1001;
    private FilterCriteria currentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.homepage);

        this.toolbar = findViewById(R.id.topAppBar);
        this.setSupportActionBar(toolbar);

        this.toolbar.setNavigationOnClickListener(view -> {
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
        Intent addIntent = this.getIntent();

        if (addIntent.getExtras() != null) {
            if (addIntent.hasExtra("added_post")) {
                MoodPost post = addIntent.getParcelableExtra("mood");
                this.viewModelMoods.addMood(post);
            }
        }

        FloatingActionButton fab = this.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NewMoodActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = this.findViewById(R.id.bottomNavigationView);

        Fragment firstFragment  = new HomeTabsFragment();
        Fragment secondFragment = new MapFragmentTest();
        Fragment thirdFragment  = new SearchUserFragment();
        Fragment fourthFragment = new NotificationsFragment();

        this.setFragment(firstFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment currentFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                currentFragment = firstFragment;
                fab.setVisibility(View.VISIBLE);
                this.viewModelMoods.requestData();
                this.viewModelMoodsFollowing.requestData();
                toolbar.setTitle("Home");
                setShowFilterIcon(true);
            }
            else if (itemId == R.id.map) {
                currentFragment = secondFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Map");
                setShowFilterIcon(false);
            }
            else if (itemId == R.id.search) {
                currentFragment = thirdFragment;
                fab.setVisibility(View.GONE);
                toolbar.setTitle("Search");
                setShowFilterIcon(false);
            }
            else if (itemId == R.id.notifications) {
                currentFragment = fourthFragment;
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

            if ((emotion == null || emotion.equals("Select mood")) &&
                    (timeRange == null || timeRange.equals("All time")) &&
                    (filterWord == null || filterWord.isEmpty())) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flFragment);
                if (currentFragment instanceof HomeTabsFragment) {
                    ((HomeTabsFragment) currentFragment).clearAllFilters();
                }
            } else {
                currentFilter = new FilterCriteria(emotion, timeRange, filterWord);
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flFragment);
                if (currentFragment instanceof HomeTabsFragment) {
                    ((HomeTabsFragment) currentFragment).applyEmotionFilter(currentFilter);
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