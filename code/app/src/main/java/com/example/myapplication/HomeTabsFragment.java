package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * HomeTabsFragment displays two tabs ("Your History" and "Following History")
 * using a TabLayout and ViewPager2.
 *
 * @author Pranav Gupta
 */
public class HomeTabsFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomeTabsAdapter homeTabsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage_tabs_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tabLayout = view.findViewById(R.id.homepage_tab_layout);
        viewPager = view.findViewById(R.id.homepage_tabs_view);

        homeTabsAdapter = new HomeTabsAdapter(this);
        viewPager.setAdapter(homeTabsAdapter);

        viewPager.setSaveEnabled(false);  // Prevent fragment recreation

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Your History");
            } else {
                tab.setText("Following History");
            }
        }).attach();
    }

    public MoodHistoryFragment getMoodHistoryFragment() {
        if (homeTabsAdapter != null) {
            return homeTabsAdapter.getMoodHistoryFragment();
        }
        return null;
    }

    /**
     * Apply filter to Your History tab only.
     */
    public void applyEmotionFilter(FilterCriteria filter) {
        // For "Your History" tab, you might still be using a tag lookup or similar logic.
        // If needed, update it similarly by storing a reference via the adapter.
        if (homeTabsAdapter.getMoodHistoryFragment() != null) {
            homeTabsAdapter.getMoodHistoryFragment().applyEmotionFilter(filter);
        }
    }

    /**
     * Clears filters in Your History tab only.
     */
    public void clearAllFilters() {
        if (homeTabsAdapter.getMoodHistoryFragment() != null) {
            homeTabsAdapter.getMoodHistoryFragment().clearFilters();
        }
    }

    /**
     * Apply filter to Following History tab only.
     */
    public void applyFollowingFilter(FilterCriteria filter) {
        if (homeTabsAdapter.getFollowingHistoryFragment() != null) {
            homeTabsAdapter.getFollowingHistoryFragment().applyEmotionFilter(filter);
        }
    }

    /**
     * Clears filters in Following History tab only.
     */
    public void clearFollowingFilter() {
        if (homeTabsAdapter.getFollowingHistoryFragment() != null) {
            homeTabsAdapter.getFollowingHistoryFragment().clearFilters();
        }
    }

    /**
     * Returns the currently selected tab position: 0 (Your History) or 1 (Following History).
     */
    public int getCurrentTabPosition() {
        return viewPager.getCurrentItem();
    }
}
