package com.example.industrial.models;

public class Machine {
    private String name;
    private int id;
    private int sectorId;
    private String status;

    public Machine(String name, int id, int sectorId, String status) {
        this.name = name;
        this.id = id;
        this.sectorId = sectorId;
        this.status = status;
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
}
