package com.jackq.funfurniture.AR;


import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import com.jackq.funfurniture.AR.util.Utilities;
import com.jackq.funfurniture.AR.util.VideoBackgroundShader;
import com.vuforia.COORDINATE_SYSTEM_TYPE;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.GLTextureUnit;
import com.vuforia.Matrix34F;
import com.vuforia.Mesh;
import com.vuforia.Renderer;
import com.vuforia.RenderingPrimitives;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackerManager;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.VIEW;
import com.vuforia.Vec2F;
import com.vuforia.Vec2I;
import com.vuforia.Vec4I;
import com.vuforia.VideoBackgroundConfig;
import com.vuforia.VideoMode;
import com.vuforia.ViewList;

public class VuforiaApplicationException extends Exception
{

    private static final long serialVersionUID = 2L;

    public static final int INITIALIZATION_FAILURE = 0;
    public static final int VUFORIA_ALREADY_INITIALIZATED = 1;
    public static final int TRACKERS_INITIALIZATION_FAILURE = 2;
    public static final int LOADING_TRACKERS_FAILURE = 3;
    public static final int UNLOADING_TRACKERS_FAILURE = 4;
    public static final int TRACKERS_DEINITIALIZATION_FAILURE = 5;
    public static final int CAMERA_INITIALIZATION_FAILURE = 6;
    public static final int SET_FOCUS_MODE_FAILURE = 7;
    public static final int ACTIVATE_FLASH_FAILURE = 8;

    private int mCode = -1;
    private String mString = "";


    public VuforiaApplicationException(int code, String description)
    {
        super(description);
        mCode = code;
        mString = description;
    }


    public int getCode()
    {
        return mCode;
    }


    public String getString()
    {
        return mString;
    }
}

