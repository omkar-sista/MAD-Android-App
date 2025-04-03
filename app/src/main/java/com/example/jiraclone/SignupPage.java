package com.example.jiraclone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupPage extends AppCompatActivity {
    private EditText email, password, confirmPassword;
    private Button signup;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        signup = findViewById(R.id.signup);
        loginLink = findViewById(R.id.loginLink);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();
                String confirmPasswordText = confirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(emailText)) {
                    Toast.makeText(SignupPage.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(passwordText)) {
                    Toast.makeText(SignupPage.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordText.equals(confirmPasswordText)) {
                    Toast.makeText(SignupPage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Toast.makeText(SignupPage.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupPage.this, project_tab.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(SignupPage.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupPage.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the sign-up page
            }
        });
    }
}
