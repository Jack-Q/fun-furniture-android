package com.jackq.funfurniture.model;

import java.util.ArrayList;

public class Furniture{
    private int id;
    private String name;

    private String description;
    private int categoryCode;
    private float price;
    private String url;
    private ArrayList<String> pictures;

    public Furniture() {
        this.pictures = new ArrayList<>();
    }

    public Furniture(int id, String name, String description, int categoryCode, float price, String url, ArrayList<String> pictures) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryCode = categoryCode;
        this.price = price;
        this.url = url;
        this.pictures = pictures;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }
}
