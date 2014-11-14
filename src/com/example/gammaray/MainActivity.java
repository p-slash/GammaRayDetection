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
import android.widget.Toast;

public class MainActivity extends Activity {
   final static int PIC_NUMBER      = 100;         // Bigger numbers take too much time and heat up the phone
   final static int SLEEP_TIME      = 200;         // in ms
   final static int MAX_PIX_VALUE   = 50;
   final static char IN_LETT        = 'E';         // initial letter for file names

   public static int[] allRData     = new int[MAX_PIX_VALUE + 1]; // Java initialize to 0 itself
   public static int[] allBData     = new int[MAX_PIX_VALUE + 1]; // Java initialize to 0 itself
   public static int[] allGData     = new int[MAX_PIX_VALUE + 1]; // Java initialize to 0 itself
   public static int counter        = 0;            // to count how many pictures are taken
   
   private Camera cameraObject;
   private ShowCamera showCamera;
   private Handler photoHandler  = new Handler();   // To take consecutive photos
   private boolean butEnable     = false;           // disable start button if already taking pictures
   
   // Safely open the camera
   private boolean getAvailiableCamera() {
      cameraObject = null;

      try {
         cameraObject = Camera.open();

         Camera.Parameters params = cameraObject.getParameters();
         
         params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);               // Set flash off
         params.setExposureCompensation(params.getMaxExposureCompensation()); // Set max exposure
         params.setJpegQuality(100);                                          // Max jpeg quality
         cameraObject.setParameters(params);                                  // Set parameters

         butEnable = true;

         return true;
      }
      catch (Exception e){
         butEnable = false;
         Log.e("DeadJim", "Error in camera", e);
      }

      return false; 
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

         Toast.makeText(getApplicationContext(), "No of pic: " + counter, Toast.LENGTH_SHORT).show();
         
         saveHisto();
      }
   }

   // Calls SnapIt to save the photo, and later itself
   private Runnable startTakingPhotos = new Runnable() {      
      private int i, j        = 0;      
      private int bmpHeight   = 0;
      private int bmpWidth    = 0;
      private int pix_color;
      private int r, g, b;
      private Bitmap bmp      = null;

      private void doTheThing(byte[] data) {
         if (counter == PIC_NUMBER)
            return;

         bmp = BitmapFactory.decodeByteArray(data , 0, data.length);

         // not successful, capture another photo
         if (bmp == null) {   
            counter--;
            return;
         }

         if (bmpHeight == 0) {
            bmpHeight   = bmp.getHeight();
            bmpWidth    = bmp.getWidth();
         }

         for (i = 0; i < bmpWidth; i++) {
            for (j = 0; j < bmpHeight; j++) {
               
               pix_color = bmp.getPixel(i, j);

               r = Color.red(pix_color);
               g = Color.green(pix_color);
               b = Color.blue(pix_color);
               
               if (r > MAX_PIX_VALUE)              
                  r = MAX_PIX_VALUE;

               if (g > MAX_PIX_VALUE)
                  g = MAX_PIX_VALUE;
               
               if (b > MAX_PIX_VALUE)
                  b = MAX_PIX_VALUE;

               allRData[r]++;
               allGData[g]++;
               allBData[b]++;
            }
         }
         
         // increase the number of pictures
         counter++;

         bmp = null;

         /* save the last picture
         if (counter == PIC_NUMBER)
            saveFile(data);
         */
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
            photoHandler.removeCallbacks(this);
            saveHisto();
            Toast.makeText(getApplicationContext(), "Exp finished.", Toast.LENGTH_SHORT).show();
            return;
         }

         cameraObject.takePicture(null, null, SnapIt);
         cameraObject.startPreview();         
         
         photoHandler.postDelayed(this, SLEEP_TIME);     // run this again SLEEP_TIME after
      }
   };

   public static boolean isExternalStorageWritable() {
      String state = Environment.getExternalStorageState();

      if (Environment.MEDIA_MOUNTED.equals(state))
          return true;
      
      return false;
   }

   // Create a File to save data. extension : .txt or .jpg
   public static File getOutputFile(String extention){
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

      if (extention == ".txt")
         timeStamp = "pixelData";

      mFile = new File(mStorageDir.getPath() + File.separator +  IN_LETT + "_" + timeStamp + extention);
           
      return mFile;
   }

   public static boolean savePic(byte[] data) {
      // Check external storage
      if (!isExternalStorageWritable())    
         return false;
      
      File fout = getOutputFile(".jpg");

      if (fout == null) {
         Log.d("DeadJim", "Error creating file, check storage permissions.");
         return false;
      }

      // Write data to fout
      try {        
         FileOutputStream fos = new FileOutputStream(fout.getPath());
         
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
   
   private static boolean saveHisto() {
      // Check external storage
      if (!isExternalStorageWritable())    
         return false;
      
      File fout = getOutputFile(".txt");

      if (fout == null) {
         Log.d("DeadJim", "Error creating file, check storage permissions.");
         return false;
      }
      /*
       * If file already exists, do sth
      
      if (fout.exists()) {
         Scanner sc = new Scanner(fout);
         if (sc.nextInt() == MAX_PIX_VALUE)
            counter += sc.nextInt();
      }
      */
      
      // Write data to fout
      try {               
         PrintWriter pw = new PrintWriter(fout);
         
         pw.println(MAX_PIX_VALUE);
         pw.println(counter);

         for (int a : allRData)
            pw.println(a);
         
         for (int a : allGData)
            pw.println(a);

         for (int a : allBData)
            pw.println(a);

         pw.flush();
         pw.close();
      } 
      catch (FileNotFoundException e) {
         Log.d("DeadJim", "File not found: " + e.getMessage());
         return false;
      } 

      return true;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      getAvailiableCamera();
      
      showCamera = new ShowCamera(this, cameraObject);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(showCamera);
   }

   @Override
   public void onPause() {
      super.onPause();  // Always call the superclass method first
      
      photoHandler.removeCallbacks(startTakingPhotos);
      photoHandler = null;

      saveHisto();

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
         getAvailiableCamera();  // Local method to handle camera init
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