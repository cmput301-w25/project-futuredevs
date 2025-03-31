package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.futuredevs.database.Database;
import com.futuredevs.models.IModelListener;
import com.futuredevs.models.ModelUserSearch;
import com.futuredevs.models.items.UserSearchResult;
import com.futuredevs.models.ModelBase;


import java.util.ArrayList;
import java.util.List;

/**
 * SearchUserFragment provides an interface for searching users.
 * It displays a SearchView along with a custom search button to initiate user searches.
 * When a search is performed, a ModelUserSearch instance is used to request data from the database.
 * The results are displayed in a ListView via a SearchUserAdapter.
 * This fragment also implements IModelListener to update the displayed list when the model data changes.
 *
 * @author Pranav Gupta
 */
public class SearchUserFragment extends Fragment implements IModelListener<UserSearchResult> {

    private ListView listView;
    private SearchView searchView;
    private ProgressBar searchingProgressBar;
    private TextView emptySearchText;
    private Button searchButton;
    private SearchUserAdapter searchUserAdapter;
    private ArrayList<UserSearchResult> userList; // Full list of users
    private ModelUserSearch modelUserSearch;
    private final String currentUsername = Database.getInstance().getCurrentUser();

    /**
     * Called when the fragment is created.
     * Initializes the ModelUserSearch with the current username and registers this fragment
     * as a change listener to receive updates when search results are available.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Create the model instance with the current username.
        this.modelUserSearch = new ModelUserSearch(this.currentUsername);
        // Register this fragment as a listener to the model.
        this.modelUserSearch.addChangeListener(this);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout, initializes the search view, button, and list view.
     * Sets up a click listener on the custom search button to trigger the user search.
     * Also sets up an item click listener for the list view to handle item selections.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_user, container, false);
        this.listView = view.findViewById(R.id.user_search_list);
        this.searchView = view.findViewById(R.id.search_user_text);
        this.searchingProgressBar = view.findViewById(R.id.loading_user_search);
        this.searchButton = view.findViewById(R.id.search_button);

        this.emptySearchText = view.findViewById(R.id.empty_search_text);
        this.listView.setEmptyView(this.emptySearchText);

        this.userList = new ArrayList<>();
        this.searchUserAdapter = new SearchUserAdapter(this.getContext(), this.userList, this.currentUsername);
        this.listView.setAdapter(this.searchUserAdapter);

        this.searchButton.setOnClickListener(v -> {
            String query = searchView.getQuery().toString();

            if (query.trim().isEmpty()) {
                Toast.makeText(getContext(), "Please enter a Username", Toast.LENGTH_SHORT).show();
            }
            else {
                searchButton.setEnabled(false);
                emptySearchText.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                searchingProgressBar.setVisibility(View.VISIBLE);
                modelUserSearch.setSearchTerm(query);
                modelUserSearch.requestData();
            }
        });

        return view;
    }

    /**
     * Called when the model data changes.
     * Updates the user list with the new search results and notifies the adapter to refresh the ListView.
     *
     * @param theModel The model that was changed.
     */
    @Override
    public void onModelChanged(ModelBase<UserSearchResult> theModel) {
        List<UserSearchResult> results = this.modelUserSearch.getModelData();
        this.searchingProgressBar.setVisibility(View.GONE);
        this.listView.setVisibility(View.VISIBLE);
        this.emptySearchText.setVisibility(View.VISIBLE);
        this.searchButton.setEnabled(true);

        if (results.isEmpty()) {
            this.emptySearchText.setText("No users found!");
        }

        this.userList.clear();
        this.searchUserAdapter.notifyDataSetChanged();
        this.userList.addAll(results);
        this.searchUserAdapter.notifyDataSetChanged();
    }
}
