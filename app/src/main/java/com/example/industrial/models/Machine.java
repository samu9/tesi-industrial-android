package com.example.industrial.models;

public class Machine {
    private String name;
    private int id;
    private int sectorId;

    public Machine(String name, int id, int sectorId) {
        this.name = name;
        this.id = id;
        this.sectorId = sectorId;
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
