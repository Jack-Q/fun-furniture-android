package com.jackq.funfurniture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jackq.funfurniture.user.User;
import com.koushikdutta.ion.Ion;

public class UserDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        User user = User.getUser(this);
        TextView nameTextView = (TextView) findViewById(R.id.user_name);
        nameTextView.setText(user.getUsername() != null ? user.getUsername() : "Please login");

        ImageView pictureImageView = (ImageView) findViewById(R.id.user_picture);
        Ion.with(pictureImageView).placeholder(R.drawable.logo_transparent).load(user.getPicture());

        Button logoutButton = (Button) findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User.setCurrentUser(null);
                UserDetail.this.finish();
            }
        });
    }
}
