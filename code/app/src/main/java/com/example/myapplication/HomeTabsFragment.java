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
 */
public class HomeTabsFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomeTabsAdapter homeTabsAdapter;


    /**
     * Inflates the fragment's layout.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent container.
     * @param savedInstanceState The saved state.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.homepage_tabs_layout, container, false);
    }



    /**
     * Sets up the TabLayout and ViewPager2, and attaches a TabLayoutMediator to link them.
     *
     * @param view               The inflated view.
     * @param savedInstanceState The saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tabLayout = view.findViewById(R.id.homepage_tab_layout);
        viewPager = view.findViewById(R.id.homepage_tabs_view);

        homeTabsAdapter = new HomeTabsAdapter(this);
        viewPager.setAdapter(homeTabsAdapter);

        viewPager.setSaveEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Your History");
            } else {
                tab.setText("Following History");
            }
        }).attach();
    }


}

