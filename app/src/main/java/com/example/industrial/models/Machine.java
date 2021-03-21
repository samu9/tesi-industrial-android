package com.example.industrial.models;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Machine implements Serializable {

    // Machine status constants
    public final static String START = "START";
    public final static String PAUSE = "PAUSE";
    public final static String STOP = "STOP";
    public final static String HALT = "HALT";
    public static final String RESUME = "RESUME";

    private String name;
    private int id;
    private int sectorId;

    @SerializedName("value_in_danger")
    private MachineValue valueInDanger;
    private String status;

    @SerializedName("temp_threshold")
    private int tempThreshold;

    @SerializedName("speed_threshold")
    private int speedThreshold;

    @SerializedName("efficiency_threshold")
    private int efficiencyThreshold;

    public Machine(String name, int id, int sectorId, String status, int tempThreshold, int speedThreshold, int efficiencyThreshold, @Nullable MachineValue valueInDanger) {
        this.name = name;
        this.id = id;
        this.sectorId = sectorId;
        this.status = status;
        this.tempThreshold = tempThreshold;
        this.speedThreshold = speedThreshold;
        this.efficiencyThreshold = efficiencyThreshold;
        this.valueInDanger = valueInDanger;
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

    public MachineValue getValueInDanger() {
        return valueInDanger;
    }

    public void setValueInDanger(MachineValue valueInDanger) {
        this.valueInDanger = valueInDanger;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean checkData(MachineData data){
        return this.checkTemp(data.getTemp()) &&
                this.checkEfficiency(data.getEfficiency()) &&
                this.checkSpeed(data.getSpeed());
    }

    public boolean checkTemp(int temp){
        boolean pass = temp <= tempThreshold;
        if(!pass){
            Log.d(getClass().getName() + " " + getId(),"temp in danger: " + temp + "/" + tempThreshold);
            this.setValueInDanger(MachineValue.Temp);
        }
        return pass;
    }

    public boolean checkEfficiency(int efficiency){
        boolean pass = efficiency >= efficiencyThreshold;
        if(!pass){
            Log.d(getClass().getName() + " " + getId(),"efficiency in danger: " + efficiency);
            this.setValueInDanger(MachineValue.Efficiency);
        }
        return pass;
    }

    public boolean checkSpeed(int speed){
        boolean pass = speed <= speedThreshold;
        if(!pass){
            Log.d(getClass().getName() + " " + getId(),"speed in danger");
            this.setValueInDanger(MachineValue.RPM);
        }
        return pass;
    }
}
