package com.example.dropboxapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;


public class ImageDisplay extends Activity {
	private ImageView imageView1;
	private String coordinates;
	private TextView textView;
	private Bitmap bmp;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imgdisplay);
		
		byte[] byteArray = getIntent().getByteArrayExtra("BitmapImage");
		coordinates = getIntent().getExtras().getString("Coordinates");
		
		//Setting text view and image view to coordinates and picture
		bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);		
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		textView = (TextView) findViewById(R.id.textView1);
		textView.setText("\n"+coordinates+"\n");
		imageView1.setImageBitmap(bmp);
		   
		
		}

}
