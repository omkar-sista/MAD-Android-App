package com.example.jiraclone;

import java.util.ArrayList;

public class Project {
    private String id;
    private String name;
    private String description;
    private String color;
    private String userEmail;  // Updated field for storing the user email
    private ArrayList<String> members;

    public Project() {
    }

    public Project(String id, String name, String description, String color, String userEmail, ArrayList<String> members) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.userEmail = userEmail;
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }
}
