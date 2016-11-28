package com.jackq.funfurniture.AR;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by jackq on 11/28/16.
 */ // The config chooser.
public class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
    public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
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
