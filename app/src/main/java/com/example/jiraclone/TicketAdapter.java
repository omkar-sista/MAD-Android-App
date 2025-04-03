package com.example.jiraclone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<Ticket> ticketList;
    private boolean isDraggable;

    public TicketAdapter(List<Ticket> ticketList,  boolean isDraggable) {
        this.ticketList = ticketList;
        this.isDraggable = isDraggable;

    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {

        Ticket ticket = ticketList.get(position);
        holder.ticketTitleTextView.setText(ticket.getTitle());
        holder.ticketDescriptionTextView.setText(ticket.getDescription());
        holder.ticketDateTextView.setText(ticket.getDate());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.status_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.ticketStatus.setAdapter(adapter);

        String currentStatus = ticket.getStatus();
        if (currentStatus != null) {
            int spinnerPosition = adapter.getPosition(currentStatus);
            holder.ticketStatus.setSelection(spinnerPosition);
        } else {
            Log.d("TicketAdapter", "No status available for this ticket.");
        }

        holder.ticketStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = parent.getItemAtPosition(position).toString();

                if (!newStatus.equals(currentStatus)) {
                    ticket.setStatus(newStatus);
                    DatabaseReference ticketRef = FirebaseDatabase.getInstance().getReference("projects").child(ticket.getProjectId()).child("tickets");
                    ticketRef.child(ticket.getTicketId()).setValue(ticket)
                            .addOnSuccessListener(aVoid -> Log.d("FirebaseUpdate", "Updating status at path: projects/" + ticket.getProjectId() +" "+ticket))
                            .addOnFailureListener(e -> Log.d("FirebaseUpdate", "Failed to update status: " + e.getMessage()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        holder.itemView.setOnClickListener(v -> showTicketDialog(holder, ticket));

        if (isDraggable) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipData dragData = ClipData.newPlainText("ticket", ticket.getTitle());
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(dragData, shadowBuilder, v, 0);
                    return true;
                }
            });
        }
        holder.itemView.setOnLongClickListener(view -> {
            ClipData.Item item = new ClipData.Item(ticket.getTicketId());
            ClipData dragData = new ClipData(ticket.getTicketId(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDragAndDrop(dragData, shadowBuilder, null, 0);

            return true;
        });

    }
    private void showTicketDialog(TicketViewHolder holder, Ticket ticket) {
        Dialog dialog = new Dialog(holder.itemView.getContext());
        dialog.setContentView(R.layout.dialog_ticket_details);

        ImageView closeDialog = dialog.findViewById(R.id.closeDialog);
        closeDialog.setOnClickListener(v -> dialog.dismiss());

        TextView projectTitle = dialog.findViewById(R.id.projectTitle);
        FirebaseDatabase.getInstance().getReference("projects").child(ticket.getProjectId()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String projectName = snapshot.getValue(String.class);
                        projectTitle.setText("Project: " + projectName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Failed to retrieve project name: " + error.getMessage());
                    }
                });

        EditText ticketTitleEditText = dialog.findViewById(R.id.ticketTitle);
        ticketTitleEditText.setText(ticket.getTitle());

        EditText ticketDescriptionEditText = dialog.findViewById(R.id.ticketDescription);
        ticketDescriptionEditText.setText(ticket.getDescription());

        TextView dateCreated = dialog.findViewById(R.id.dateCreated);
        dateCreated.setText(ticket.getDate());

        TextView dateAssigned = dialog.findViewById(R.id.dateAssigned);
        dateAssigned.setText(ticket.getDateAssigned());

        dateAssigned.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    holder.itemView.getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Update the TextView with the selected date
                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        dateAssigned.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        Spinner statusDropdown = dialog.findViewById(R.id.statusDropdown);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.status_array,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusDropdown.setAdapter(statusAdapter);

        String currentStatus = ticket.getStatus();
        final String[] selectedStatus = {currentStatus};
        if (currentStatus != null) {
            int spinnerPosition = statusAdapter.getPosition(currentStatus);
            statusDropdown.setSelection(spinnerPosition);
        }

        statusDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus[0] = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner assigneeDropdown = dialog.findViewById(R.id.assigneeDropdown);
        DatabaseReference projectRef = FirebaseDatabase.getInstance().getReference("projects")
                .child(ticket.getProjectId()).child("members");

        projectRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                List<String> members = (List<String>) dataSnapshot.getValue();
                if (members != null && !members.isEmpty()) {
                    ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                            android.R.layout.simple_spinner_item, members);
                    assigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    assigneeDropdown.setAdapter(assigneeAdapter);

                    String currentAssignee = ticket.getAssignee();
                    final String[] selectedAssignee = {currentAssignee};

                    if (currentAssignee != null) {
                        int assigneePosition = assigneeAdapter.getPosition(currentAssignee);
                        assigneeDropdown.setSelection(assigneePosition);
                    }

                    assigneeDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedAssignee[0] = parent.getItemAtPosition(position).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Log.d("FirebaseRetrieve", "No members found for the project.");
                }
            }
        }).addOnFailureListener(e -> Log.d("FirebaseRetrieve", "Failed to retrieve members: " + e.getMessage()));

        dialog.findViewById(R.id.saveButton).setOnClickListener(v -> {
            // Update ticket title and description
            String newTitle = ticketTitleEditText.getText().toString().trim();
            String newDescription = ticketDescriptionEditText.getText().toString().trim();

            if (!newTitle.equals(ticket.getTitle())) {
                ticket.setTitle(newTitle);
            }
            if (!newDescription.equals(ticket.getDescription())) {
                ticket.setDescription(newDescription);
            }

            if (!selectedStatus[0].equals(ticket.getStatus())) {
                ticket.setStatus(selectedStatus[0]);
            }

            if (!assigneeDropdown.getSelectedItem().toString().equals(ticket.getAssignee())) {
                ticket.setAssignee(assigneeDropdown.getSelectedItem().toString());
            }
            
            String newDateAssigned = dateAssigned.getText().toString().trim();
            if (!newDateAssigned.equals(ticket.getDateAssigned())) {
                ticket.setDateAssigned(newDateAssigned);
            }

            updateTicketInFirebase(ticket);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateTicketInFirebase(Ticket ticket) {
        DatabaseReference ticketRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(ticket.getProjectId())
                .child("tickets")
                .child(ticket.getTicketId());
        ticketRef.setValue(ticket)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseUpdate", "Ticket updated successfully"))
                .addOnFailureListener(e -> Log.d("FirebaseUpdate", "Failed to update ticket: " + e.getMessage()));
    }


    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketTitleTextView;
        TextView ticketDescriptionTextView;
        TextView ticketDateTextView; // New TextView for date
        Spinner ticketStatus;
        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketTitleTextView = itemView.findViewById(R.id.ticket_title);
            ticketDescriptionTextView = itemView.findViewById(R.id.ticket_summary);
            ticketDateTextView = itemView.findViewById(R.id.ticket_date);
            ticketStatus = itemView.findViewById(R.id.status_spinner);
        }
    }

    public void updateTicketList(List<Ticket> tickets) {
        this.ticketList = tickets;
        notifyDataSetChanged();
    }
}