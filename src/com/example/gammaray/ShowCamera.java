package com.example.gammaray;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

   private SurfaceHolder mHolder;
   private Camera mCamera;

   public ShowCamera(Context context,Camera camera) {
      super(context);
      
      // Install a SurfaceHolder.Callback so we get notified when the
      // underlying surface is created and destroyed.
      mCamera = camera;
      mHolder = getHolder();
      mHolder.addCallback(this);
   }

   @Override
   public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      // If your preview can change or rotate, take care of those events here.
      // Make sure to stop the preview before resizing or reformatting it.
      if (mHolder.getSurface() == null) {
         // preview surface does not exist
         return;
      }

      // stop preview before making changes
      try {
         mCamera.stopPreview();
      } 
      catch (Exception e) {
      // ignore: tried to stop a non-existent preview
      }  
      
      // set preview size and make any resize, rotate or
      // reformatting changes here

      // start preview with new settings
      try {
         mCamera.setPreviewDisplay(mHolder);
         mCamera.startPreview();
      } 
      catch (Exception e){
         Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
      }
   }

   @Override
   public void surfaceCreated(SurfaceHolder holder) {
      // The Surface has been created, now tell the camera where to draw the preview.
      try   {
         mCamera.setPreviewDisplay(holder);
         mCamera.startPreview(); 
      } catch (IOException e) {
      }
   }

   @Override
   public void surfaceDestroyed(SurfaceHolder holder) {
      // empty. Take care of releasing the Camera preview in your activity.
   }

}