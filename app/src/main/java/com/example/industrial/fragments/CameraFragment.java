/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.industrial.fragments;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.industrial.API.APIClient;
import com.example.industrial.API.APIInterface;
import com.example.industrial.R;
import com.example.industrial.camera.AnimationManager;
import com.example.industrial.camera.CameraActionHandler;
import com.example.industrial.glass.GlassGestureDetector;
import com.example.industrial.menu.MenuActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Fragment responsible for displaying the camera preview and handling camera actions.
 */
public class CameraFragment extends Fragment
    implements ActivityCompat.OnRequestPermissionsResultCallback, GlassGestureDetector.OnGestureListener,
        CameraActionHandler.CameraActionHandlerCallback{

  private static final String TAG = CameraFragment.class.getSimpleName();

  private static final String MENU_KEY = "menu_key";
  private static final int MENU_REQUEST_CODE = 100;
  /**
   * Request code for the camera permission. This value doesn't have any special meaning.
   */
  private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 105;

  /**
   * Permissions required for the camera usage.
   */
  private static final String[] REQUIRED_PERMISSIONS = new String[]{permission.CAMERA,
      permission.WRITE_EXTERNAL_STORAGE, permission.RECORD_AUDIO};

  /**
   * Default margin for the shutter indicator.
   */
  private static final int DEFAULT_MARGIN_PX = 8;

  /**
   * An {@link TextureView} for camera preview.
   */
  private TextureView textureView;

  /**
   * An {@link ImageView} for camera shutter image.
   */
  private ImageView shutterImageView;

  /**
   * An {@link ImageView} for video cam image.
   */
  private ImageView videoImageView;

  /**
   * {@link CameraActionHandler} for the camera.
   */
  private CameraActionHandler cameraActionHandler;

  /**
   * Flag indicating if the long press action has been performed.
   */
  private boolean isLongPressPerformed = false;

  private int machineId;
  private int rotation;
  private int screenWidth;
  private int screenHeight;

  private APIInterface apiService;

  private Bitmap currentImageBitmap;

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener
      = new TextureView.SurfaceTextureListener() {

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
      Log.d(TAG, "Surface texture available");
      cameraActionHandler.setPreviewSurface(getSurface(texture));
      cameraActionHandler.openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
      Log.d(TAG, "Surface texture size changed");

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
      Log.d(TAG, "Surface texture destroyed");
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }
  };

  public void setMachineId(int machineId){
    this.machineId = machineId;
  }
  public void setRotation(int rotation) { this.rotation = rotation;}
  public void setScreenSize(int width, int height){
    screenWidth = width;
    screenHeight = height;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == MenuActivity.RESULT_MENU && data != null) {
      final int id = data.getIntExtra(MenuActivity.EXTRA_MENU_ITEM_ID_KEY,
              MenuActivity.EXTRA_MENU_ITEM_DEFAULT_VALUE);
      Log.d(TAG,"menu result: " + id);

      if(id == R.id.save){
        Toast.makeText(getActivity(),"Uploading image", Toast.LENGTH_LONG).show();
        postImage(currentImageBitmap);
      }
      if(id == R.id.discard){
        Toast.makeText(getActivity(),"Image discarded", Toast.LENGTH_LONG).show();
      }
    }
  }

  public void postImage(Bitmap imageBitmap){
    Log.d(TAG, "Uploading image");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);

    String encoded = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);

    apiService.uploadMachineImage(machineId, encoded).subscribe(
            apiResult -> {
              Log.i(TAG, apiResult.getMessage());
            },
            Throwable::printStackTrace
    );
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.camera_layout, container, false);


  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    textureView = view.findViewById(R.id.cameraTextureView);
//    textureView.setRotation(-90);

    shutterImageView = view.findViewById(R.id.cameraImageView);
//    videoImageView = view.findViewById(R.id.videoImageView);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    cameraActionHandler = new CameraActionHandler(getContext(), this);
    cameraActionHandler.handleIntent(requireActivity().getIntent());

    apiService = APIClient.getInstance().create(APIInterface.class);
  }

  private void transformImage(){
    if(textureView == null){
      return;
    }
    Matrix matrix = new Matrix();
    RectF textureRectF = new RectF(0, 0, screenWidth, screenHeight);
    RectF previewRectF = new RectF(0, 0, screenHeight, screenWidth);
    float centerX = textureRectF.centerX();
    float centerY = textureRectF.centerY();
    if(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270){
      previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
      matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
      float scale = Math.max((float) screenWidth / screenWidth, (float) screenHeight / screenHeight);
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    }
    textureView.setTransform(matrix);
  }

  @Override
  public void onResume() {
    super.onResume();
    cameraActionHandler.startBackgroundThread();

    for (String permission : REQUIRED_PERMISSIONS) {
      if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity(), "Activity must not be null"),
              permission)
          != PackageManager.PERMISSION_GRANTED) {
        Log.d(TAG, "Requesting for the permissions");
        requestPermissions(new String[]{permission}, CAMERA_PERMISSIONS_REQUEST_CODE);
        return;
      }
    }

    if (textureView.isAvailable()) {
      cameraActionHandler.setPreviewSurface(getSurface(textureView.getSurfaceTexture()));
      cameraActionHandler.openCamera();
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
      transformImage();

    }
  }

  @Override
  public void onPause() {
    cameraActionHandler.closeCamera();
    cameraActionHandler.stopBackgroundThread();
    super.onPause();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == CAMERA_PERMISSIONS_REQUEST_CODE) {
      for (int result : grantResults) {
        if (result != PackageManager.PERMISSION_GRANTED) {
          Log.d(TAG, "Permission denied");
          Objects.requireNonNull(getActivity(), "Activity must not be null").finish();
        }
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    Log.d(TAG, "Permission granted");
  }

  @Override
  public boolean onGesture(GlassGestureDetector.Gesture gesture) {
    switch (gesture) {
      case TAP:
        cameraActionHandler.performTapAction();
        return true;
      case SWIPE_DOWN:
        requireActivity().finish();
        return true;
      default:
        return false;
    }
  }

  /**
   * Handles {@link KeyEvent#ACTION_UP} events for the {@link KeyEvent#KEYCODE_CAMERA} and calls
   * corresponding method on {@link CameraActionHandler}.
   */
  public boolean onKeyUp(int keyCode) {
    if (isLongPressPerformed) {
      isLongPressPerformed = false;
      return false;
    }
    switch (keyCode) {
      case KeyEvent.KEYCODE_CAMERA:
        cameraActionHandler.performCameraButtonPress();
        return true;
      default:
        return false;
    }
  }

  /**
   * Handles long key press events for the {@link KeyEvent#KEYCODE_CAMERA} and calls corresponding
   * method on {@link CameraActionHandler}.
   */
  public boolean onKeyLongPress(int keyCode) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_CAMERA:
        cameraActionHandler.performCameraButtonLongPress();
        isLongPressPerformed = true;
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onTakingPictureStarted() {
    Log.d(TAG, "Taking picture started");
    AnimationManager.animateShutter(getContext(), shutterImageView);
  }

  @Override
  public void onVideoRecordingStarted() {
    Log.d(TAG, "Video recording started");
    AnimationManager.changeImageByAlpha(videoImageView, R.drawable.ic_videocam_red);
  }

  @Override
  public void onVideoRecordingStopped() {
    Log.d(TAG, "Video recording stopped");
    AnimationManager.changeImageByAlpha(videoImageView, R.drawable.ic_videocam_white);
  }

  @Override
  public void onCameraModeChanged(CameraActionHandler.CameraMode newCameraMode) {
    Log.d(TAG, "Camera mode changed to " + newCameraMode.name());
    switch (newCameraMode) {
      case VIDEO:
        AnimationManager.changeBackgroundDrawable(shutterImageView, false);
        AnimationManager.changeBackgroundDrawable(videoImageView, false);
        break;
      case PICTURE:
        AnimationManager.changeBackgroundDrawable(videoImageView, true);
        AnimationManager.changeBackgroundDrawable(shutterImageView, true);
        break;
    }
  }

  @Override
  public void imageConfirm(Bitmap imageBitmap) {
    Log.i(TAG, "Image Ready");

    currentImageBitmap = imageBitmap.copy(imageBitmap.getConfig(),false);
    Intent intent = new Intent(getActivity(), MenuActivity.class);
    intent.putExtra(MenuActivity.EXTRA_MENU_KEY, R.menu.camera_menu);
    startActivityForResult(intent, MENU_REQUEST_CODE);
  }

  private Surface getSurface(SurfaceTexture surfaceTexture) {
    final DisplayMetrics displayMetrics = new DisplayMetrics();
    Objects.requireNonNull(getActivity(), "Activity must not be null").getWindowManager()
        .getDefaultDisplay().getRealMetrics(displayMetrics);
    surfaceTexture.setDefaultBufferSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    return new Surface(surfaceTexture);
  }

}