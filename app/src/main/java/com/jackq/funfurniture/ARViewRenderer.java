package com.jackq.funfurniture;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.jackq.funfurniture.AR.ARApplicationSession;
import com.jackq.funfurniture.AR.AbstractARViewRenderer;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;

import org.rajawali3d.Object3D;
import org.rajawali3d.debug.CoordinateTrident;
import org.rajawali3d.debug.GridFloor;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

import java.util.Locale;

public class ARViewRenderer extends AbstractARViewRenderer {
    private Vector3 mPosition = new Vector3();
    private Quaternion mOrientation = new Quaternion();
    private double[] mModelViewMatrix = new double[16];
    private Activity activity;
    protected Object3D object3D = null;
    private static final String TAG = "ARViewRenderer";

    public ARViewRenderer(Activity activity, ARApplicationSession session) {
        super(activity, session);
        this.activity = activity;



    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }


    private float mLastTouchX;
    private float mLastTouchY;
    private float mRotation = 0;
    private boolean mIsDown = false;

    @Override
    public void onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "Touch event!");
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mIsDown = true;
                final float x = ev.getX();
                final float y = ev.getY();

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if(mIsDown) {
                    final float x = ev.getX();
                    final float y = ev.getY();

                    // Calculate the distance moved
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if(object3D != null){

                        mRotation += dx / 10;

                        object3D.setRotX(mRotation);
                    }

                    // Remember this touch position for the next move event
                    mLastTouchX = x;
                    mLastTouchY = y;
                }


                break;
            }

            case MotionEvent.ACTION_UP: {
                mIsDown = false;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mIsDown = false;
                break;
            }

        }
    }


    @Override
    public void onUpdateARViewScene(State state, float[] projectionMatrix) {

        // Currently, we only care about single marker, thus only retrieve the first marker
        if (state.getNumTrackableResults() <= 0) {
            if (this.object3D != null)
                this.object3D.setVisible(false);
            return;
        }
        TrackableResult result = state.getTrackableResult(0);
        // Trackable trackable = result.getTrackable();
        // Log.d(TAG, "User data of detected marker: " + trackable.getUserData());
        Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        // from this interface can we acquire the current marker, thus multiple marker is enabled by default
        // int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0 : 1;

        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];

        // Translation can also be applied in the similar way
        Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
        // Matrix.scaleM(modelViewMatrix, 0, 2, 2, 2);
        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

        if (this.object3D != null) {
            // TODO: update object position
            object3D.setVisible(true);
            // object3D.render(getCurrentCamera(), new Matrix4(modelViewMatrix), new Matrix4(projectionMatrix), new Matrix4(), null);
            String str = "Model View Matrix\n";
            for (int i = 0; i < 4; i++)
                str += String.format(Locale.ENGLISH, "%4.2f,%4.2f,%4.2f,%4.2f\n", modelViewMatrix[i], modelViewMatrix[i + 4], modelViewMatrix[i + 8], modelViewMatrix[i + 12]);
            str += "Projection Matrix\n";
            for (int i = 0; i < 4; i++)
                str += String.format(Locale.ENGLISH, "%4.2f,%4.2f,%4.2f,%4.2f\n", projectionMatrix[i], projectionMatrix[i + 4], projectionMatrix[i + 8], projectionMatrix[i + 12]);

            //object3D.setBackSidedobject3D.getScenePosition());

            //object3D.getModelViewProjectionMatrix().setAll(projectionMatrix);
            transformPositionAndOrientation(modelViewMatrix);

            // object3D.setPosition(mPosition);
            mPosition.z = -mPosition.z;
            mPosition.x = -mPosition.x;
            mPosition.y = -mPosition.y;
            getCurrentCamera().setPosition(mPosition);
            object3D.setOrientation(mOrientation);
//            str += "Marker: ";
//            str += mPosition;
//            str += "Object: ";
//            str += object3D.getPosition();
//            final String string = str;
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    TextView textView = (TextView) activity.findViewById(R.id.text_test_surface_output);
//                    textView.setText(string);
//                }
//            });
        }

    }


    public void setCurrentObject(Object3D object3D) {
        if(object3D == null)
            return;
        if (this.object3D != null) {
            getCurrentScene().removeChild(this.object3D);
        }


        // object3D.setPosition(1, 0, -10);
        object3D.setScale(800); // Actual size maybe about 1000 or resize the marker
        object3D.setRotZ(-90);
        // object3D.setRotY(180);
        this.object3D = new Object3D();
        this.object3D.addChild(object3D);
        // object3D.setVisible(false);
        getCurrentScene().addChild(this.object3D);
        /*
        DirectionalLight mLight = new DirectionalLight(1f, 0.2f, -1.0f); // set the direction
        mLight.setColor(1.0f, 1.0f, 1.0f);
        mLight.setPower(2);

        // region debug object
        CoordinateTrident coordinateTrident = new CoordinateTrident();
        coordinateTrident.setPosition(1, 0, -10);
        coordinateTrident.setVisible(true);
        getCurrentScene().addChild(coordinateTrident);
        GridFloor gridFloor = new GridFloor(100, Color.YELLOW, 2, 10);
        gridFloor.setPosition(0, 0, -10);
        gridFloor.setRotZ(90);
        gridFloor.setVisible(false);
        getCurrentScene().addChild(gridFloor);
        gridFloor = new GridFloor(1000, Color.BLUE, 2, 100);
        gridFloor.setPosition(0, 0, -10);
        gridFloor.setRotZ(90);
        gridFloor.setVisible(false);
        getCurrentScene().addChild(gridFloor);
        // endregion
        */

    }


    protected void transformPositionAndOrientation(float[] modelViewMatrix) {
        mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
                -modelViewMatrix[14]);
        for (int mI = 0; mI < 16; mI++) {
            mModelViewMatrix[mI] = modelViewMatrix[mI];
        }
        mOrientation.fromMatrix(mModelViewMatrix);

        if (isPortrait) {
            mPosition.setAll(-modelViewMatrix[13], -modelViewMatrix[12],
                    -modelViewMatrix[14]);
            double orX = mOrientation.x;
            mOrientation.x = -mOrientation.y;
            mOrientation.y = -orX;
            mOrientation.z = -mOrientation.z;
        } else {
            mPosition.setAll(modelViewMatrix[12], -modelViewMatrix[13],
                    -modelViewMatrix[14]);
            mOrientation.y = -mOrientation.y;
            mOrientation.z = -mOrientation.z;
        }
    }
}
