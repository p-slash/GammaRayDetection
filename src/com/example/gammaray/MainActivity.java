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
import android.os.Handler;
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
   private Handler photoHandler = new Handler();
   
   private  static boolean camEnable = false;
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
         //params.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
         //Set parameters
         object.setParameters(params);

         camEnable = true;
      }
      catch (Exception e){
         camEnable = false;
         Log.e("DeadJim", "Error in camera", e);
      }

      return object; 
   }
   
   private Runnable startTakingPhotos = new Runnable() {
      public void run() {
         cameraObject.takePicture(null, null, SnapIt);
         cameraObject.startPreview();

         photoHandler.postDelayed(this, SLEEP_TIME); // run this again SLEEP_TIME after
      }
   };

   // Capturing a photo
   private PictureCallback SnapIt = new PictureCallback() {

      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
         new SaveData().execute(data);
      }
   };
   
   // Async task to save files
   private class SaveData extends AsyncTask<byte[], Void, Void> {
      protected Void doInBackground (byte[]... data) { // photo is in data
         // Check external storage
         if(!isExternalStorageWritable())
            return null;
         
         File fout;

         if (data[0].length == PIC_NUMBER)
            fout = getOutputFile(".txt");
         else
            fout = getOutputFile(".jpg");

         if (fout == null) {
            Log.d("DeadJim", "Error creating file, check storage permissions.");
            return null;
         }

         // Write data to fofoutto
         try {        
            FileOutputStream fos = new FileOutputStream(fout.getPath());
       
            fos.write(data[0]);
            fos.flush();
            fos.close();
         } 
         catch (FileNotFoundException e) {
            Log.d("DeadJim", "File not found: " + e.getMessage());
         } 
         catch (IOException e) {
            Log.d("DeadJim", "Error accessing file: " + e.getMessage());
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
         
         if(camEnable) {
            photoHandler.postDelayed(startTakingPhotos, 0);

            camEnable = false;
         }
      }
   }

   // Calibrating for noise, linked to Noise Button
   public void CalibrateIt(View view) {
      if(cameraObject != null) {
         in_lett = 'N';
         
         if(camEnable) {
            photoHandler.postDelayed(startTakingPhotos, 0);

            camEnable = false;
         }
      }
   }

   public void StopIt(View view) {
      if (cameraObject != null) {
         camEnable = true;

         photoHandler.removeCallbacks(startTakingPhotos);
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
   private static File getOutputFile(String extention){
      // To be safe, you should check that the SDCard is mounted
      // using Environment.getExternalStorageState() before doing this.
      // Done by isExternalStorageWritable()

      File mStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GammaRay");
      // This location works best if you want the created images to be shared
      // between applications and persist after your app has been uninstalled.

      // Create the storage directory if it does not exist
      if (!mStorageDir.exists()) {
         if (!mStorageDir.mkdirs()){
            Log.d("DeadJim", "failed to create directory");
            return null;
         }
      }

      // Create a media file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      File mFile = null;

      mFile = new File(mStorageDir.getPath() + File.separator +  in_lett + "_" + timeStamp + extention);
           
      return mFile;
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

   @Override
   public void onPause() {
      super.onPause();  // Always call the superclass method first

      // Release the Camera because we don't need it when paused
      // and other activities might need to use it.
      if (cameraObject != null) {
         //cameraObject.stopPreview();
         cameraObject.release();
         cameraObject = null;

         photoHandler.removeCallbacks(startTakingPhotos);
      }
   }

   @Override
   public void onResume() {
      super.onResume();  // Always call the superclass method first
   
       // Get the Camera instance as the activity achieves full user focus
      if (cameraObject == null) {
         cameraObject = getAvailiableCamera(); // Local method to handle camera init
         cameraObject.startPreview();
      }
   }
}