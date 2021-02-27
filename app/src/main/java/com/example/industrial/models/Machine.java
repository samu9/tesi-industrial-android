package com.example.industrial.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Machine implements Serializable {
    private String name;
    private int id;
    private int sectorId;
    private String status;

    @SerializedName("temp_threshold")
    private int tempThreshold;

    @SerializedName("speed_threshold")
    private int speedThreshold;

    @SerializedName("efficiency_threshold")
    private int efficiencyThreshold;

    public Machine(String name, int id, int sectorId, String status, int tempThreshold, int speedThreshold, int efficiencyThreshold) {
        this.name = name;
        this.id = id;
        this.sectorId = sectorId;
        this.status = status;
        this.tempThreshold = tempThreshold;
        this.speedThreshold = speedThreshold;
        this.efficiencyThreshold = efficiencyThreshold;
    }


    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getSectorId() {
        return sectorId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean checkTemp(int temp){
        return (temp <= tempThreshold);
    }

    public boolean checkEfficiency(int efficiency){
        return (efficiency >= efficiencyThreshold);
    }

    public boolean checkSpeed(int speed){
        return (speed <= speedThreshold);
    }
}
