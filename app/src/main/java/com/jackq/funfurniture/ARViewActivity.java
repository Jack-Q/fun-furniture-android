package com.jackq.funfurniture;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jackq.funfurniture.AR.IVuforiaApplicationControl;
import com.jackq.funfurniture.AR.VuforiaApplicationException;
import com.jackq.funfurniture.AR.VuforiaApplicationSession;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import org.rajawali3d.view.SurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class ARViewActivity extends AppCompatActivity implements IVuforiaApplicationControl {
    private static final String TAG = "ARViewActivity";
    private SurfaceView arSurfaceView;
    private ARViewRenderer renderer;

    private VuforiaApplicationSession vuforiaApplicationSession;
    private DataSet markerDataSet;

    private boolean enableExtendedTracking = true;
    private boolean enableSwitchDataSetAsap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_view);

        // Init parameter
        vuforiaApplicationSession = new VuforiaApplicationSession(this);

        // Start loading Vuforia AR Session
        vuforiaApplicationSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
    }

    // region Android Activity Lifecycle Control

    @Override
    protected void onResume() {
        super.onResume();
        try {
            vuforiaApplicationSession.resumeAR();
        } catch (VuforiaApplicationException e) {
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
        vuforiaApplicationSession.onConfigurationChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arSurfaceView != null) {
            arSurfaceView.setVisibility(View.INVISIBLE);
            arSurfaceView.onPause();
        }

        try {
            vuforiaApplicationSession.pauseAR();
        } catch (VuforiaApplicationException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            vuforiaApplicationSession.stopAR();
        } catch (VuforiaApplicationException e) {
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
    public void onInitARDone(VuforiaApplicationException e) {
        if (e != null) {
            // error occurs in initialization process
            Log.e(TAG, e.getString(), e);
            //  TODO: Process Error In Initialization Process
            return;
        }
        FrameLayout layout = (FrameLayout) findViewById(R.id.ar_surface_view_container);
        arSurfaceView = new SurfaceView(this);

        renderer = new ARViewRenderer(this, vuforiaApplicationSession);
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
            vuforiaApplicationSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
        } catch (VuforiaApplicationException ex) {
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

// Creates OpenGL contexts.
class ContextFactory implements
        GLSurfaceView.EGLContextFactory {
    private final static String TAG = "ContextFactoryForARView";
    private final static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;


    public EGLContext createContext(EGL10 egl, EGLDisplay display,
                                    EGLConfig eglConfig) {
        EGLContext context;

        Log.i(TAG, "Creating OpenGL ES 2.0 context");
        checkEglError("Before eglCreateContext", egl);
        int[] attrib_list_gl20 = {EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE};
        context = egl.eglCreateContext(display, eglConfig,
                EGL10.EGL_NO_CONTEXT, attrib_list_gl20);

        checkEglError("After eglCreateContext", egl);
        return context;
    }

    // Checks the OpenGL error.
    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    public void destroyContext(EGL10 egl, EGLDisplay display,
                               EGLContext context) {
        egl.eglDestroyContext(display, context);
    }
}


// The config chooser.
class ConfigChooser implements
        GLSurfaceView.EGLConfigChooser {
    ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
        mRedSize = r;
        mGreenSize = g;
        mBlueSize = b;
        mAlphaSize = a;
        mDepthSize = depth;
        mStencilSize = stencil;
    }


    private EGLConfig getMatchingConfig(EGL10 egl, EGLDisplay display,
                                        int[] configAttribs) {
        // Get the number of minimally matching EGL configurations
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, configAttribs, null, 0, num_config);

        int numConfigs = num_config[0];
        if (numConfigs <= 0)
            throw new IllegalArgumentException("No matching EGL configs");

        // Allocate then read the array of minimally matching EGL configs
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, configAttribs, configs, numConfigs,
                num_config);

        // Now return the "best" one
        return chooseConfig(egl, display, configs);
    }


    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        // This EGL config specification is used to specify 2.0
        // rendering. We use a minimum size of 4 bits for
        // red/green/blue, but will perform actual matching in
        // chooseConfig() below.
        final int EGL_OPENGL_ES2_BIT = 0x0004;
        final int[] s_configAttribs_gl20 = {EGL10.EGL_RED_SIZE, 4,
                EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE};

        return getMatchingConfig(egl, display, s_configAttribs_gl20);
    }


    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                  EGLConfig[] configs) {
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0);

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize)
                continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib(egl, display, config,
                    EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config,
                    EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config,
                    EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config,
                    EGL10.EGL_ALPHA_SIZE, 0);

            if (r == mRedSize && g == mGreenSize && b == mBlueSize
                    && a == mAlphaSize)
                return config;
        }

        return null;
    }


    private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue) {

        if (egl.eglGetConfigAttrib(display, config, attribute, mValue))
            return mValue[0];

        return defaultValue;
    }

    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];
}