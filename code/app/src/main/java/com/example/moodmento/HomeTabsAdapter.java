package com.example.moodmento;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * HomeTabsAdapter is a FragmentStateAdapter that provides the appropriate fragment
 * for each tab in the HomeTabsFragment.
 */
public class HomeTabsAdapter extends FragmentStateAdapter {
    private MoodHistoryFragment moodHistoryFragment;
    private FollowingHistoryFragment followingHistoryFragment;

    public HomeTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            if (moodHistoryFragment == null) {
                moodHistoryFragment = new MoodHistoryFragment();
            }
            return moodHistoryFragment;
        } else {
            if (followingHistoryFragment == null) {
                followingHistoryFragment = new FollowingHistoryFragment();
            }
            return followingHistoryFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    // Getters to access the fragment instances
    public MoodHistoryFragment getMoodHistoryFragment() {
        return moodHistoryFragment;
    }

    public FollowingHistoryFragment getFollowingHistoryFragment() {
        return followingHistoryFragment;
    }
}
