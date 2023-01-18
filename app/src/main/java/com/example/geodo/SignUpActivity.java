package com.example.geodo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText usernameText = (EditText) findViewById(R.id.editTextEmail);
        EditText passwordText = (EditText) findViewById(R.id.editTextPassword);
        EditText confirmPasswordText = (EditText) findViewById(R.id.editTextPasswordConfirm);
        Button regButton = (Button) findViewById(R.id.buttonCreateAccount);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();

                if(password.equals(confirmPassword)){
                    signUp(email, password);
                }else{
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void signUp(String username, String password){

        ParseUser user = new ParseUser();

        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if(e == null){
                Toast.makeText(SignUpActivity.this, "User registered", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);

            }else{
                ParseUser.logOut();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}