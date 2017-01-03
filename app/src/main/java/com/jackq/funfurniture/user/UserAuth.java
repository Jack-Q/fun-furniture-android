package com.jackq.funfurniture.user;

/**
 * Created by jackq on 1/3/17.
 */

public class UserAuth {
    private String name;
    private String picture;
    private String token;
    private String method; // Google or Facebook
    private boolean success;
    private String reason;

    @Override
    public String toString() {
        if(success){
            return name + " authenticated from " + method;
        }else{
            return "authenticated failed: " + reason;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
