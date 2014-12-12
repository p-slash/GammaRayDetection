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
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
   // Settings
   final  static int SLEEP_TIME   = 1000;  // in ms
   final  static int MAX_PIX_VALUE= 255;
   public static int C_PIC_NUMBER = 10;

   // Change with text
   public static int PIC_NUMBER  = 50;  // Bigger numbers take too much time and heat up the phone
   public static int THRESHOLD   = 55;
   public static boolean hMode   = false;

   public static int pic_counter;   // to count how many pictures are taken
   public static int gam_counter;   // to count how many gamma ray signals detected
   public static Bitmap combinedPic;
   public static int[] histoData;
   public static File fPixOut = null;           // to write the location and the value of the pixels that are above threshold
   public static PrintWriter pwPixOut = null;

   public static TextView outText;
   private EditText picNoText;
   private EditText thText;
   private EditText combText;
   private CheckBox histoMode;
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
      catch (Exception e) {
         butEnable = false;
         Log.e("DeadJim", "Error in camera", e);
      }

      return false; 
   } 

   // Starting experiment, linked to Start Button
   public void StartIt(View view){
      PIC_NUMBER  = Integer.parseInt(picNoText.getText().toString());
      THRESHOLD   = Integer.parseInt(thText.getText().toString());
      C_PIC_NUMBER= Integer.parseInt(combText.getText().toString());
      hMode       = histoMode.isChecked();

      if ((cameraObject != null) && butEnable && (PIC_NUMBER != 0) && (THRESHOLD < 255) && (C_PIC_NUMBER != 0)) {
         pic_counter = 0;
         gam_counter = 0;
         combinedPic = null;

         // Check external storage
         if (!isExternalStorageWritable())    
            return;
         
         fPixOut = getOutputFile("PixList.txt");

         if (fPixOut == null) {
            outText.setText("Error creating file");
            Log.d("DeadJim", "Error creating file, check storage permissions.");
            return;
         }
         
         try {              
            pwPixOut = new PrintWriter(fPixOut); 
         }
         catch (FileNotFoundException e) {
            Log.d("DeadJim", "File not found: " + e.getMessage());
            return;
         } 
         
         if (hMode)
            histoData = new int[MAX_PIX_VALUE + 1];

         photoHandler.postDelayed(startTakingPhotos, 0);

         butEnable = false;
      }
   }

   public void StopIt(View view) {
      if ((cameraObject != null) && !butEnable) {
         butEnable = true;

         photoHandler.removeCallbacks(startTakingPhotos);

         //Toast.makeText(getApplicationContext(), "No of gamma: " + gam_counter, Toast.LENGTH_LONG).show();
         
         saveGamma();
         pwPixOut.flush();
         pwPixOut.close();
      }
   }

   // Calls SnapIt to save the photo, and later itself
   private Runnable startTakingPhotos = new Runnable() {            
      private int bmpHeight   = 0;
      private int bmpWidth    = 0;
      private int pix_color;
      private int r, g, b;
      private int i, j;
      private Bitmap bmp      = null;

      private void doTheThing(byte[] data) {
         if (pic_counter == PIC_NUMBER)
            return;

         bmp = BitmapFactory.decodeByteArray(data , 0, data.length);

         // not successful, capture another photo
         if (bmp == null)
            return;

         if (bmpHeight == 0) {
            bmpHeight   = bmp.getHeight();
            bmpWidth    = bmp.getWidth();
         }

         if (combinedPic == null)
            combinedPic = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);

         for (i = 0; i < bmpWidth; i++) {
            for (j = 0; j < bmpHeight; j++) {
               
               pix_color  = bmp.getPixel(i, j);

               r = Color.red(pix_color);
               g = Color.green(pix_color);
               b = Color.blue(pix_color);

               if (r > THRESHOLD || b > THRESHOLD || g > THRESHOLD)
                  combinedPic.setPixel(i, j, pix_color);
                  
               if (r > THRESHOLD) {
                  gam_counter++;
                  pwPixOut.println(r + ", " + pic_counter + ", " + i + ", " + j + ", red");
               }
               if (b > THRESHOLD) {
                  gam_counter++;
                  pwPixOut.println(r + ", " + pic_counter + ", " + i + ", " + j + ", blue");
               }
               if (g > THRESHOLD) {
                  gam_counter++;
                  pwPixOut.println(r + ", " + pic_counter + ", " + i + ", " + j + ", green");
               }

               if (hMode) {
                  histoData[r]++;
                  histoData[g]++;
                  histoData[b]++;
               }
            }
         }
         
         // increase the number of pictures
         pic_counter++;

         // Save combined picture after every C_PIC_NUMBER of pictures
         if (pic_counter%C_PIC_NUMBER == 0) {
            savePic(combinedPic);
            combinedPic = null;
            Log.d("DeadJim", "Combined picture saved.");
         }

         bmp = null;

         Toast.makeText(getApplicationContext(), "Picture:" + pic_counter, Toast.LENGTH_SHORT).show();
         Log.d("DeadJim", "Number of pic:" + pic_counter);
         return;
      }
      
      // Called after capturing the photo. Creates a handler to manage data.
      private PictureCallback SnapIt = new PictureCallback() {
         @Override
         public void onPictureTaken(byte[] data, Camera camera) {
            doTheThing(data);
            return;  
         }
      };

      public void run() {
         if (pic_counter == PIC_NUMBER) {                    // stop
            photoHandler.removeCallbacks(this);

            Toast.makeText(getApplicationContext(), "Exp finished: " + gam_counter, Toast.LENGTH_LONG).show();

            saveGamma();
            pwPixOut.flush();
            pwPixOut.close();
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
         timeStamp = "SomeNumbers_" + timeStamp;
      else if (extention == ".png")
         timeStamp = "CombinedPicture_" + timeStamp;

      mFile = new File(mStorageDir.getPath() + File.separator +  timeStamp + extention);
           
      return mFile;
   }

   public static boolean savePic(Bitmap data) {
      // Check external storage
      if (!isExternalStorageWritable())    
         return false;
      
      File fout = getOutputFile(".png");

      if (fout == null) {
         Log.d("DeadJim", "Error creating file, check storage permissions.");
         return false;
      }

      // Write data to fout
      try {        
         FileOutputStream fos = new FileOutputStream(fout.getPath());
         
         data.compress(Bitmap.CompressFormat.PNG, 100, fos);
         
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

   private static boolean saveGamma() {
      String msg = "Detection finished.\n";
      msg += "Pictures taken: " + pic_counter + "\n";
      msg += "Combined pictures: " + Math.floor(pic_counter/C_PIC_NUMBER) + "\n";
      msg += "Possible gamma signals: " + gam_counter;
      
      outText.setText(msg);

      // Check external storage
      if (!isExternalStorageWritable())    
         return false;
      
      File fout = getOutputFile(".txt");

      if (fout == null) {
         Log.d("DeadJim", "Error creating file, check storage permissions.");
         return false;
      }
      
      // Write data to fout
      try {               
         PrintWriter pw = new PrintWriter(fout);

         pw.println(msg);

         if (hMode) {
            pw.println("Histogram starts below from 0 to 255 pixel values.");

            for (int a : histoData)
               pw.println(a);
         }

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

      // Prevent screen from turning off
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

      picNoText   = (EditText) findViewById(R.id.pic_no);
      thText      = (EditText) findViewById(R.id.threshold);
      combText    = (EditText) findViewById(R.id.cmb_no);
      outText     = (TextView) findViewById(R.id.outText);
      histoMode   = (CheckBox) findViewById(R.id.histo_mode);

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

      //saveHisto();

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