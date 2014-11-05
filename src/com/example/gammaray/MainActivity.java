package com.example.gammaray;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
//import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
   final static int NOISE_MODE   = 0;
   final static int EXP_MODE     = 1;
   final static int PIC_NUMBER   = 3;
   final static int SLEEP_TIME   = 1000; // in ms

   private Camera cameraObject;
   private ShowCamera showCamera;

   public static char in_lett = 'E';   // to distinguish noise and experimental files

   // Safely open the camera
   public static Camera getAvailiableCamera() {
      Camera object = null;

      try {
         object = Camera.open();

         Camera.Parameters params = object.getParameters();
         //Set flash off
         params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
         //Set scene mode to maximize exposure time
         params.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
         //Set parameters
         object.setParameters(params); 
      }
      catch (Exception e){
         Log.e("CameraOpen", "Error in camera", e);
      }

      return object; 
   }
   
   // Capturing a photo
   private PictureCallback SnapIt = new PictureCallback() {

      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
         new SavePicture().execute(data);
      }
   };
   
   // Async task to save files
   private class SavePicture extends AsyncTask<byte[], Void, Void> {
      protected Void doInBackground (byte[]... data) { // photo is in data
         // Check external storage
         if(!isExternalStorageWritable())
            return null;
         
         // Safely choose a file for foto.
         File foto = getOutputMediaFile();

         if (foto == null) {
            Log.d("MediaFile", "Error creating media file, check storage permissions.");
            return null;
         }

         // Write data to foto
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
         
         return null;
     }

     protected void onProgressUpdate() {
     }

     protected void onPostExecute() {
     }
      
   }

   // Starting experiment, linked to Start Button
   public void StartIt(View view){
      if(cameraObject != null) {
         in_lett = 'E';    // Initial letter to jpg file
         
         //for(int i = 0; i < PIC_NUMBER; i++) {
            cameraObject.takePicture(null, null, SnapIt);
            cameraObject.startPreview();
   			//Toast.makeText(getApplicationContext(), i, Toast.LENGTH_SHORT).show();   			   
         //}
      }
   }

   // Calibrating for noise, linked to Noise Button
   public void CalibrateIt(View view){
      if(cameraObject != null) {
         in_lett = 'N';
         cameraObject.takePicture(null, null, SnapIt);
         cameraObject.startPreview();
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   
   // Check external storage
   public boolean isExternalStorageWritable() {
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state))
          return true;
      
      return false;
   }

   /** Create a File for saving an image */
   private static File getOutputMediaFile(){
      // To be safe, you should check that the SDCard is mounted
      // using Environment.getExternalStorageState() before doing this.
      // Done by isExternalStorageWritable()

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

      mediaFile = new File(mediaStorageDir.getPath() + File.separator +  in_lett + "_"+ timeStamp + ".jpg");
           
      return mediaFile;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      cameraObject = getAvailiableCamera();
      /*
       * Creates a new Intent to start the RSSPullService
       * IntentService. Passes a URI in the
       * Intent's "data" field.
       */
      //mPhotoMan = new Intent(getActivity(), PhotoManager.class);
      //mPhotoMan.setData();
      
      showCamera = new ShowCamera(this, cameraObject);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(showCamera);
   }

   @Override
   public void onPause() {
      super.onPause();  // Always call the superclass method first

      // Release the Camera because we don't need it when paused
      // and other activities might need to use it.
      if (cameraObject != null) {
         //cameraObject.stopPreview();
         cameraObject.release();
         cameraObject = null;
      }
   }

   @Override
   public void onResume() {
      super.onResume();  // Always call the superclass method first
   
       // Get the Camera instance as the activity achieves full user focus
      if (cameraObject == null) {
         cameraObject = getAvailiableCamera(); // Local method to handle camera init
         // cameraObject.startPreview();
      }
   }
}