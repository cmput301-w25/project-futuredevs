package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.futuredevs.database.Database;
import com.futuredevs.models.IModelListener;
import com.futuredevs.models.ModelUserSearch;
import com.futuredevs.models.items.MoodPost;
import com.futuredevs.models.items.UserSearchResult;
import com.futuredevs.models.ModelBase;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment implements IModelListener<UserSearchResult> {

    private ListView listView;
    private SearchView searchView;
    private SearchUserAdapter searchUserAdapter;
    private ArrayList<UserSearchResult> userList; // Full list of users
    private ModelUserSearch modelUserSearch;
    String currentUsername = Database.getInstance().getCurrentUser();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create the model instance with the current username.
        modelUserSearch = new ModelUserSearch(currentUsername);
        // Register this fragment as a listener to the model.
        modelUserSearch.addChangeListener(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_user, container, false);
        listView = view.findViewById(R.id.user_search_list);
        searchView = view.findViewById(R.id.search_user_text);

        userList = new ArrayList<>();
        searchUserAdapter = new SearchUserAdapter(getContext(), userList, currentUsername);
        listView.setAdapter(searchUserAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a Username", Toast.LENGTH_SHORT).show();
                } else {
                    // Set the search term in the model and request data.
                    modelUserSearch.setSearchTerm(query);
                    modelUserSearch.requestData();
                }
                return true;
            }

            @Override
//          Used to display search results as the search text is being added, could add later.
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;

    }

    /**
     * @param updatedData
     */
    @Override
    public void onModelUpdate(List<MoodPost> updatedData) {

    }

    /**
     * @param theModel the model that was changed
     */
    @Override
    public void onModelChanged(ModelBase<UserSearchResult> theModel) {
        List<UserSearchResult> results = modelUserSearch.getModelData();
        userList.clear();
        userList.addAll(results);
        searchUserAdapter.notifyDataSetChanged();
    }
}
