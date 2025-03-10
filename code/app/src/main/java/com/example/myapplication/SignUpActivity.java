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

import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;

/**
 * SignUpActivity is responsible for creating new user accounts in the system.
 * <p>
 * This activity allows a user to provide a username and password, then checks
 * whether the username is unique before creating a new account in the database.
 * It thus fulfills the requirement of US 03.01.01 ("As a user, I want a profile
 * with a unique username"). If the chosen username is already taken, it notifies
 * the user to pick a different one. Otherwise, it creates the account and
 * navigates back to the login screen.
 * </p>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * <ul>
 *   <li>Collect username and password from user input.</li>
 *   <li>Verify the uniqueness of the username using Firestore via {@link Database}.</li>
 *   <li>Create a new user profile if valid, or prompt the user to try again.</li>
 *   <li>Show error/success messages to guide user actions.</li>
 * </ul>
 * </p>
 *
 * @author
 *   [Your Name / Team Name]
 * @version
 *   1.0
 */
public class SignUpActivity extends AppCompatActivity {

    /** Field for entering a desired username. */
    private EditText signUpUsernameEditText;

    /** Field for entering a desired password. */
    private EditText signUpPasswordEditText;

    /** Button to initiate the sign-up process. */
    private Button signUpButton;

    /** Displays status messages (e.g., "Username taken", "Signed up successfully"). */
    private TextView signUpMessageTextView;

    /** Text link that allows a user to switch to the login screen. */
    private TextView loginTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Initialize all UI components.
        signUpUsernameEditText = findViewById(R.id.signUpUsernameEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signUpMessageTextView = findViewById(R.id.signUpMessageTextView);
        loginTextView = findViewById(R.id.loginTextView);

        // Listen for "Sign-up" button clicks.
        signUpButton.setOnClickListener(this::handleSignUp);

        // If the user already has an account, navigate to the login screen.
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }

    /**
     * Called when the user presses the "Sign-up" button.
     * <p>
     * This method collects the username and password from the EditText fields,
     * then attempts to create a new account via {@link Database#attemptSignup}.
     * If the username is unique, the user is notified of success and navigated
     * back to the login screen. If it is taken or an error occurs, a descriptive
     * message is displayed.
     * </p>
     *
     * @param view the view that triggered this click event (the Sign-up button)
     */
    private void handleSignUp(View view) {
        String username = signUpUsernameEditText.getText().toString().trim();
        String password = signUpPasswordEditText.getText().toString().trim();

        // Clear any previous message.
        signUpMessageTextView.setText("");
        signUpMessageTextView.setVisibility(View.GONE);

        // Validate user input.
        if (username.isEmpty() || password.isEmpty()) {
            signUpMessageTextView.setText("Enter username & password");
            signUpMessageTextView.setVisibility(View.VISIBLE);
            return;
        }

        // Attempt to sign up the user in the Firestore database.
        UserDetails userDetails = new UserDetails(username, password);
        Database.getInstance().attemptSignup(userDetails, result -> {
            switch (result) {
                case SUCCEED:
                    // Username was unique and the account was created successfully.
                    signUpMessageTextView.setText("Signed up successfully");
                    signUpMessageTextView.setTextColor(
                            getResources().getColor(android.R.color.holo_green_dark)
                    );
                    signUpMessageTextView.setVisibility(View.VISIBLE);

                    // Brief delay before redirecting to login screen.
                    signUpMessageTextView.postDelayed(() -> {
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    }, 1500);
                    break;

                case USERNAME_TAKEN:
                    // The chosen username already exists.
                    signUpMessageTextView.setText("Username already exists");
                    signUpMessageTextView.setVisibility(View.VISIBLE);
                    break;

                case FAIL:
                default:
                    // Some other error occurred (e.g., network issues).
                    signUpMessageTextView.setText("Error encountered! Please try again");
                    signUpMessageTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}
