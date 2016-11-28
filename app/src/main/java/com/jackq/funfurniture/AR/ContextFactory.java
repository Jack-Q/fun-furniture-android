package com.jackq.funfurniture.AR;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by jackq on 11/28/16.
 */ // Creates OpenGL contexts.
public class ContextFactory implements GLSurfaceView.EGLContextFactory {
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
