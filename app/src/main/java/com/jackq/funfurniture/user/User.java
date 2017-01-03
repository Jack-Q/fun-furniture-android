package com.jackq.funfurniture.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jackq.funfurniture.LoginActivity;

import java.util.Map;


public class User {
    public User(Map<String, ?> userInfo) {

    }

    private boolean isValid() {
        return false;
    }

    public static User getUser(Context ctx){
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
}
