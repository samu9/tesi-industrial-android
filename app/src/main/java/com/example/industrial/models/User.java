package com.example.industrial.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String name;
    private String role;
    private String phone;

    @SerializedName("img_url")
    private String imgUrl;

    public User(int id, String name, String role, String phone, String imgUrl) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.imgUrl = imgUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getPhone() {
        return phone;
    }
}
