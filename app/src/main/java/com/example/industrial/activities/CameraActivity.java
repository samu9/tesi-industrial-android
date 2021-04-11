package com.example.industrial.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;

import com.example.industrial.R;
import com.example.industrial.fragments.CameraFragment;
import com.example.industrial.glass.GlassGestureDetector;

public class CameraActivity extends BaseActivity {
    public static final String MACHINE_ID_EXTRA = "machine id";

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private int machineId;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        machineId = getIntent().getIntExtra(MACHINE_ID_EXTRA, 1);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        cameraFragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.camera_fragment);
        cameraFragment.setMachineId(machineId);
        cameraFragment.setRotation(rotation);
        cameraFragment.setScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);


        Log.i(TAG, "rotation:" + rotation);

    }

    @Override
    public boolean onGesture(GlassGestureDetector.Gesture gesture) {
        return cameraFragment.onGesture(gesture) || super.onGesture(gesture);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return cameraFragment.onKeyUp(keyCode) || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return cameraFragment.onKeyLongPress(keyCode) || super.onKeyLongPress(keyCode, event);
    }

//    private int getOrientation(int rotation) {
//        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
//        // We have to take that into account and rotate JPEG properly.
//        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
//        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
//        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
//    }
}