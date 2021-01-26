package com.example.industrial.models;

public class Area {
    private int id;
    private String name;

    public Area(int id, String name, int sectors_count) {
        this.id = id;
        this.name = name;
        this.sectors_count = sectors_count;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSectors_count() {
        return sectors_count;
    }

    private int sectors_count;
}
