package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class SearchUserFragment extends AppCompatActivity {

    private EditText search_box_text;
    private Button search_button;
    private ListView search_results_view;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);

        search_box_text = findViewById(R.id.search_user_text);
        search_button = findViewById(R.id.search_button);
        search_results_view = findViewById(R.id.user_search_list);




    }
    }
