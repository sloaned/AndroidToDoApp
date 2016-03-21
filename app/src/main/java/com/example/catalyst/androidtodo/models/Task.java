package com.example.catalyst.androidtodo.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Task {

    @SerializedName("id")
    private int id;

    private int serverId;

    @SerializedName("taskTitle")
    private String taskTitle;

    @SerializedName("taskDetails")
    private String taskDetails;

    @SerializedName("dueDate")
    private String dueDate;

    private long lastModifiedDate;

    private long syncDate;

    private List<Participant> participants;

    private String locationName;

    private double latitude;
    private double longitude;

    private String timeZone;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(long date) {
        lastModifiedDate = date;
    }

    public long getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(long date) {
        syncDate = date;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }


}
