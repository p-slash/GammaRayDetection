package com.example.gammaray;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
   final static int NOISE_MODE   = 0;
   final static int EXP_MODE     = 1;
   final static int PIC_NUMBER   = 5;

   private Camera cameraObject;
   private ShowCamera showCamera;
   private ImageView pic;

   public static Camera getAvailiableCamera() {
      Camera object = null;

      try {
         object = Camera.open(); 
      }
      catch (Exception e){
         Log.e("CameraOpen", "Error in camera", e);
      }

      return object; 
   }

   private PictureCallback ExpIt = new PictureCallback() {

      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
         if(!isExternalStorageWritable())
            return;
         
         File foto = getOutputMediaFile(EXP_MODE);

         if (foto == null){
            Log.d("MediaFile", "Error creating media file, check storage permissions: ");
            return;
         }

         try {        
            FileOutputStream fos = new FileOutputStream(foto.getPath());
       
            fos.write(data[0]);
            fos.close();
         } 
         catch (FileNotFoundException e) {
            Log.d("File", "File not found: " + e.getMessage());
         } 
         catch (IOException e) {
            Log.d("File", "Error accessing file: " + e.getMessage());
        }
   
         cameraObject.startPreview();
         takePicture();
      }
   };

   private PictureCallback NoiseIt = new PictureCallback() {

      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
         if(!isExternalStorageWritable())
            return;
         
         File foto = getOutputMediaFile(NOISE_MODE);

         if (foto == null) {
            Log.d("MediaFile", "Error creating media file, check storage permissions: ");
            return;
         }

         try {        
            FileOutputStream fos = new FileOutputStream(foto.getPath());
       
            fos.write(data[0]);
            fos.close();
         } 
         catch (FileNotFoundException e) {
            Log.d("File", "File not found: " + e.getMessage());
         } 
         catch (IOException e) {
            Log.d("File", "Error accessing file: " + e.getMessage());
         }
   
         cameraObject.startPreview();
      }
   };
   

   public void StartIt(View view){
      if(cameraObject != null)
         cameraObject.takePicture(null, null, ExpIt);
   }

   public void CalibrateIt(View view){
      if(cameraObject != null) 
         cameraObject.takePicture(null, null, NoiseIt);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   
   public boolean isExternalStorageWritable() {
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state))
          return true;
      
      return false;
   }

   /** Create a File for saving an image or video */
   private static File getOutputMediaFile(int type){
      // To be safe, you should check that the SDCard is mounted
      // using Environment.getExternalStorageState() before doing this.

      File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GammaRay");
      // This location works best if you want the created images to be shared
      // between applications and persist after your app has been uninstalled.

      // Create the storage directory if it does not exist
      if (!mediaStorageDir.exists()) {
         if (!mediaStorageDir.mkdirs()){
            Log.d("GammaRay", "failed to create directory");
            return null;
         }
      }

      // Create a media file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      File mediaFile = null;

      switch(type) {
         case NOISE_MODE:
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "N_"+ timeStamp + ".jpg");
            break;
         case EXP_MODE:
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "E_"+ timeStamp + ".jpg");
            break;
      }      
      
      return mediaFile;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      cameraObject = getAvailiableCamera();
      showCamera = new ShowCamera(this, cameraObject);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(showCamera);
   }
}