package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.futuredevs.models.ViewModelMoodsFollowing;
import com.futuredevs.models.items.MoodPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the most recent mood event from each followed user.
 * It uses {@link ViewModelMoodsFollowing} to fetch mood posts and groups them by username,
 * retaining only the most recent post (with the highest timestamp) per user.
 */
public class FollowingHistoryFragment extends Fragment /* implements IModelListener<MoodPost>*/ {

    private RecyclerView recyclerView;
    private FollowingHistoryAdapter adapter;
    private List<MoodPost> moodHistoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout defined in following_mood_history_list.xml (which now uses a LinearLayout)
        View view = inflater.inflate(R.layout.following_mood_history_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FollowingHistoryAdapter(moodHistoryList);
        recyclerView.setAdapter(adapter);

        if (this.getActivity() != null) {
            ViewModelMoodsFollowing viewModelMoods = new ViewModelProvider(this.getActivity())
                                                            .get(ViewModelMoodsFollowing.class);
            viewModelMoods.getData().observe(this.getViewLifecycleOwner(), o -> {
                // Group posts by username; for each user, keep the post with the highest timePosted.
//              Map<String, MoodPost> mostRecentPosts = new HashMap<>();
//              for (MoodPost post : allPosts) {
//                  String user = post.getUser();
//                  if (!mostRecentPosts.containsKey(user)) {
//                      mostRecentPosts.put(user, post);
//                  }
//                  else {
//                      MoodPost existing = mostRecentPosts.get(user);
//                      if (post.getTimePosted() > existing.getTimePosted()) {
//                          mostRecentPosts.put(user, post);
//                      }
//                  }
//              }

                moodHistoryList.clear();
                moodHistoryList.addAll(o);
                moodHistoryList.sort((p1, p2) -> -Long.compare(p1.getTimePosted(), p2.getTimePosted()));
                adapter.notifyDataSetChanged();
            });
        }

        return view;
    }
}
