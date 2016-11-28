package com.jackq.funfurniture;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jackq.funfurniture.AR.ARApplicationSession;
import com.jackq.funfurniture.AR.ARException;
import com.jackq.funfurniture.AR.IARActivityControl;
import com.jackq.funfurniture.AR.util.ConfigChooser;
import com.jackq.funfurniture.AR.util.ContextFactory;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.view.SurfaceView;


public class ARViewActivity extends AppCompatActivity implements IARActivityControl {
    private static final String TAG = "ARViewActivity";
    private SurfaceView arSurfaceView;
    private ARViewRenderer renderer;

    private ARApplicationSession ARApplicationSession;
    private DataSet markerDataSet;

    private boolean enableExtendedTracking = true;
    private boolean enableSwitchDataSetAsap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(this, R.layout.activity_ar_view, null);
        contentView.setVisibility(View.VISIBLE);
        setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
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

        // Init parameter
        ARApplicationSession = new ARApplicationSession(this);

        // Start loading Vuforia AR Session
        ARApplicationSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    private void initApplicationAR() {

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        // By default GLSurfaceView tries to find a surface that is as close
        // as possible to a 16-bit RGB frame buffer with a 16-bit depth buffer.
        // This function can override the default values and set custom values.

        // By default, GLSurfaceView() creates a RGB_565 opaque surface.
        // If we want a translucent one, we should change the surface's
        // format here, using PixelFormat.TRANSLUCENT for GL Surfaces
        // is interpreted as any 32-bit surface with alpha by SurfaceFlinger.

        // If required set translucent format to allow camera image to
        // show through in the background
        if (translucent)
            arSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Setup the context factory for 2.0 rendering
        arSurfaceView.setEGLContextFactory(new ContextFactory());

        // We need to choose an EGLConfig that matches the format of
        // our surface exactly. This is going to be done in our
        // custom config chooser. See ConfigChooser class definition
        // below.
        arSurfaceView.setEGLConfigChooser(
                translucent ? new ConfigChooser(8, 8, 8, 8, depthSize, stencilSize)
                        : new ConfigChooser(5, 6, 5, 0, depthSize, stencilSize));

        arSurfaceView.setSurfaceRenderer(renderer);

        loadObject3D();
    }

    private void loadObject3D() {
        final LoaderOBJ loaderOBJ = new LoaderOBJ(getResources(), renderer.getTextureManager(), R.raw.model_chair_obj);
        renderer.loadModel(loaderOBJ, new IAsyncLoaderCallback() {
            @Override
            public void onModelLoadComplete(ALoader loader) {

                Log.d(TAG, "Model load complete: " + loader);
                final LoaderOBJ obj = (LoaderOBJ) loader;
                renderer.setCurrentObject(obj.getParsedObject());
            }

            @Override
            public void onModelLoadFailed(ALoader loader) {
                Log.e(TAG, "failed to load the content");
            }
        }, R.raw.model_chair_obj);
    }


    // region Android Activity Lifecycle Control

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ARApplicationSession.resumeAR();
        } catch (ARException e) {
            Log.e(TAG, e.getString(), e);
        }

        // Resume the GL view:
        if (arSurfaceView != null) {
            arSurfaceView.setVisibility(View.VISIBLE);
            arSurfaceView.onResume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ARApplicationSession.onConfigurationChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arSurfaceView != null) {
            arSurfaceView.setVisibility(View.INVISIBLE);
            arSurfaceView.onPause();
        }

        try {
            ARApplicationSession.pauseAR();
        } catch (ARException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ARApplicationSession.stopAR();
        } catch (ARException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        System.gc();
    }

    // endregion

    // region Vuforia Lifecycle Control

    @Override
    public boolean doLoadTrackersData() {

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (markerDataSet == null)
            markerDataSet = objectTracker.createDataSet();

        if (markerDataSet == null)
            return false;

        if (!markerDataSet.load(
                "StonesAndChips.xml",
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(markerDataSet))
            return false;

        int numTrackers = markerDataSet.getNumTrackables();
        for (int count = 0; count < numTrackers; count++) {
            Trackable trackable = markerDataSet.getTrackable(count);
            if (enableExtendedTracking) {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data " + trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData() {
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (markerDataSet != null && markerDataSet.isActive()) {
            if (objectTracker.getActiveDataSet().equals(markerDataSet)
                    && !objectTracker.deactivateDataSet(markerDataSet)) {
                result = false;
            } else if (!objectTracker.destroyDataSet(markerDataSet)) {
                result = false;
            }

            markerDataSet = null;
        }

        return result;
    }

    @Override
    public void onInitARDone(ARException e) {
        if (e != null) {
            // error occurs in initialization process
            Log.e(TAG, e.getString(), e);
            //  TODO: Process Error In Initialization Process
            return;
        }
        FrameLayout layout = (FrameLayout) findViewById(R.id.ar_surface_view_container);
        arSurfaceView = new SurfaceView(this);

        renderer = new ARViewRenderer(this, ARApplicationSession);
        renderer.setActive(true);

        initApplicationAR();
        // Now add the GL surface view. It is important
        // that the OpenGL ES surface view gets added
        // BEFORE the camera is started and video
        // background is configured.
        // addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        //        ViewGroup.LayoutParams.MATCH_PARENT));

        // Sets the UILayout to be drawn in front of the camera
        // mUILayout.bringToFront();

        // Sets the layout background to transparent
        // mUILayout.setBackgroundColor(Color.TRANSPARENT);

        layout.addView(arSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        try {
            ARApplicationSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
        } catch (ARException ex) {
            Log.e(TAG, ex.getString(), ex);
        }

        CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
    }

    @Override
    public void onVuforiaUpdate(State state) {
        if (enableSwitchDataSetAsap) {
            enableSwitchDataSetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker.getClassType());
            if (ot == null || markerDataSet == null
                    || ot.getActiveDataSet() == null) {
                Log.d(TAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(TAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doStartTrackers() {
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();
        return true;
    }

    @Override
    public boolean doStopTrackers() {
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        return true;
    }

    @Override
    public boolean doDeinitTrackers() {
        TrackerManager.getInstance().deinitTracker(ObjectTracker.getClassType());

        // Indicate if the trackers were de-initialized correctly
        return true;
    }

    // endregion
}
