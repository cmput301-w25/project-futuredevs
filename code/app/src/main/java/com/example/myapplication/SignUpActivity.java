package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private EditText signUpUsernameEditText;
    private EditText signUpPasswordEditText;
    private Button signUpButton;
    private TextView signUpMessageTextView;
    private TextView loginTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Find Views
        signUpUsernameEditText = findViewById(R.id.signUpUsernameEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signUpMessageTextView = findViewById(R.id.signUpMessageTextView);
        loginTextView = findViewById(R.id.loginTextView);

        db = FirebaseFirestore.getInstance();

        signUpButton.setOnClickListener((View v) -> {
            String username = signUpUsernameEditText.getText().toString().trim();
            String password = signUpPasswordEditText.getText().toString().trim();

            // Reset the message for new attempts
            signUpMessageTextView.setText("");
            signUpMessageTextView.setVisibility(View.GONE);

            if (username.isEmpty() || password.isEmpty()) {
                signUpMessageTextView.setText("Enter username & password");
                signUpMessageTextView.setVisibility(View.VISIBLE);
                return;
            }

            // Debugging Firestore Queries
            Log.d("SignUpDebug", "Checking Firestore for existing username: " + username);

            UserDetails userDetails = new UserDetails(username, password);
            Database.getInstance().attemptSignup(userDetails, r -> {
                switch (r) {
                    case SUCCEED:
                        Log.d("SignUpDebug", "Sign-up successful for: " + username);
                        signUpMessageTextView.setText("Signed up successfully");
                        signUpMessageTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        signUpMessageTextView.setVisibility(View.VISIBLE);
                        signUpMessageTextView.postDelayed(() -> {
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }, 1500); // 1.5-second delay
                        break;
                    case USERNAME_TAKEN:
                        Log.d("SignUpDebug", "Username already exists: " + username);
                        signUpMessageTextView.setText("Username already exists");
                        signUpMessageTextView.setVisibility(View.VISIBLE);
                        break;
                    case FAIL:
                        Log.d("SignUpDebug", "Signup failed: " + username);
                        signUpMessageTextView.setText("Error encountered! Please try again");
                        signUpMessageTextView.setVisibility(View.VISIBLE);
                }
            });
        });

        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }
}
