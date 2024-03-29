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

public class SignInActivity extends AppCompatActivity {
TextView invalidText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        EditText usernameText = (EditText) findViewById(R.id.editTextUsername);
        EditText passwordText = (EditText) findViewById(R.id.editTextSignInPassword);
        invalidText = (TextView) findViewById(R.id.textViewInvalid);
        Button buttonLogIn = (Button) findViewById(R.id.buttonLogIn);

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = usernameText.getText().toString();
                String password = passwordText.getText().toString();
                userLogin(userName, password);
            }
        });
    }

    public void userLogin(String UserName, String Password){
        ParseUser.logInInBackground(UserName, Password, (parseUser, e) -> {
           if(parseUser != null){
               Toast.makeText(SignInActivity.this, "Login Successful, Welcome Back! " + UserName + "!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.putExtra("Name", UserName);
                startActivity(intent);
           }else{
               ParseUser.logOut();
               invalidText.setText("Invalid Username or Password");
           }
        });
    }

    public void GoToSignUp(View view){
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}