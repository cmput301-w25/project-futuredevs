package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class SearchUserAdapter extends ArrayAdapter<SearchUser> {
    private ArrayList<SearchUser> searchusers;
    private Context context;

    private FirebaseFirestore db;


    public SearchUserAdapter(Context context, ArrayList<SearchUser> searchusers) {
        super(context, 0, searchusers);
        this.searchusers = searchusers;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        SearchUser searchUser = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_search_results, parent, false);
        }

        TextView usernameTextView = convertView.findViewById(R.id.result_username_text);
        Button followButton = convertView.findViewById(R.id.result_user_follow_button);

        usernameTextView.setText(searchUser.getUsername());

         return convertView;
    }
}
