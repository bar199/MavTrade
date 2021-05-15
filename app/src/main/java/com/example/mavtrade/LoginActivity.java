package com.example.mavtrade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;
    private ToggleButton tbtnLogin;
    private TextView tvEmailInfo;

    private Boolean goodEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tbtnLogin = findViewById(R.id.tbtnLogin);
        tvEmailInfo = findViewById(R.id.tvEmailInfo);

        etEmail.addTextChangedListener(onEmailChangedListener());

        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            etUsername.setError(null);
            etEmail.setError(null);

            if (username != null && password != null) {
                loginUser(username, password);

            } else {
                if (username == null) {
                    etUsername.setError("This field cannot be blank");
                }

                if (password == null) {
                    etPassword.setError("This field cannot be blank");
                }
            }
        });

        btnRegister.setOnClickListener(v -> {
            Log.i(TAG, "onClick signup button");
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            etUsername.setError(null);
            etEmail.setError(null);
            etPassword.setError(null);

            if (username != null && password != null && goodEmail) {
                registerUser(username, email, password);

            } else {
                if (username == null) {
                    etUsername.setError("This field cannot be blank");
                }

                if (email == null) {
                    etEmail.setError("This field cannot be blank");
                }

                if (password == null) {
                    etPassword.setError("This field cannot be blank");
                }
            }
        });

        tbtnLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etEmail.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.VISIBLE);
                    tvEmailInfo.setVisibility(View.VISIBLE);

                } else {
                    etEmail.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.GONE);
                    tvEmailInfo.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);

        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Invalid username/password", e);
                displayToast("Invalid username/password");
                return;
            }
            // Navigate to the main activity if the user has signed in properly
            goMainActivity();
        });
    }

    private void registerUser(String username, String email, String password) {
        Log.i(TAG, "Attempting to signup user " + username);

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        user.signUpInBackground(e -> {
            if (e != null) {
                switch (e.getCode()) {
                    case ParseException.USERNAME_TAKEN: {
                        Log.e(TAG, "Username already taken", e);
                        etUsername.setError("Username already taken");
                        break;
                    }

                    case ParseException.EMAIL_TAKEN: {
                        Log.e(TAG, "Already existing account with this email", e);
                        etEmail.setError("Already existing account with this email");
                        break;
                    }

                    default: {
                        Log.e(TAG, "Issue with registration", e);
                        displayToast("Issue with registration!");
                    }
                }

                return;
            }

            // Navigate to the main activity if the user has signed in properly
            goMainActivity();
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private TextWatcher onEmailChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().isEmpty()) {
                    goodEmail = false;
                    etEmail.setError("This field cannot be blank");

                } else {
                    etEmail.setError(null);
                    String newEmail = s.toString().trim();
                    String mavEmail = "@mavs.uta.edu";

                    Boolean isValidEmail = (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches());

                    if (isValidEmail) {
                        if (newEmail.contains(mavEmail)) {
                            goodEmail = true;
                        } else {
                            goodEmail = false;
                            etEmail.setError("Email must be a \'@mavs.uta.edu\' email");
                        }

                    } else {
                        goodEmail = false;
                        etEmail.setError("Not a valid email address");
                    }
                }
            }
        };
    }

    //Custom toast
    private void displayToast(String message) {
        // Inflate toast XML layout
        View layout = getLayoutInflater().inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        // Fill in the message into the textview
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        // Construct the toast, set the view and display
        Toast toast = new Toast(getApplicationContext());
        toast.setView(layout);
        toast.show();
    }
}