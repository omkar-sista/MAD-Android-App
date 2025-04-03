package com.example.jiraclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccPage extends AppCompatActivity {
    private LinearLayout logout;
    private FirebaseAuth mAuth;
    private TextView userNameTextView, userEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.acc_page);

        mAuth = FirebaseAuth.getInstance();
        logout = findViewById(R.id.logoutButton);
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                String username = email.split("@")[0];
                userNameTextView.setText(username);
                userEmailTextView.setText(email);
            }
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(AccPage.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_account);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_invites) {
                Intent intent = new Intent(AccPage.this, InvitesPage.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_account) {
                return true;
            } else if (menuItem.getItemId() == R.id.nav_projects) {
                Intent intent = new Intent(AccPage.this, project_tab.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
    }
}
