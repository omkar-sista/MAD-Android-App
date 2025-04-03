package com.example.jiraclone;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectDetailsActivity extends AppCompatActivity {

    private ImageButton backArrowButton;
    private RecyclerView todoRecyclerView, doneRecyclerView, inProgressRecyclerView;
    private TicketAdapter todoAdapter, doneAdapter, inProgressAdapter;
    private List<Ticket> todoList, doneList, inProgressList;
    private TextView todoCountTextView, doneCountTextView, inProgressCountTextView; // TextView to display the To Do count
    private String ticketStatus;

    private DatabaseReference databaseReference;
    private String projectId;

    private Button inviteButton, settingsButton, boardButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_project_details);

        projectId = getIntent().getStringExtra("PROJECT_ID");

        if (projectId == null) {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("projects").child(projectId).child("tickets");

        String projectName = getIntent().getStringExtra("PROJECT_NAME");
        String projectDescription = getIntent().getStringExtra("PROJECT_DESCRIPTION");

        TextView projectNameTextView = findViewById(R.id.projectTitle);
        projectNameTextView.setText(projectName);

        todoCountTextView = findViewById(R.id.todoCount);
        doneCountTextView = findViewById(R.id.doneCount);
        inProgressCountTextView = findViewById(R.id.inProgressCount);

        todoList = new ArrayList<>();
        doneList = new ArrayList<>();
        inProgressList = new ArrayList<>();

        todoAdapter = new TicketAdapter(todoList, true);
        doneAdapter = new TicketAdapter(doneList, true);
        inProgressAdapter = new TicketAdapter(inProgressList, true);

        todoRecyclerView = findViewById(R.id.todoRecyclerView);
        doneRecyclerView = findViewById(R.id.doneRecyclerView);
        inProgressRecyclerView = findViewById(R.id.inProgressRecyclerView);

        todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        doneRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inProgressRecyclerView.setLayoutManager(new LinearLayoutManager( this));

        todoRecyclerView.setAdapter(todoAdapter);
        doneRecyclerView.setAdapter(doneAdapter);
        inProgressRecyclerView.setAdapter(inProgressAdapter);

        // Back button to return to the project list
        backArrowButton = findViewById(R.id.backArrow);
        backArrowButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectDetailsActivity.this, project_tab.class);
            startActivity(intent);
        });

        settingsButton = findViewById(R.id.settingsButton);
        boardButton = findViewById(R.id.boardButton);
        inviteButton = findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(v -> showInviteDialog());
        
        // Bottom navigation menu setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_projects);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_invites) {
                Intent intent = new Intent(ProjectDetailsActivity.this, InvitesPage.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_projects) {
                Intent intent = new Intent(ProjectDetailsActivity.this, project_tab.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_account) {
                Intent intent = new Intent(ProjectDetailsActivity.this, AccPage.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        findViewById(R.id.createTodoButton).setOnClickListener(v -> showCreateTicketDialog());

        loadProjectTickets();

        doneRecyclerView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;

                    case DragEvent.ACTION_DROP:
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String ticketId = item.getText().toString();

                        moveTicketToDone(ticketId);
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;

                    default:
                        return false;
                }
            }
        });
        todoRecyclerView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;

                    case DragEvent.ACTION_DROP:
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String ticketId = item.getText().toString();

                        moveTicketToToDo(ticketId);
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;

                    default:
                        return false;
                }
            }
        });



        inProgressRecyclerView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;

                    case DragEvent.ACTION_DROP:
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String ticketId = item.getText().toString();

                        moveTicketToInProgress(ticketId);
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;

                    default:
                        return false;
                }
            }
        });
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> showEditProjectDialog());
    }

    private void showInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_invite, null);
        builder.setView(dialogView).setTitle("Invite User");

        EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        Button inviteButton1 = dialogView.findViewById(R.id.inviteButton1);

        AlertDialog dialog = builder.create();
        dialog.show();

        inviteButton1.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (isValidEmail(email)) {
                saveInvite(email, projectId);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void saveInvite(String email, String projectId) {
        // Sanitize the email to replace dots with commas
        String sanitizedEmail = email.replace(".", ",");

        DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference("invites");
        DatabaseReference emailRef = invitesRef.child(sanitizedEmail);
        emailRef.child(projectId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Invite sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send invite", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadProjectTickets() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                todoList.clear();
                doneList.clear();
                inProgressList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ticket ticket = snapshot.getValue(Ticket.class);
                    if (ticket != null) {
                        if ("toDo".equals(ticket.getStatus())) {
                            todoList.add(ticket);
                        } else if ("done".equals(ticket.getStatus())) {
                            doneList.add(ticket);
                        }
                        else if ("inProgress".equals(ticket.getStatus())) {
                            inProgressList.add(ticket);
                        }
                    }
                }
                todoAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
                inProgressAdapter.notifyDataSetChanged();
                updateTodoCount();
                updateDoneCount();
                updateinProgressCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProjectDetailsActivity.this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveTicketToDone(String ticketId) {

        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = todoList.get(i);
                movedTicket.setStatus("done");

                databaseReference.child(ticketId).setValue(movedTicket);

                doneList.add(movedTicket);
                todoList.remove(i);

                todoAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
                updateTodoCount();
                updateDoneCount();
                break;
            }
        }


        for (int i = 0; i < inProgressList.size(); i++) {
            if (inProgressList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = inProgressList.get(i);
                movedTicket.setStatus("done");
                databaseReference.child(ticketId).setValue(movedTicket);
                doneList.add(movedTicket);
                inProgressList.remove(i);
                inProgressAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
                updateinProgressCount();
                updateDoneCount();
                break;
            }
        }
    }

    private void moveTicketToToDo(String ticketId) {
        for (int i = 0; i < doneList.size(); i++) {
            if (doneList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = doneList.get(i);
                movedTicket.setStatus("toDo");

                databaseReference.child(ticketId).setValue(movedTicket);

                todoList.add(movedTicket);
                doneList.remove(i);

                todoAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
                updateTodoCount();
                updateDoneCount();
                break;
            }
        }
        for (int i = 0; i < inProgressList.size(); i++) {
            if (inProgressList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = inProgressList.get(i);
                movedTicket.setStatus("toDo");
                databaseReference.child(ticketId).setValue(movedTicket);
                todoList.add(movedTicket);
                inProgressList.remove(i);
                inProgressAdapter.notifyDataSetChanged();
                todoAdapter.notifyDataSetChanged();
                updateinProgressCount();
                updateTodoCount();
                break;
            }
        }    }
    private void moveTicketToInProgress(String ticketId) {
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = todoList.get(i);
                movedTicket.setStatus("inProgress");

                databaseReference.child(ticketId).setValue(movedTicket);

                inProgressList.add(movedTicket);
                todoList.remove(i);

                todoAdapter.notifyDataSetChanged();
                inProgressAdapter.notifyDataSetChanged();
                updateTodoCount();
                updateinProgressCount();
                break;
            }
        }
        for (int i = 0; i < doneList.size(); i++) {
            if (doneList.get(i).getTicketId().equals(ticketId)) {
                Ticket movedTicket = doneList.get(i);
                movedTicket.setStatus("inProgress");

                databaseReference.child(ticketId).setValue(movedTicket);

                inProgressList.add(movedTicket);
                doneList.remove(i);

                inProgressAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
                updateinProgressCount();
                updateDoneCount();
                break;
            }
        }
    }

    private void createTicket(String title, String description, String dateAssigned) {
        if (projectId == null) {
            Toast.makeText(this, "Project ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference("projects").child(projectId).child("tickets");

        String ticketId = projectRef.push().getKey();

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        Ticket newTicket = new Ticket(ticketId, title, description, currentDate,dateAssigned, "toDo", projectId);// Pass the date

        projectRef.child(ticketId).setValue(newTicket).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Ticket created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateTicketDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_ticket, null);
        builder.setView(dialogView)
                .setTitle("Create Ticket");

        EditText ticketTitleEditText = dialogView.findViewById(R.id.ticketTitleEditText);
        EditText ticketDescriptionEditText = dialogView.findViewById(R.id.ticketDescriptionEditText);
        TextView ticketDateAssignedTextView = dialogView.findViewById(R.id.ticketDateAssignedTextView);

        Button createButton = dialogView.findViewById(R.id.createTicketButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        ticketDateAssignedTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        ticketDateAssignedTextView.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        createButton.setOnClickListener(v -> {
            String title = ticketTitleEditText.getText().toString().trim();
            String description = ticketDescriptionEditText.getText().toString().trim();
            String dateAssigned = ticketDateAssignedTextView.getText().toString().trim();
            if (!title.isEmpty() && !description.isEmpty()) {
                createTicket(title, description, dateAssigned);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTodoCount() {
        int todoCount = todoList.size();
        todoCountTextView.setText(String.valueOf(todoCount));
    }
    private void updateDoneCount() {
        int doneCount = doneList.size();
        doneCountTextView.setText(String.valueOf(doneCount));
    }

    private void updateinProgressCount() {
        int inProgressCount = inProgressList.size();
        inProgressCountTextView.setText(String.valueOf(inProgressCount));
    }

    private void showEditProjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_project, null);
        builder.setView(dialogView).setTitle("Edit Project");

        EditText editProjectName = dialogView.findViewById(R.id.editProjectName);
        EditText editProjectDescription = dialogView.findViewById(R.id.editProjectDescription);

        editProjectName.setText(getIntent().getStringExtra("PROJECT_NAME"));
        editProjectDescription.setText(getIntent().getStringExtra("PROJECT_DESCRIPTION"));

        Button saveButton = dialogView.findViewById(R.id.saveProjectButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String updatedProjectName = editProjectName.getText().toString().trim();
            String updatedProjectDescription = editProjectDescription.getText().toString().trim();

            if (!updatedProjectName.isEmpty() && !updatedProjectDescription.isEmpty()) {
                updateProjectInDatabase(updatedProjectName, updatedProjectDescription);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProjectInDatabase(String name, String description) {
        if (projectId == null) {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference projectRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId);

        projectRef.child("name").setValue(name);
        projectRef.child("description").setValue(description)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                        TextView projectNameTextView = findViewById(R.id.projectTitle);
                        projectNameTextView.setText(name);
                    } else {
                        Toast.makeText(this, "Failed to update project", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProjectDetailsActivity.this, project_tab.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
