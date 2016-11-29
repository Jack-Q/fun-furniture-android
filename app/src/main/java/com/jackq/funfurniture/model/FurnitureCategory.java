package com.jackq.funfurniture.model;


public class FurnitureCategory {
    private int code;
    private String displayName;

    public FurnitureCategory(){

    }

    public FurnitureCategory(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

