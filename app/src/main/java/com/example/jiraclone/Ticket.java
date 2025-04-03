package com.example.jiraclone;

public class Ticket {
    private String ticketId;
    private String title;
    private String description;
    private String date; // New field to store the creation date
    private String status;
    private String projectId;
    private String assignee;
    private String dateAssigned;
    public Ticket() {
    }

    public Ticket(String ticketId, String title, String description, String date,String dateAssigned, String status, String projectId) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.date = date; // Initialize the date field
        this.status = status;
        this.projectId = projectId;
        this.dateAssigned = dateAssigned;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() { // Getter for the date field
        return date;
    }

    public void setDate(String date) { // Setter for the date field
        this.date = date;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' + // Include date in the string representation
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(String dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
}