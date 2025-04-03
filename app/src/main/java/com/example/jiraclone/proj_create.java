package com.example.jiraclone;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class proj_create extends AppCompatActivity {

    private int defaultColor;
    private View colorDisplay;
    EditText projectDescInput, projectNameInput;
    TextView descInputCount, nameInputCount;
    Button createProjectButton, colorPickerButton;
    ImageButton backToProjects;
    private DatabaseReference databaseProjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.proj_create);

        colorDisplay = findViewById(R.id.colorDisplay);
        colorPickerButton = findViewById(R.id.colorPickerButton);
        backToProjects = findViewById(R.id.backToProjects);
        projectNameInput = findViewById(R.id.projectName);
        projectDescInput = findViewById(R.id.projectDescription);
        createProjectButton = findViewById(R.id.createProjectButton);

        nameInputCount = findViewById(R.id.projectNameCount);
        descInputCount = findViewById(R.id.projectDescriptionCount);

        databaseProjects = FirebaseDatabase.getInstance().getReference("projects");

        defaultColor = 0xFFFFFF;

        colorPickerButton.setOnClickListener(v -> openColorPickerDialog());

        createProjectButton.setOnClickListener(v ->
            addProjectToFirebase());

        backToProjects.setOnClickListener(v -> {
            Intent intent = new Intent(proj_create.this, project_tab.class);
            startActivity(intent);
        });

        projectNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameInputCount.setText(s.length() + "/50");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        projectDescInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                descInputCount.setText(s.length() + "/255");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openColorPickerDialog() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;
                colorDisplay.setBackgroundColor(color);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                Toast.makeText(proj_create.this, "Color picking cancelled", Toast.LENGTH_SHORT).show();
            }
        });
        colorPicker.show();
    }

    private void addProjectToFirebase() {
        String projectName = projectNameInput.getText().toString().trim();
        String projectDesc = projectDescInput.getText().toString().trim();
        String colorString = String.format("#%06X", (0xFFFFFF & defaultColor));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : null;

        if (userEmail == null){
            Toast.makeText(proj_create.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!projectName.isEmpty() && !projectDesc.isEmpty()) {
            if (projectDesc.length() <= 255) {
                String projectId = databaseProjects.push().getKey();
                ArrayList<String> mem = new ArrayList<>();
                mem.add(userEmail);
                Project newProject = new Project(projectId, projectName, projectDesc, colorString, userEmail, mem);
                assert projectId != null;
                databaseProjects.child(projectId).setValue(newProject);

                Toast.makeText(proj_create.this, "Project created", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(proj_create.this, project_tab.class);
                intent.putExtra("projectId", projectId);
                startActivity(intent);
                finish();
                Toast.makeText(proj_create.this, "Project created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(proj_create.this, "Description must be less than 256 characters", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(proj_create.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        }

    }
}