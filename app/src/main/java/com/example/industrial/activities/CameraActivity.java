package com.example.industrial.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Size;
import android.view.KeyEvent;
import android.view.TextureView;
import android.widget.ImageView;

import com.example.industrial.R;
import com.example.industrial.fragments.CameraFragment;
import com.example.industrial.glass.GlassGestureDetector;

public class CameraActivity extends BaseActivity {

    private ImageView cameraBtn;
    private TextureView textureView;

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size image;

    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraFragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.camera_fragment);

//        textureView = findViewById(R.id.cameraTextureView);
//        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//                openCamera();
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//
//            }
//        });
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
}