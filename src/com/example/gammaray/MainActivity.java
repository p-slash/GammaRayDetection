package com.example.gammaray;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
//import android.widget.Toast;

public class MainActivity extends Activity {
   final static int PIC_NUMBER   = 100;
   final static int PIX_NUMBER   = 10;
   final static int NOISE_LENGTH = PIC_NUMBER * PIX_NUMBER;
   final static int SLEEP_TIME   = 500;            // in ms

   private Camera cameraObject;
   private ShowCamera showCamera;
   private Handler photoHandler = new Handler();   // To take consecutive photos
   private boolean butEnable = false;              // disable start button if already taking pictures
   
   private static char in_lett = 'E';              // to distinguish noise and experimental files

   // Safely open the camera
   private Camera getAvailiableCamera() {
      Camera object = null;

      try {
         object = Camera.open();

         Camera.Parameters params = object.getParameters();
         
         params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);               // Set flash off
         params.setExposureCompensation(params.getMaxExposureCompensation()); // Set max exposure
         params.setJpegQuality(100);                                          // Max jpeg quality
         object.setParameters(params);                                        // Set parameters

         butEnable = true;
      }
      catch (Exception e){
         butEnable = false;
         Log.e("DeadJim", "Error in camera", e);
      }

      return object; 
   } 

   // Starting experiment, linked to Start Button
   public void StartIt(View view){
      if ((cameraObject != null) && butEnable) {
         photoHandler.postDelayed(startTakingPhotos, 0);

         butEnable = false;
      }
   }

   public void StopIt(View view) {
      if ((cameraObject != null) && !butEnable) {
         butEnable = true;

         photoHandler.removeCallbacks(startTakingPhotos);
      }
   }

   // Calls SnapIt to save the photo, and later itself
   private Runnable startTakingPhotos = new Runnable() {
      private int counter = 0;         // to count how many pictures are taken
      private Bitmap bmp = null;
      private int index = 0;
      private int coord = 10;
      private byte noiseData[] = new byte[NOISE_LENGTH];

      private boolean isExternalStorageWritable() {
         String state = Environment.getExternalStorageState();

         if (Environment.MEDIA_MOUNTED.equals(state))
             return true;
         
         return false;
      }

      // Create a File to save data. extension : .txt or .jpg
      private File getOutputFile(String extention){
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

         // Create a file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         File mFile = null;

         mFile = new File(mStorageDir.getPath() + File.separator +  in_lett + "_" + timeStamp + extention);
              
         return mFile;
      }

      private boolean saveFile(byte[] data) {
         // Check external storage
         if (!isExternalStorageWritable())    
            return false;
         
         File fout;
         boolean isTxt = (data.length == NOISE_LENGTH);  // true if we are going to save noiseData

         if (isTxt)
            fout = getOutputFile(".txt");
         else
            fout = getOutputFile(".jpg");

         if (fout == null) {
            Log.d("DeadJim", "Error creating file, check storage permissions.");
            return false;
         }

         // Write data to fout
         try {        
            FileOutputStream fos = new FileOutputStream(fout.getPath(), true);
            
            if (isTxt) {
               fos.write(Integer.toString(PIC_NUMBER).getBytes());
               fos.write(",".getBytes());

               fos.write(Integer.toString(PIX_NUMBER).getBytes());               

               for (byte a : data) {
                  fos.write(",".getBytes());
                  fos.write(Byte.toString(a).getBytes());
               }

            }
            else
               fos.write(data);
            
            fos.flush();
            fos.close();
         } 
         catch (FileNotFoundException e) {
            Log.d("DeadJim", "File not found: " + e.getMessage());
            return false;
         } 
         catch (IOException e) {
            Log.d("DeadJim", "Error accessing file: " + e.getMessage());
            return false;
         }

         return true;
      }

      private void doTheThing(byte[] data) {
         if (counter == PIC_NUMBER)
            return;

         bmp = BitmapFactory.decodeByteArray(data , 0, data.length);

         // not successful, capture another photo
         if (bmp == null) {   
            counter--;
            return;
         }

         index = counter * PIX_NUMBER;
         coord = 10;

         for (int i = 0; i < PIX_NUMBER; i++) {
            coord += 50;

            // get red comp of the pixel
            noiseData[index + i] = (byte)(Color.red(bmp.getPixel(coord , coord)));  
         }         
         
         // increase the number of pictures
         counter++;
         
         // save the last picture
         if (counter == PIC_NUMBER)
            saveFile(data);
         
         Log.d("DeadJim", "Number of pic:" + counter);
         return;
      }
      
      // Called after capturing the photo. Creates a asynctask to manage data.
      private PictureCallback SnapIt = new PictureCallback() {
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
            doTheThing(data);
            return;  
         }
      };

      public void run() {
         if (counter == PIC_NUMBER) {                    // enough data
            saveFile(noiseData);                         // save it
            photoHandler.removeCallbacks(this);
            return;
         }

         cameraObject.takePicture(null, null, SnapIt);
         cameraObject.startPreview();         
         
         photoHandler.postDelayed(this, SLEEP_TIME);     // run this again SLEEP_TIME after
      }
   };

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
      
      photoHandler.removeCallbacks(startTakingPhotos);
      photoHandler = null;

      // Release the Camera because we don't need it when paused
      // and other activities might need to use it.
      if (cameraObject != null) {
         cameraObject.release();
         cameraObject = null;         
      }
   }

   @Override
   public void onResume() {
      super.onResume();                         // Always call the superclass method first
      photoHandler = new Handler();
      
      // Get the Camera instance as the activity achieves full user focus
      if (cameraObject == null) {
         cameraObject = getAvailiableCamera();  // Local method to handle camera init
         cameraObject.startPreview();
      }
   }

   /*
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   */
    //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
}