package com.example.moodmento;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.futuredevs.database.Database;
import com.futuredevs.database.UserDetails;

public class MainActivity extends AppCompatActivity {
    /** Field for entering the username when logging in. */
    private EditText usernameEditText;
    /** Field for entering the password when logging in. */
    private EditText passwordEditText;
    /** Button to attempt a login with the provided credentials. */
    private Button loginButton;
    /**
     * TextView for displaying login error messages.
     * This is reused from the sign-up layout to provide consistent error display.
     */
    private TextView signUpMessageTextView;
    private ProgressBar loginIndicator;

	/**
     * Called when the activity is created. Initializes the login UI fields,
     * checks location permissions, and sets up button listeners for login
     * and sign-up navigation.
     *
     * @param savedInstanceState The previously saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We are reusing the sign-up layout for login purposes.
        this.setContentView(R.layout.activity_sign_up);
        EdgeToEdge.enable(this);

        // Edge-to-edge display adjustments.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components.
        this.loginButton = this.findViewById(R.id.signUpButton);
        this.loginButton.setEnabled(false);
        this.signUpMessageTextView = this.findViewById(R.id.signUpMessageTextView);
        this.usernameEditText = this.findViewById(R.id.signUpUsernameEditText);
        this.passwordEditText = this.findViewById(R.id.signUpPasswordEditText);
        TextWatcher namePasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                loginButton.setEnabled(canLogin());
            }
        };
        this.usernameEditText.addTextChangedListener(namePasswordWatcher);
        this.passwordEditText.addTextChangedListener(namePasswordWatcher);
        this.loginIndicator = this.findViewById(R.id.progress_login_signup);

        // Because we reuse the sign-up layout for login, update some text fields.
        TextView signupTitle = this.findViewById(R.id.signUpTitleTextView);
        signupTitle.setText("Login");
        this.loginButton.setText("Login");
        this.loginButton.setOnClickListener(v -> loginUser());

        TextView signUpTextView = this.findViewById(R.id.loginTextView);
        signUpTextView.setText("Sign-up");
        signUpTextView.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });
    }

    /**
     * Collects the username and password from the UI and attempts to validate
     * them using the {@link Database}. If valid, the user is navigated to
     * {@link HomeActivity}; otherwise an error message is displayed in the UI.
     */
    private void loginUser() {
        this.loginIndicator.setVisibility(View.VISIBLE);
        this.loginButton.setEnabled(false);
        String username = this.usernameEditText.getText().toString().trim();
        String password = this.passwordEditText.getText().toString().trim();

        this.signUpMessageTextView.setText("");
        this.signUpMessageTextView.setVisibility(TextView.GONE);

        // Attempt to validate login credentials.
        UserDetails userDetails = new UserDetails(username, password);
        Database.getInstance().validateLogin(userDetails, result -> {
            switch (result) {
                case SUCCEED:
                    Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    // Set the current user in the Database for app-wide access.
                    Database.getInstance().setCurrentUser(username);
                    loadApp();
                    break;
                case INVALID_DETAILS:
                    signUpMessageTextView.setText("Invalid username or password");
                    signUpMessageTextView.setVisibility(TextView.VISIBLE);
                    loginIndicator.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    break;
                case FAIL:
                    signUpMessageTextView.setText("Error encountered! Please try again");
                    signUpMessageTextView.setVisibility(TextView.VISIBLE);
                    loginIndicator.setVisibility(View.GONE);
                    loginButton.setEnabled(true);
                    break;
            }
        });
    }

    /**
     * Navigates to HomeActivity after a successful login, clearing this
     * activity from the back stack.
     */
    private void loadApp() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Returns whether the user can attempt to login with the details entered
     * in the text fields.
     *
     * @return {@code true} if the user has entered a username and password,
     *         {@code false} otherwise
     */
    private boolean canLogin() {
        boolean usernameFilled = !this.usernameEditText.getText().toString().isEmpty();
        boolean passwordFilled = !this.passwordEditText.getText().toString().isEmpty();
        return usernameFilled && passwordFilled;
    }
}