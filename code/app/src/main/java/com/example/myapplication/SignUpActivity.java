package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText signUpUsernameEditText;
    private EditText signUpPasswordEditText;
    private Button signUpButton;
    private TextView loginTextView; // "Login" link at bottom

    // Firestore instance for storing user credentials
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find Views by their IDs
        signUpUsernameEditText = findViewById(R.id.signUpUsernameEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Sign-up button click listener
        signUpButton.setOnClickListener((View v) -> {
            String username = signUpUsernameEditText.getText().toString().trim();
            String password = signUpPasswordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Enter username & password", Toast.LENGTH_SHORT).show();
            } else {
                // Check if the username already exists in Firestore
                db.collection("users").document(username).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Toast.makeText(SignUpActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Username does not exist; create a new user document.
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username);
                                    user.put("password", password); // Note: In production, do not store plain-text passwords!

                                    db.collection("users").document(username).set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SignUpActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                                                // Navigate back to the Login screen after successful sign-up
                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SignUpActivity.this, "Error signing up: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // "Login" link click listener - navigate back to MainActivity
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }
}