package com.jackq.funfurniture;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jackq.funfurniture.API.APIModelLoader;
import com.jackq.funfurniture.API.APIServer;
import com.jackq.funfurniture.AR.ARApplicationSession;
import com.jackq.funfurniture.AR.AbstractARViewActivity;
import com.jackq.funfurniture.model.Furniture;
import com.jackq.funfurniture.model.FurnitureModel;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.SceneModel;
import org.rajawali3d.loader.SceneModelLoader;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.util.RajLog;

import java.util.Locale;


public class ARViewActivity extends AbstractARViewActivity<ARViewRenderer> {
    private static final String TAG = "ARViewActivity";
    private Furniture furniture;
    private FurnitureModel furnitureModel;
    private View contentView;
    private boolean model;

    Object3D object1 = null;
    Object3D object2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Get data from previous item
        if (getIntent().getExtras() != null) {
            furniture = (Furniture) getIntent().getExtras().get("furniture");
        }
        if (furniture == null) furniture = Furniture.SAMPLE; // TODO: test data used here

        contentView = View.inflate(this, R.layout.activity_ar_view, null);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        enableFullScreen();


        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.activity_ar_view);
        drawerLayout.setScrimColor(0x00000000);
        drawerLayout.setClipToPadding(false);

        final ImageButton drawerButton = (ImageButton) findViewById(R.id.drawer_open_btn);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                }
            }
        });

        final ImageButton backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARViewActivity.this.onBackPressed();
            }
        });

        // Update UI display entry
        TextView itemNameView = (TextView) findViewById(R.id.ar_item_name);
        itemNameView.setText(furniture.getName());
        TextView itemPriceView = (TextView) findViewById(R.id.ar_item_price);
        itemPriceView.setText(String.format(Locale.ENGLISH, "$ %.2f", furniture.getPrice()));
        TextView itemDescriptionView = (TextView) findViewById(R.id.ar_item_detail);
        itemDescriptionView.setText(furniture.getDescription());
        Button itemActionButton = (Button) findViewById(R.id.ar_item_action);
        itemActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Not connected yet, coming soon...", Snackbar.LENGTH_LONG)
                        .setAction("OK", null).show();
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.change_model);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeModel();
            }
        });

        loadModel();
    }

    @Override
    public ARViewRenderer createRenderer(ARApplicationSession session) {
        return new ARViewRenderer(this, session);
    }

    private void enableFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        contentView.setVisibility(View.VISIBLE);
        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | contentView.getSystemUiVisibility());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | contentView.getSystemUiVisibility());
        }
    }

    @Override
    public void initApplicationARScene() {
        // load content form server
        // TODO: Load model from server
        // final SceneModel sceneModel =

        final SceneModelLoader sceneModelLoader = new SceneModelLoader(getResources(), getRenderer().getTextureManager(), R.raw.model_chair_obj);
        getRenderer().loadModel(sceneModelLoader, new IAsyncLoaderCallback() {
            @Override
            public void onModelLoadComplete(ALoader loader) {

                Log.d(TAG, "Model load complete: " + loader);
                final LoaderOBJ obj = (LoaderOBJ) loader;
                object1 = obj.getParsedObject();
                if(model){
                    getRenderer().setCurrentObject(object1);
                }
                final SceneModelLoader sceneModelLoader1 = new SceneModelLoader(getResources(), getRenderer().getTextureManager(), R.raw.model_bed_obj);
                getRenderer().loadModel(sceneModelLoader1, new IAsyncLoaderCallback() {
                    @Override
                    public void onModelLoadComplete(ALoader loader) {
                        LoaderOBJ loader1 = (LoaderOBJ) loader;
                        object2 = loader1.getParsedObject();
                        if(!model){
                            getRenderer().setCurrentObject(object2);
                        }
                    }

                    @Override
                    public void onModelLoadFailed(ALoader loader) {

                    }
                }, R.raw.model_bed_obj);
            }

            @Override
            public void onModelLoadFailed(ALoader loader) {
                Log.e(TAG, "failed to load the content");
            }
        }, R.raw.model_chair_obj);


    }

    @Override
    public ViewGroup getARViewContainer() {
        return (ViewGroup) findViewById(R.id.ar_surface_view_container);
    }

    private void loadModel(){
        APIServer.getItemModel(this, furniture.getId(), new APIServer.APIServerCallback<FurnitureModel>() {
            @Override
            public void onResource(FurnitureModel resource) {
                furnitureModel = resource;
                loadModelFile();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: error occurred while loading model", e);
            }
        });
    }

    private void loadModelFile(){
        new APIModelLoader(this, furnitureModel, new APIModelLoader.LoaderCallback() {
            @Override
            public void handlerError(Exception e) {
                Log.e(TAG, "handlerError: error occurred wil", e);
            }

            @Override
            public void finish() {
                Log.e(TAG, "finish: download files");
            }
        }).load();
    }

    void changeModel(){
        if(model){
            model = false;
            getRenderer().setCurrentObject(object1);
        }else{
            model = true;
            getRenderer().setCurrentObject(object2);
        }
    }
}
