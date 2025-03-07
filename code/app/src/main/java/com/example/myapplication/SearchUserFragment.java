package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class SearchUserFragment extends Fragment {

    private ListView listView;
    private SearchView searchView;
    private SearchUserAdapter searchUserAdapter;
    private List<SearchUser> userList; // Full list of users

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                         @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_user, container, false);
        listView = view.findViewById(R.id.user_search_list);
        searchView = view.findViewById(R.id.search_user_text);

        return inflater.inflate(R.layout.search_user, container, false);

    }
}
