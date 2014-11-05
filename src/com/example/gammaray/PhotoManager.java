package com.example.gammaray;

import android.app.IntentService;
import android.content.Intent;

public class PhotoManager extends IntentService {
	
    public PhotoManager() {
		super("PhotoManager");
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        byte[] picData = workIntent.getDataString();
        
        // Do work here, based on the contents of dataString
        
    }
}