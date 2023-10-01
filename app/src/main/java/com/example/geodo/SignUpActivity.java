package com.example.geodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText usernameText = (EditText) findViewById(R.id.editTextEmail);
        EditText passwordText = (EditText) findViewById(R.id.editTextPassword);
        EditText confirmPasswordText = (EditText) findViewById(R.id.editTextPasswordConfirm);
        TextView invalidPassword = (TextView) findViewById(R.id.textViewInvalidPassword);
        Button regButton = (Button) findViewById(R.id.buttonCreateAccount);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();

                // Check if passwords match and are valid
                if (password.equals(confirmPassword) && checkPassword(password) && checkEmail(email)) {
                    signUp(email, password);
                } else {
                    if(!password.equals(confirmPassword)) {
                        Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                    } else {
                        invalidPassword.setText("Invalid Password");
                    }
                }
                if(!checkEmail(email)) {
                    invalidPassword.setText("Invalid Email");
                }
            }
        });
    }

    public void signUp(String username, String password) {

        ParseUser user = new ParseUser();

        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e == null) {
                Toast.makeText(SignUpActivity.this, "User registered", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);

            } else {
                ParseUser.logOut();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Check if password is valid
    public boolean checkPassword(String password) {
        boolean isValid = true;
        if (password.length() < 8) {
            isValid = false;
        }
        if (!password.matches(".*[A-Z].*")) {
            isValid = false;
        }
        if (!password.matches(".*[a-z].*")) {
            isValid = false;
        }
        if (!password.matches(".*\\d.*")) {
            isValid = false;
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            isValid = false;
        }
        return isValid;
    }

    // Check if email is valid
    public boolean checkEmail(String email) {
        boolean isValid = true;
        if (!email.contains("@")) {
            isValid = false;
        }
        if (!email.contains(".")) {
            isValid = false;
        }
        return isValid;
    }
}
