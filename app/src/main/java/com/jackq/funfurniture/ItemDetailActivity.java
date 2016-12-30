package com.jackq.funfurniture;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.jackq.funfurniture.API.APIServer;
import com.jackq.funfurniture.model.Furniture;
import com.jackq.funfurniture.model.FurnitureDetail;

public class ItemDetailActivity extends AppCompatActivity {
    private static final String TAG = "ITEM_DETAIL_ACTIVITY";

    private Furniture furniture;
    private FurnitureDetail detail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        furniture = (Furniture) getIntent().getExtras().get("furniture");

        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setToolbarTitle(furniture.getName());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemDetailActivity.this, ARViewActivity.class);
                intent.putExtra("furniture", furniture);
                startActivity(intent);
            }
        });

        loadDetail();
    }

    private void loadDetail(){
        APIServer.getItemDetail(this, furniture.getId(), new APIServer.APIServerCallback<FurnitureDetail>() {
            @Override
            public void onResource(FurnitureDetail resource) {
                detail = resource;
                applyDetail();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: exception on loading data", e);
            }
        });
    }


    private void setToolbarTitle(String title){
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(title);
    }

    private void applyDetail(){
        // TODO: update ui after loading resource
    }
}
