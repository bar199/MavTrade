package com.example.mavtrade;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            loginUser(username, password);
        });

        btnRegister.setOnClickListener(v -> {
            Log.i(TAG, "onClick signup button");
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            registerUser(username, password);
        });

    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);

        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with login", e);
                displayToast("Issue with login!");
                return;
            }
            // Navigate to the main activity if the user has signed in properly
            goMainActivity();
        });
    }

    private void registerUser(String username, String password) {
        Log.i(TAG, "Attempting to signup user " + username);

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(e -> {
            if (e != null) {
                switch (e.getCode()) {
                    case ParseException.USERNAME_TAKEN: {
                        Log.e(TAG, "Username already taken", e);
                        displayToast("Username already taken!");
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