package com.example.jiraclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.widget.TextView;

public class project_tab extends AppCompatActivity implements ProjectAdapter.OnProjectClickListener {

    private List<Project> projectList = new ArrayList<>();
    private List<Project> recentProjectList = new ArrayList<>();
    private List<Project> filteredProjectList = new ArrayList<>();

    private ProjectAdapter projectAdapter;
    private ProjectAdapter recentProjectAdapter;
    private ProjectAdapter filteredProjectAdapter;

    private DatabaseReference databaseReference;

    private ValueEventListener valueEventListener;

    private EditText searchEditText;
    private TextView textViewAllProjects, textViewRecentlyViewed;
    private RecyclerView projectRecyclerView;
    private RecyclerView recentlyViewedRecyclerView;
    private RecyclerView filteredProjectRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_project_tab);

        databaseReference = FirebaseDatabase.getInstance().getReference("projects");

        projectRecyclerView = findViewById(R.id.projectRecyclerView);
        projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        projectAdapter = new ProjectAdapter(projectList, this);
        projectRecyclerView.setAdapter(projectAdapter);

        recentlyViewedRecyclerView = findViewById(R.id.recentlyViewedRecyclerView);
        recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentProjectAdapter = new ProjectAdapter(recentProjectList, this);
        recentlyViewedRecyclerView.setAdapter(recentProjectAdapter);

        filteredProjectRecyclerView = findViewById(R.id.filteredProjectRecyclerView);
        filteredProjectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filteredProjectAdapter = new ProjectAdapter(filteredProjectList, this);
        filteredProjectRecyclerView.setAdapter(filteredProjectAdapter);

        textViewAllProjects = findViewById(R.id.textView5);
        textViewRecentlyViewed = findViewById(R.id.textView2);

        filteredProjectRecyclerView.setVisibility(View.GONE);

        loadUserProjects();
        loadRecentProjectsLocally();

        Button addProjectButton = findViewById(R.id.addProject);
        addProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(project_tab.this, proj_create.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_projects);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_invites) {
                Intent intent = new Intent(project_tab.this, InvitesPage.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_projects) {
                return true;

            } else if (menuItem.getItemId() == R.id.nav_account) {
                Intent intent = new Intent(project_tab.this, AccPage.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    textViewAllProjects.setVisibility(View.GONE);
                    textViewRecentlyViewed.setVisibility(View.GONE);
                    projectRecyclerView.setVisibility(View.GONE);
                    recentlyViewedRecyclerView.setVisibility(View.GONE);
                    filteredProjectRecyclerView.setVisibility(View.VISIBLE);
                    searchProjects(s.toString()); // Call search method as user types
                } else {
                    textViewAllProjects.setVisibility(View.VISIBLE);
                    textViewRecentlyViewed.setVisibility(View.VISIBLE);
                    projectRecyclerView.setVisibility(View.VISIBLE);
                    recentlyViewedRecyclerView.setVisibility(View.VISIBLE);
                    filteredProjectRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchProjects(String query) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            Query searchQuery;
            if (TextUtils.isEmpty(query)) {
                searchQuery = databaseReference.orderByChild("userEmail").equalTo(userEmail);
            } else {
                searchQuery = databaseReference.orderByChild("name").startAt(query).endAt(query + "\uf8ff");
            }

            searchQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    filteredProjectList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Project project = snapshot.getValue(Project.class);
                        if (project != null && project.getUserEmail().equals(userEmail)) {
                            filteredProjectList.add(project);
                        }
                    }
                    filteredProjectAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(project_tab.this, "Failed to load projects: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    @Override
    public void onProjectClick(String projectId) {
        storeRecentProjectLocally(projectId); // Store the recent project
        Intent intent = new Intent(this, ProjectDetailsActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        startActivity(intent);
    }

    private void loadUserProjects() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            Query userProjectsQuery = databaseReference;
            valueEventListener = userProjectsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    projectList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Project project = snapshot.getValue(Project.class);
                        if (project != null) {
                            DataSnapshot membersSnapshot = snapshot.child("members");
                            if (membersSnapshot.exists()) {
                                boolean isMember = false;
                                for (DataSnapshot member : membersSnapshot.getChildren()) {
                                    String memberEmail = member.getValue(String.class);
                                    if (userEmail.equals(memberEmail)) {
                                        isMember = true;
                                        break;
                                    }
                                }
                                if (isMember) {
                                    projectList.add(project);
                                }
                            }
                        }
                    }
                    projectAdapter.notifyDataSetChanged();
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(project_tab.this, "Failed to load projects: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecentProjectsLocally() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            SharedPreferences sharedPreferences = getSharedPreferences("RecentProjects", MODE_PRIVATE);
            String recentProjects = sharedPreferences.getString("recent_projects", "");

            if (!recentProjects.isEmpty()) {
                String[] recentProjectIds = recentProjects.split(",");

                List<String> limitedRecentProjectIds = Arrays.asList(recentProjectIds).subList(0, Math.min(recentProjectIds.length, 3));

                for (String projectId : limitedRecentProjectIds) {
                    databaseReference.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Project project = dataSnapshot.getValue(Project.class);
                            if (project != null && project.getUserEmail() != null ) {
                                recentProjectList.add(project);
                                recentProjectAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeRecentProjectLocally(String projectId) {
        SharedPreferences sharedPreferences = getSharedPreferences("RecentProjects", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String recentProjects = sharedPreferences.getString("recent_projects", "");

        List<String> recentProjectList = new ArrayList<>(Arrays.asList(recentProjects.split(",")));

        recentProjectList.remove(projectId);
        recentProjectList.add(0, projectId);

        if (recentProjectList.size() > 3) {
            recentProjectList = recentProjectList.subList(0, 3);
        }

        editor.putString("recent_projects", TextUtils.join(",", recentProjectList));
        editor.apply();
    }

}
