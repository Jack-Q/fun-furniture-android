package com.jackq.funfurniture;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.jackq.funfurniture.AR.ARApplicationSession;
import com.jackq.funfurniture.AR.AbstractARViewActivity;

import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;


public class ARViewActivity extends AbstractARViewActivity<ARViewRenderer> {
    private static final String TAG = "ARViewActivity";

    @Override
    public ARViewRenderer createRenderer(ARApplicationSession session){
        return new ARViewRenderer(this, session);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(this, R.layout.activity_ar_view, null);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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
        final LoaderOBJ loaderOBJ = new LoaderOBJ(getResources(), getRenderer().getTextureManager(), R.raw.model_chair_obj);
        getRenderer().loadModel(loaderOBJ, new IAsyncLoaderCallback() {
            @Override
            public void onModelLoadComplete(ALoader loader) {

                Log.d(TAG, "Model load complete: " + loader);
                final LoaderOBJ obj = (LoaderOBJ) loader;
                getRenderer().setCurrentObject(obj.getParsedObject());
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
}
