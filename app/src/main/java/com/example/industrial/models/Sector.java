package com.example.industrial.models;

public class Sector {
    private int id;
    private String name;

    public Sector(int id, String name, int machines_count) {
        this.id = id;
        this.name = name;
        this.machines_count = machines_count;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMachines_count() {
        return machines_count;
    }

    private int machines_count;
}
