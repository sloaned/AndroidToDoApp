package com.example.catalyst.androidtodo.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dsloane on 3/15/2016.
 */
public class Participant {

    @SerializedName("id")
    private int id;
    
    private String participantName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

}
