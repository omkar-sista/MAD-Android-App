package com.example.jiraclone;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;
    private OnProjectClickListener projectClickListener;
    public ProjectAdapter(List<Project> projectList, OnProjectClickListener projectClickListener) {
        this.projectList = projectList;
        this.projectClickListener = projectClickListener;
    }

    public interface OnProjectClickListener {
        void onProjectClick(String projectId);
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);

        // Set project name and description
        holder.projectNameTextView.setText(project.getName());
        holder.projectDescriptionTextView.setText(project.getDescription());

        String projectColor = project.getColor();
        try {
            holder.projectColorView.setBackgroundColor(Color.parseColor(projectColor));
        } catch (IllegalArgumentException e) {
            holder.projectColorView.setBackgroundColor(Color.parseColor("#D3D3D3")); // Default color
        }

        holder.itemView.setOnClickListener(v -> {
            if (projectClickListener != null) {
                projectClickListener.onProjectClick(project.getId());
            }
            Intent intent = new Intent(v.getContext(), ProjectDetailsActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            intent.putExtra("PROJECT_DESCRIPTION", project.getDescription());
            intent.putExtra("PROJECT_COLOR", project.getColor().toString());
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectNameTextView;
        TextView projectDescriptionTextView;
        View projectColorView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            projectDescriptionTextView = itemView.findViewById(R.id.projectDescriptionTextView);
            projectColorView = itemView.findViewById(R.id.projectColorView);
        }
    }
}

