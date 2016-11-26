package com.jackq.funfurniture;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.jackq.funfurniture.AR.VuforiaAppRenderer;
import com.jackq.funfurniture.AR.VuforiaAppRendererControl;
import com.jackq.funfurniture.AR.VuforiaApplicationSession;
import com.jackq.funfurniture.AR.util.CubeShaders;
import com.jackq.funfurniture.AR.util.LoadingDialogHandler;
import com.jackq.funfurniture.AR.util.SingleObject;
import com.jackq.funfurniture.AR.util.Utilities;
import com.jackq.funfurniture.AR.util.VuforiaTexture;
import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.renderer.Renderer;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CameraRenderer extends Renderer implements GLSurfaceView.Renderer, VuforiaAppRendererControl {
    private static final String LOGTAG = "ImageTargetRenderer";

    private VuforiaApplicationSession vuforiaAppSession;
    private CameraActivity mActivity;
    private VuforiaAppRenderer mSampleAppRenderer;

    private Vector<VuforiaTexture> mTextures;

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    private SingleObject mTeapot;
    private Object3D chairObject;

    private float kBuildingScale = 12.0f;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;

    private static final float OBJECT_SCALE_FLOAT = 3.0f;


    public CameraRenderer(CameraActivity activity, VuforiaApplicationSession session) {
        super(activity);

        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new VuforiaAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 10f, 5000f);
    }




    // Function for initializing the renderer.
    private void initRendering() {
        Log.d(LOGTAG, "Init rendering ... ");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (VuforiaTexture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = Utilities.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        if (!mModelIsLoaded) {
            // Load model from file
            try {
                mTeapot = new SingleObject();
                mTeapot.loadModel(mActivity.getResources().getAssets(),
                        "model_obj.txt");
                mModelIsLoaded = true;
                Log.e(LOGTAG, "object file is loaded from the MTL file");
            } catch (IOException e) {
                Log.e(LOGTAG, "Unable to load object file");
            }

            try{
                LoaderOBJ loaderOBJ = new LoaderOBJ(mActivity.getResources(), this.getTextureManager(), R.raw.model_chair_obj);
                this.loadModel(loaderOBJ, new IAsyncLoaderCallback() {
                    @Override
                    public void onModelLoadComplete(ALoader loader) {

                        Log.d(LOGTAG, "Model load complete: " + loader);
                        final LoaderOBJ obj = (LoaderOBJ) loader;
                        CameraRenderer.this.chairObject = obj.getParsedObject();

                        getCurrentScene().addChild(chairObject);
                        // Hide the Loading Dialog
                        mActivity.loadingDialogHandler
                                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
                    }

                    @Override
                    public void onModelLoadFailed(ALoader loader) {
                        Log.e(LOGTAG, "failed to load the content");
                    }
                }, R.raw.model_chair_obj);
            }catch (Exception e){
                Log.e(LOGTAG, "Failed to load object", e);
            }

        }

    }


    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix) {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();


            // from this interface can we acquire the current marker, thus multiple marker is enabled by default
            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0 : 1;

            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            // Translation can also be applied in the similar way
            Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
            Matrix.scaleM(modelViewMatrix, 0, 2, 2, 2);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

                /* Render the imported object */
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mTeapot.getVertices());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(1).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                    mTeapot.getNumObjectVertex());
            Log.d(LOGTAG, "Render Chair model on screen");
            Utilities.checkGLError("Renderer DrawBuildings");
                /* Render finished*/
            /* Render Chair Object */

            Utilities.checkGLError("Render Frame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<VuforiaTexture> textures) {
        mTextures = textures;
    }


    // region Merged



    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }


    public void setActive(boolean active) {
        mIsActive = active;

        if (mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        initRendering();
    }
    // Renderer Setting
    @Override
    protected void initScene() {

    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
    // endregion
}
