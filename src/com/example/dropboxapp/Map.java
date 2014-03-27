package com.example.dropboxapp;

import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

public class Map extends Activity {
	private HashMap<Bitmap, Coordinates> hashMap;

	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.map);
	        
	        Intent intent = getIntent();
	        hashMap = (HashMap<Bitmap, Coordinates>)intent.getSerializableExtra("hashmap");
	        Log.i("size","is"+hashMap.size());
	        int size = intent.getExtras().getInt("size");
	        
	    	for (Entry<Bitmap, Coordinates> entry : hashMap.entrySet()) {
	    		
			    Log.i("width","is"+entry.getKey().getWidth());
			    Log.i("width","is"+entry.getKey().getHeight());
			    
			}	
	        
	    }
}
