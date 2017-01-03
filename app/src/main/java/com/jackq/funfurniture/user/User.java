package com.jackq.funfurniture.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jackq.funfurniture.ListActivity;
import com.jackq.funfurniture.LoginActivity;

import java.util.Map;


public class User {
    private static User currentUser = null;
    private boolean valid = false;
    private String username;
    private String picture;
    private String token;

    public User(Map<String, ?> userInfo) {
        username = (String) userInfo.get("username");
        picture = (String) userInfo.get("picture");
        token = (String) userInfo.get("token");

    }

    public User(UserAuth auth){
        username = auth.getName();
        picture = auth.getPicture();
        token = auth.getToken();
        valid = true;
    }

    private boolean isValid() {
        return valid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public static User getUser(Context ctx){
        if(currentUser != null)
            return currentUser;

        SharedPreferences userSharedPreferences = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        Map<String, ?> userInfo = userSharedPreferences.getAll();

        User user = new User(userInfo);
        if(user.isValid()){
         return user;
        }else{
            return null;
        }
    }



    public static void login(Activity activity, String prompt){
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.putExtra("prompt", prompt == null ? "login to check your favorite" : prompt);
        activity.startActivity(intent);
    }

    public static void viewDetail(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }
}
