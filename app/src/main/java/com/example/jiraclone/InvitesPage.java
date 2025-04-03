package com.example.jiraclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvitesPage extends AppCompatActivity {

    private RecyclerView invitesRecyclerView;
    private InvitesAdapter invitesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites_page);

        invitesRecyclerView = findViewById(R.id.invitesRecyclerView);
        invitesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            fetchUserInvites(userEmail);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.nav_invites) {
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(InvitesPage.this, AccPage.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_projects) {
                Intent intent = new Intent(InvitesPage.this, project_tab.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
    }

    private void fetchUserInvites(String userEmail) {
        String sanitizedEmail = userEmail.replace(".", ",");

        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invites").child(sanitizedEmail);
        invitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<InviteItem> inviteList = new ArrayList<>();
                List<Task<DataSnapshot>> tasks = new ArrayList<>();  // To collect all fetch tasks

                if (snapshot.exists()) {
                    for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                        String projectId = projectSnapshot.getKey();

                        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference("projects").child(projectId);
                        Task<DataSnapshot> fetchTask = projectRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DataSnapshot projSnapshot = task.getResult();
                                if (projSnapshot.exists()) {
                                    Map<String, Object> value = (Map<String, Object>) projSnapshot.getValue();
                                    String name = value.get("name").toString();
                                    String description = value.get("description").toString();
                                    inviteList.add(new InviteItem(name, description));
                                }
                            }
                        });
                        tasks.add(fetchTask);
                    }
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                    if (!inviteList.isEmpty()) {
                        invitesAdapter = new InvitesAdapter(inviteList);
                        invitesRecyclerView.setAdapter(invitesAdapter);
                    } else {
                        Toast.makeText(InvitesPage.this, "No invites found", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvitesPage.this, "Failed to load invites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
