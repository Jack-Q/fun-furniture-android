package com.jackq.funfurniture;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;

import com.jackq.funfurniture.AR.VuforiaApplicationSession;
import com.jackq.funfurniture.AR.util.Utilities;
import com.vuforia.COORDINATE_SYSTEM_TYPE;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.GLTextureUnit;
import com.vuforia.Matrix34F;
import com.vuforia.Mesh;
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

import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class VideoBackgroundShader {
    private static final String VB_VERTEX_SHADER =
            "attribute vec4 vertexPosition; " +
                    "attribute vec2 vertexTexCoord;\n" +
                    "uniform mat4 projectionMatrix;\n" +
                    "varying vec2 texCoord;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = projectionMatrix * vertexPosition;\n" +
                    "    texCoord = vertexTexCoord;\n" +
                    "}\n";

    private static final String VB_FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 texCoord;\n" +
                    "uniform sampler2D texSampler2D;\n" +
                    "void main ()\n" +
                    "{\n" +
                    "    gl_FragColor = texture2D(texSampler2D, texCoord);\n" +
                    "}\n";

    // Members holds the reference to a shader instance
    // performed as a container class used with in this file
    int shaderProgramID = 0;
    int textureSampler2DHandle = 0;
    int vertexPositionHandle = 0;
    int vertexTextureCoordinateHandle = 0;
    int projectionMatrixHandle = 0;

    VideoBackgroundShader() {

        shaderProgramID = com.jackq.funfurniture.AR.util.Utilities.createProgramFromShaderSrc(VB_VERTEX_SHADER,
                VB_FRAGMENT_SHADER);

        // Rendering configuration for video background
        if (shaderProgramID > 0) {
            // Activate shader:
            GLES20.glUseProgram(shaderProgramID);

            // Retrieve handle to shader variables
            vertexPositionHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
            vertexTextureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
            projectionMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "projectionMatrix");
            textureSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");

            // Stop using the program
            GLES20.glUseProgram(0);
        }
    }
}

public class ARViewRenderer extends Renderer {
    private static final String TAG = "ARViewRenderer";
    private com.vuforia.Renderer vuforiaRenderer;
    private RenderingPrimitives mRenderingPrimitives = null;
    private Activity activity;
    private VuforiaApplicationSession vuforiaAppSession;


    private boolean isActive;

    private int currentView = VIEW.VIEW_SINGULAR;
    private float mNearPlane = -1.0f;
    private float mFarPlane = -1.0f;


    // Shader user to render the video background on AR mode
    private VideoBackgroundShader videoBackgroundShader = null;
    private GLTextureUnit videoBackgroundTexture = null;

    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    // Stores orientation
    private boolean isPortrait = false;

    public ARViewRenderer(Activity activity, VuforiaApplicationSession session) {
        // construct Rajawali 3D
        super(activity);
        this.activity = activity;

        // Fields
        this.vuforiaAppSession = session;
        setNearFarPlanes(10f, 5000f); // TODO: Near / far plane ?

        // construct Vuforia
        vuforiaRenderer = com.vuforia.Renderer.getInstance();
        Device vuforiaDevice = Device.getInstance();
        vuforiaDevice.setViewerActive(false); // whether the app will be working with a viewer
        vuforiaDevice.setMode(com.vuforia.Device.MODE.MODE_AR); // Working in VR or AR mode

    }

    private void initVuforiaRendering() {
        this.videoBackgroundShader = new VideoBackgroundShader();
        videoBackgroundTexture = new GLTextureUnit();
    }

    @Override
    protected void initScene() {
        // the buffer will be cleared by Vuforia
        getCurrentScene().alwaysClearColorBuffer(false);
    }

    /**
     * This is an equivalence to the onSurfaceChanged handler in standard interface
     *
     * @see org.rajawali3d.view.SurfaceView.RendererDelegate#onSurfaceChanged(GL10, int, int)
     */
    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done

        updateActivityOrientation();
        storeScreenDimensions();

        configureVideoBackground();

        mRenderingPrimitives = Device.getInstance().getRenderingPrimitives();

        // initRendering();
        super.onRenderSurfaceSizeChanged(gl, width, height);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    /**
     * @see org.rajawali3d.view.SurfaceView.RendererDelegate#onSurfaceCreated(GL10, EGLConfig)
     */
    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        vuforiaAppSession.onSurfaceCreated();
        initVuforiaRendering();
    }

    /**
     * Render the frame on screen
     * This will be directly invoked by Surface View
     *
     * @see org.rajawali3d.view.SurfaceView.RendererDelegate#onDrawFrame(GL10)
     */
    @Override
    public void onRenderFrame(GL10 gl) {
        // if this is not active, stop render
        if (!isActive) return;

        // Render Vuforia Scene (at this stage, the old color buffer will be cleared)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        State state;
        // Get our current state
        state = TrackerManager.getInstance().getStateUpdater().updateState();
        vuforiaRenderer.begin(state);

        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore standard counter clockwise face culling will result in
        // "inside out" models.
        if (com.vuforia.Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // We get a list of views which depend on the mode we are working on, for mono we have
        // only one view, in stereo we have three: left, right and postprocess
        ViewList viewList = mRenderingPrimitives.getRenderingViews();

        // Cycle through the view list
        for (int v = 0; v < viewList.getNumViews(); v++) {
            // Get the view id
            int viewID = viewList.getView(v);

            Vec4I viewport;
            // Get the viewport for that specific view
            viewport = mRenderingPrimitives.getViewport(viewID);

            // Set viewport for current view
            GLES20.glViewport(viewport.getData()[0], viewport.getData()[1], viewport.getData()[2], viewport.getData()[3]);

            // Set scissor
            GLES20.glScissor(viewport.getData()[0], viewport.getData()[1], viewport.getData()[2], viewport.getData()[3]);

            // Get projection matrix for the current view. COORDINATE_SYSTEM_CAMERA used for AR and
            // COORDINATE_SYSTEM_WORLD for VR
            Matrix34F projMatrix = mRenderingPrimitives.getProjectionMatrix(viewID, COORDINATE_SYSTEM_TYPE.COORDINATE_SYSTEM_CAMERA);

            // Create GL matrix setting up the near and far planes
            float rawProjectionMatrixGL[] = Tool.convertPerspectiveProjection2GLMatrix(
                    projMatrix,
                    mNearPlane,
                    mFarPlane)
                    .getData();

            // Apply the appropriate eye adjustment to the raw projection matrix, and assign to the global variable
            float eyeAdjustmentGL[] = Tool.convert2GLMatrix(mRenderingPrimitives
                    .getEyeDisplayAdjustmentMatrix(viewID)).getData();

            float projectionMatrix[] = new float[16];
            // Apply the adjustment to the projection matrix
            Matrix.multiplyMM(projectionMatrix, 0, rawProjectionMatrixGL, 0, eyeAdjustmentGL, 0);

            currentView = viewID;

            // Call renderFrame from the app renderer class which implements SampleAppRendererControl
            // This will be called for MONO, LEFT and RIGHT views, POSTPROCESS will not render the
            // frame
            if (currentView != VIEW.VIEW_POSTPROCESS) {
                // TODO: Rendering The Scene here
                // 1. Render the background with pixels from camera
                renderVideoBackground();
                // 2. Update the object position & perspective based on the update of the physical position
                // TODO:
                // mRenderingInterface.renderFrame(state, projectionMatrix);
                // 3. Render the Rajawali scene with managed position and relations
                super.onRenderFrame(gl);
            }
        }

        vuforiaRenderer.end();

    }

    /**
     * Render the Scene
     */
    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Render Rajawali Scene
        super.onRender(elapsedRealTime, deltaTime);
    }


    // Stores the orientation depending on the current resources configuration
    private void updateActivityOrientation() {
        Configuration config = activity.getResources().getConfiguration();

        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                isPortrait = true;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                isPortrait = false;
                break;
            case Configuration.ORIENTATION_UNDEFINED:
            default:
                break;
        }

        Log.i(TAG, "Activity is in "
                + (isPortrait ? "PORTRAIT" : "LANDSCAPE"));
    }


    public void setActive(boolean active) {
        isActive = active;

        if (isActive)
            configureVideoBackground();
    }


    public void setNearFarPlanes(float near, float far) {
        mNearPlane = near;
        mFarPlane = far;
    }

    public void renderVideoBackground() {
        if (currentView == VIEW.VIEW_POSTPROCESS)
            return;

        int vbVideoTextureUnit = 0;
        // Bind the video bg texture and get the Texture ID from Vuforia
        videoBackgroundTexture.setTextureUnit(vbVideoTextureUnit);
        if (!vuforiaRenderer.updateVideoBackgroundTexture(videoBackgroundTexture)) {
            Log.e(TAG, "Unable to update video background texture");
            return;
        }

        float[] vbProjectionMatrix = Tool.convert2GLMatrix(
                mRenderingPrimitives.getVideoBackgroundProjectionMatrix(currentView, COORDINATE_SYSTEM_TYPE.COORDINATE_SYSTEM_CAMERA)).getData();

        // Apply the scene scale on video see-through eyewear, to scale the video background and augmentation
        // so that the display lines up with the real world
        // This should not be applied on optical see-through devices, as there is no video background,
        // and the calibration ensures that the augmentation matches the real world
        if (Device.getInstance().isViewerActive()) {
            float sceneScaleFactor = (float) getSceneScaleFactor();
            Matrix.scaleM(vbProjectionMatrix, 0, sceneScaleFactor, sceneScaleFactor, 1.0f);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        Mesh vbMesh = mRenderingPrimitives.getVideoBackgroundMesh(currentView);
        // Load the shader and upload the vertex/texcoord/index data
        GLES20.glUseProgram(videoBackgroundShader.shaderProgramID);
        GLES20.glVertexAttribPointer(videoBackgroundShader.vertexPositionHandle,
                3, GLES20.GL_FLOAT, false, 0, vbMesh.getPositions().asFloatBuffer());
        GLES20.glVertexAttribPointer(videoBackgroundShader.vertexTextureCoordinateHandle,
                2, GLES20.GL_FLOAT, false, 0, vbMesh.getUVs().asFloatBuffer());

        GLES20.glUniform1i(videoBackgroundShader.textureSampler2DHandle, vbVideoTextureUnit);

        // Render the video background with the custom shader
        // First, we enable the vertex arrays
        GLES20.glEnableVertexAttribArray(videoBackgroundShader.vertexPositionHandle);
        GLES20.glEnableVertexAttribArray(videoBackgroundShader.vertexTextureCoordinateHandle);

        // Pass the projection matrix to OpenGL
        GLES20.glUniformMatrix4fv(videoBackgroundShader.projectionMatrixHandle, 1, false, vbProjectionMatrix, 0);

        // Then, we issue the render call
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vbMesh.getNumTriangles() * 3, GLES20.GL_UNSIGNED_SHORT,
                vbMesh.getTriangles().asShortBuffer());

        // Finally, we disable the vertex arrays
        GLES20.glDisableVertexAttribArray(videoBackgroundShader.vertexPositionHandle);
        GLES20.glDisableVertexAttribArray(videoBackgroundShader.vertexTextureCoordinateHandle);

        Utilities.checkGLError("Rendering of the video background failed");
    }


    static final float VIRTUAL_FOV_Y_DEGS = 85.0f;
    static final float M_PI = 3.14159f;

    double getSceneScaleFactor() {
        // Get the y-dimension of the physical camera field of view
        Vec2F fovVector = CameraDevice.getInstance().getCameraCalibration().getFieldOfViewRads();
        float cameraFovYRads = fovVector.getData()[1];

        // Get the y-dimension of the virtual camera field of view
        float virtualFovYRads = VIRTUAL_FOV_Y_DEGS * M_PI / 180;

        // The scene-scale factor represents the proportion of the viewport that is filled by
        // the video background when projected onto the same plane.
        // In order to calculate this, let 'd' be the distance between the cameras and the plane.
        // The height of the projected image 'h' on this plane can then be calculated:
        //   tan(fov/2) = h/2d
        // which rearranges to:
        //   2d = h/tan(fov/2)
        // Since 'd' is the same for both cameras, we can combine the equations for the two cameras:
        //   hPhysical/tan(fovPhysical/2) = hVirtual/tan(fovVirtual/2)
        // Which rearranges to:
        //   hPhysical/hVirtual = tan(fovPhysical/2)/tan(fovVirtual/2)
        // ... which is the scene-scale factor
        return Math.tan(cameraFovYRads / 2) / Math.tan(virtualFovYRads / 2);
    }

    // Configures the video mode and sets offsets for the camera's image
    private void configureVideoBackground() {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize = 0, ySize = 0;
        // We keep the aspect ratio to keep the video correctly rendered. If it is portrait we
        // preserve the height and scale width and vice versa if it is landscape, we preserve
        // the width and we check if the selected values fill the screen, otherwise we invert
        // the selection
        if (isPortrait) {
            xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
                    .getWidth()));
            ySize = mScreenHeight;

            if (xSize < mScreenWidth) {
                xSize = mScreenWidth;
                ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
        } else {
            xSize = mScreenWidth;
            ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
                    .getWidth()));

            if (ySize < mScreenHeight) {
                xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
                        .getHeight()));
                ySize = mScreenHeight;
            }
        }

        config.setSize(new Vec2I(xSize, ySize));

        Log.i(TAG, "Configure Video Background : Video (" + vm.getWidth()
                + " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
                + mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");

        com.vuforia.Renderer.getInstance().setVideoBackgroundConfig(config);

    }


    // Stores screen dimensions
    private void storeScreenDimensions() {
        // Query display dimensions:
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.getWindowManager().getDefaultDisplay().getRealSize(size);
        }
        mScreenWidth = size.x;
        mScreenHeight = size.y;
    }
}
