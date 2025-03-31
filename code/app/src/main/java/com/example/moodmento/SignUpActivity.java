package com.example.moodmento;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;

/**

 SignUpActivity handles user registration by capturing username and password input,
 checking for existing usernames in Firestore via the Database helper,
 and giving feedback based on the outcome of the registration attempt.
 <p>This activity ensures proper validation of input, displays loading indicators,
 and transitions to MainActivity upon successful sign-up.</p>
 <p>Dependencies: Firebase Firestore, custom `Database` and `UserDetails` classes.</p>
 @author Israel
 */

public class SignUpActivity extends AppCompatActivity {
    private EditText signUpUsernameEditText;
    private EditText signUpPasswordEditText;
    private Button signUpButton;
    private TextView signUpMessageTextView;
	private ProgressBar signupIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_sign_up);

        this.signupIndicator = this.findViewById(R.id.progress_login_signup);
        this.signUpMessageTextView = this.findViewById(R.id.signUpMessageTextView);

        this.signUpUsernameEditText = this.findViewById(R.id.signUpUsernameEditText);
        this.signUpPasswordEditText = this.findViewById(R.id.signUpPasswordEditText);
        TextWatcher namePasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                signUpButton.setEnabled(canSignup());
            }
        };
        this.signUpUsernameEditText.addTextChangedListener(namePasswordWatcher);
        this.signUpPasswordEditText.addTextChangedListener(namePasswordWatcher);

        this.signUpButton = this.findViewById(R.id.signUpButton);
        this.signUpButton.setEnabled(false);
        this.signUpButton.setOnClickListener(v -> {
            signUpButton.setEnabled(false);
            signupIndicator.setVisibility(View.VISIBLE);
            String username = signUpUsernameEditText.getText().toString().trim();
            String password = signUpPasswordEditText.getText().toString().trim();

            // Reset the message for new attempts
            signUpMessageTextView.setText("");
            signUpMessageTextView.setVisibility(View.GONE);

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
                            signupIndicator.setVisibility(View.GONE);
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }, 1500); // 1.5-second delay
                        break;
                    case USERNAME_TAKEN:
                        Log.d("SignUpDebug", "Username already exists: " + username);
                        signUpMessageTextView.setText("Username already exists");
                        signUpMessageTextView.setVisibility(View.VISIBLE);
                        signUpButton.setEnabled(true);
                        signupIndicator.setVisibility(View.GONE);
                        break;
                    case FAIL:
                        Log.d("SignUpDebug", "Signup failed: " + username);
                        signUpMessageTextView.setText("Error encountered! Please try again");
                        signUpMessageTextView.setVisibility(View.VISIBLE);
                        signUpButton.setEnabled(true);
                        signupIndicator.setVisibility(View.GONE);
                }
            });
        });

        TextView loginTextView = this.findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            finish();
        });
    }

    /**
     * Returns whether the user can attempt to sign up with the currently
     * entered details.
     *
     * @return {@code true} if both the username and password fields are
     *         filled, {@code false} otherwise
     */
    private boolean canSignup() {
        boolean userFilled = !this.signUpUsernameEditText.getText().toString().isEmpty();
        boolean passwordFilled = !this.signUpPasswordEditText.getText().toString().isEmpty();
        return userFilled && passwordFilled;
    }
}
