package com.example.jiraclone;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.InviteViewHolder> {

    private List<InviteItem> inviteList;

    public InvitesAdapter(List<InviteItem> inviteList) {
        this.inviteList = inviteList;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {

        int currentPosition = holder.getAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return; // Ensure valid position

        InviteItem invite = inviteList.get(currentPosition);;
        holder.projectNameTextView.setText(invite.getProjectName());
        holder.projectDescriptionTextView.setText(invite.getProjectDescription());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            DatabaseReference projectsRef = FirebaseDatabase.getInstance().getReference("projects");

            projectsRef.orderByChild("name").equalTo(invite.getProjectName())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String projectId = snapshot.getKey();  // The projectId is the key in the projects node

                                DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("projects").child(projectId).child("members");

                                holder.acceptButton.setOnClickListener(v -> {
                                    membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            long currentIndex = snapshot.getChildrenCount();

                                            membersRef.child(String.valueOf(currentIndex)).setValue(userEmail)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Log.d("InviteAdapter", "User added to project successfully");
                                                        String sanitizedEmail = userEmail.replace(".", ",");
                                                        DatabaseReference inviteRef = FirebaseDatabase.getInstance().getReference("invites")
                                                                .child(sanitizedEmail).child(projectId);

                                                        inviteRef.setValue(null).addOnCompleteListener(removeTask -> {
                                                            if (removeTask.isSuccessful()) {
                                                                int updatedPosition = holder.getAdapterPosition();
                                                                if (updatedPosition != RecyclerView.NO_POSITION) {

                                                                    inviteList.remove(updatedPosition);
                                                                    notifyItemRemoved(updatedPosition);
                                                                    notifyItemRangeChanged(updatedPosition, inviteList.size());
                                                                }
                                                            } else {
                                                                Log.e("InviteAdapter", "Failed to remove invite: " + removeTask.getException());
                                                            }
                                                        });

                                                        holder.acceptButton.setEnabled(false);
                                                    } else {
                                                        Log.d("InviteAdapter", "Failed to add user to project: " + task.getException());
                                                    }
                                                });
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            Log.e("InviteAdapter", "Error fetching members data", error.toException());
                                        }
                                    });

                                });
                                holder.declineButton.setOnClickListener(v -> {

                                    String sanitizedEmail = userEmail.replace(".", ",");
                                    DatabaseReference inviteRef = FirebaseDatabase.getInstance().getReference("invites")
                                            .child(sanitizedEmail).child(projectId);

                                    inviteRef.setValue(null).addOnCompleteListener(removeTask -> {
                                        if (removeTask.isSuccessful()) {

                                            int updatedPosition = holder.getAdapterPosition();
                                            if (updatedPosition != RecyclerView.NO_POSITION) {

                                                inviteList.remove(updatedPosition);
                                                notifyItemRemoved(updatedPosition);
                                                notifyItemRangeChanged(updatedPosition, inviteList.size());
                                            }
                                        } else {
                                            Log.e("InviteAdapter", "Failed to remove invite: " + removeTask.getException());
                                        }
                                    });
                                    Log.d("check this out","not accepted");
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("InviteAdapter", "Error fetching project data", databaseError.toException());
                        }
                    });

        }
    }



    @Override
    public int getItemCount() {
        return inviteList.size();
    }

    static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView projectNameTextView, projectDescriptionTextView;
        ImageButton acceptButton, declineButton;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            projectDescriptionTextView = itemView.findViewById(R.id.projectDescriptionTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}
