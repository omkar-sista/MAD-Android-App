package com.example.jiraclone;

public class InviteItem {
    private String projectName;
    private String projectDescription;

    public InviteItem(String projectName, String projectDescription) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }
}

