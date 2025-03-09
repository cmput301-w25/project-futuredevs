package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * HomeTabsAdapter is a FragmentStateAdapter that provides the appropriate fragment
 * for each tab in the HomeTabsFragment.
 */
public class HomeTabsAdapter extends FragmentStateAdapter {

    /**
     * Constructs a new HomeTabsAdapter.
     *
     * @param fragment the fragment that this adapter is associated with
     */
    public HomeTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }


    /**
     * Creates a fragment for the given tab position.
     *
     * @param position the position of the tab
     * @return a fragment corresponding to the tab position; returns MoodHistoryFragmentTest for
     *         position 0 and FollowingHistoryFragmentTest for any other position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the fragment corresponding to the tab position
        if (position == 0) {
            return new MoodHistoryFragmentTest();
        } else {
            return new FollowingHistoryFragmentTest();
        }
    }


    /**
     * Returns the total number of tabs.
     *
     * @return the total number of tabs (2).
     */
    @Override
    public int getItemCount() {
        // We have two tabs
        return 2;
    }


}
