package com.example.industrial.models;

import java.util.Date;

public class MachineData {
    private int machine_id;
    private int[] values;
    private String timestamp;

    public MachineData(int machine_id, int[] values, String timestamp) {
        this.machine_id = machine_id;
        this.values = values;
        this.timestamp = timestamp;
    }

    public int[] getValues() {
        return values;
    }


    public int getSpeed(){
        return values[0];
    }

    public int getEfficiency(){
        return values[1];
    }

    public int getTemp(){
        return values[2];
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getMachine_id() {
        return machine_id;
    }
}
