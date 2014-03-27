package com.example.dropboxapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.ExecutionException;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;


public class PostLink extends Activity {
	
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private DbxAccountManager mDbxAcctMgr;
	private DbxFileSystem dbxFs;
	private DbxPath rootpath; 
	private DbxFile image,imageToSync,imageToSend,imageToMap;
	private List<DbxFileInfo> folderContents;
	private ArrayList<String> names = null;
	private String[] fileNames = null;
	private ListView photoList;
	private ArrayAdapter<String> listViewAdapter;
	private Context mContext;
	private FileInputStream fis;
	private Bitmap bitmap1,bitmapToSend;
	private ProgressDialog loader;
	private Display display;
	private int width,height;
	private Uri fileUri;
	private File mediaFile;
	private HashMap<Bitmap, Coordinates> pictureCoordinateSet;
	private CurrentLocation cL;
	private Coordinates c,coordinateSet;
	private BitmapFactory.Options options;
	private LocationManager locationManager;
	private SharedPreferences sp;
	private int listSize;
	private String feedURL;
	public String cityName = "";
	public Geocoding g;
	private Bitmap[] listOfImages;
	private TextView textView;
	private DbxFileInfo element;
	private DbxPath Dfile;
	private String fileName;
	private Intent intentPhoto;
	private Intent mapIntent;
	
	
	@Override

 	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.postlink);
	display = getWindowManager().getDefaultDisplay();
	
	width = display.getWidth();
	height = display.getHeight();
	
	//Setting up the account manager..
	mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "l59nplqoyij0rwg" , "y17w2sbno1c1c1p");
	
	//Getting linked account
	try {
		dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
	} catch (Unauthorized e) {
		
		e.printStackTrace();
	}
	rootpath = new DbxPath("/");
	
	try {
		folderContents = dbxFs.listFolder(rootpath);//Getting path of root folder
	} catch (DbxException e) {
		
		e.printStackTrace();
	}
	names = new ArrayList<String>();
	
	//Getting names of files and storing them to a string array to be later used in GridView..
	for (Iterator<DbxFileInfo> iter = folderContents.iterator(); iter.hasNext(); ) {
	    element = iter.next();
	    Dfile = element.path;
	    fileName = Dfile.getName();
	    
	    names.add(fileName);    
	}
	if(names.size() == 0)
	{
		textView = (TextView)findViewById(R.id.textView1);
		textView.setText("No files");
	}
	
	fileNames = names.toArray(new String[names.size()]);
	
	//Setting up the photo list..
	photoList=(ListView)findViewById(R.id.listView1);
	mContext = getApplicationContext();
	listViewAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,fileNames);
	
	photoList.setBackgroundColor(Color.BLACK);
	photoList.setAdapter(listViewAdapter);
	
	//Setting up image display on click
	photoList.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1,
                int arg2, long arg3) {
        		try {
        			 loader = ProgressDialog.show(PostLink.this, "Please wait ...", "Downloading Image ...", true);
        			 loader.setCancelable(true);
        			 final int position = arg2;
        			 new Thread(new Runnable() {
        				
        					@Override
        					public void run() {
        						try {/*Retrieving,compressing and resizing image bitmap and sending it in intent to next activity 
        								to display it */
        		        			 image = dbxFs.open(new DbxPath("/"+photoList.getItemAtPosition(position).toString()));
        							 fis = image.getReadStream();
        							 bitmap1 = BitmapFactory.decodeStream(fis);
        							 sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        							 String coordinates = sp.getString(photoList.getItemAtPosition(position).toString(), "No coordinates found");
        							 //Getting coordinates from SharedPreferences..
        							 //Adjusting aspect ratio..
        							 if(bitmap1.getWidth()>bitmap1.getHeight())
        							 {
        							 bitmap1 = Bitmap.createScaledBitmap(bitmap1, width, 3*width/4, false);
        							 }
        							 else
        							 {
        								 bitmap1 = Bitmap.createScaledBitmap(bitmap1, 3*height/4, height, false); 
        							 }
        						     ByteArrayOutputStream stream = new ByteArrayOutputStream();
        						     bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
        						     byte[] bitmapBytes = stream.toByteArray();
        						     Intent intent = new Intent(PostLink.this,ImageDisplay.class);
        							 //Passing image coordinates and bitmaps in intent..
        							 intent.putExtra("BitmapImage", bitmapBytes);
        							 intent.putExtra("Coordinates", coordinates);
        							 startActivity(intent);
        						
        						} catch (Exception e) {
        							
        						}
        						loader.dismiss();
        					}
        				}).start();

				} catch (InvalidPathException e) {
					
					e.printStackTrace();
				}
          
        }

        });
	
	}
	
	//Starting Camera Intent
	public void takePhoto(View v) throws InterruptedException, ExecutionException
	{
		 // create Intent to take a picture and return control to the calling application
	    intentPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        Toast.makeText(getApplicationContext(), "Please enable your GPS", Toast.LENGTH_LONG).show();
	    }   
	    g = new Geocoding();//Starting Gecoding API..
		g.execute();
		g.get();
	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	   

	    // start the image capture Intent
	    startActivityForResult(intentPhoto, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	
	public void refreshFolder(View v)
	{
		finish();
		textView = (TextView) findViewById(R.id.textView1);
		if(names.size()>0)
		textView.setVisibility(View.GONE);
		startActivity(getIntent());
	}
	//Incmoplete feature photoMap
	//Tried implementing the photo map feature by sending a hashmap of images and coordinates..
	public void openMap(View v){
		listSize = photoList.getCount();
		listOfImages = new Bitmap[listSize];
		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		pictureCoordinateSet = new HashMap<Bitmap, Coordinates>();
		
		final ProgressDialog mapDialog = ProgressDialog.show(PostLink.this, "Please wait ...",	"Setting up map..", true);
		mapDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for(int position = 0; position < listSize; position++ ) 
					{
						try {
							/* Tried making the thumbnail folder for the map */
							//imageToSend = dbxFs.open(new DbxPath("/mapThumbs"+ File.separator + photoList.getItemAtPosition(position).toString()));
							Log.i("path","/mapThumbs/"+photoList.getItemAtPosition(position).toString());
							fis = imageToSend.getReadStream();
							
							bitmapToSend = BitmapFactory.decodeStream(fis); 
							
							//bitmapToSend = decodeFile(fis);
							Log.i("size!!","iss");
							
							String name = photoList.getItemAtPosition(position).toString();
							float lat = sp.getFloat(name+"lattitude", 999);
							float lon = sp.getFloat(name+"longitude", 999);
							coordinateSet = new Coordinates(lat, lon);
							pictureCoordinateSet.put(bitmapToSend, coordinateSet);
							imageToSend.close();
						
						} catch (InvalidPathException e) {
							
							e.printStackTrace();
						} catch (DbxException e) {
						
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						
					}
				
				} catch (Exception e) {

				}
				
			}
		}).start();

		
		mapIntent = new Intent(this,Map.class);
		mapIntent.putExtra("hashmap", pictureCoordinateSet);
		mapIntent.putExtra("size", listSize);
		mapDialog.dismiss();
		startActivity(mapIntent);
	}
	
	//This function will build the Uri and name for the image to be stored
	private Uri getOutputMediaFileUri(int type) throws InterruptedException, ExecutionException {
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
		
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("Camera Pictures", "failed to create directory");
	            return null;
	        }
	}
	
	    
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	   
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        cityName + timeStamp + ".jpg");
	        
	        Log.i("path",mediaStorageDir.getPath());

	   
	    	
			
	    } else {
	        return null;
	    }

	    return Uri.fromFile(mediaFile);
	}
	
	//This function will operate post the camera intent and will save the image to dropbox
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	try {
	        		//Retrieving image from sd card and attempting to create thumbnails
	        		File pictureFile = new File("/sdcard/MyCameraApp/"+mediaFile.getName());
//	        		File mapFile = new File("/sdcard/MapThumbs/"+mediaFile.getName());
	        	 
//	        		mapFile.createNewFile();
//	        		FileChannel src = new FileInputStream(pictureFile).getChannel();
//	        		FileChannel dest = new FileOutputStream(mapFile).getChannel();
//	        		dest.transferFrom(src, 0, src.size());
//	        		

	        		
	        		options = new BitmapFactory.Options();
	        		
//	        		Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/MapThumbs/"+mediaFile.getName(), options);
//	        		bitmap = bitmap.createScaledBitmap(bitmap, 100, 100, false);
//	        		FileOutputStream fOut = new FileOutputStream(mapFile);

//	        		bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
//	        		fOut.flush();
//	        		fOut.close();
	        		
	        		 
	        		//Getting the current location
	        		cL = new CurrentLocation();
	        		c = cL.getCurrentLocation(locationManager);
	        		
	        		options = new BitmapFactory.Options();
	        		//Storing coordinates in Shared Preferences
	        		sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        		Editor edit = sp.edit();
	        		edit.putString(mediaFile.getName(), String.valueOf(c.getLattitude())+","+String.valueOf(c.getLongitude()));
	    			edit.putFloat(mediaFile.getName()+"lattitude", c.getLattitude());
	    			edit.putFloat(mediaFile.getName()+"longitude", c.getLongitude());
	        		
	    			edit.commit();
	        		
	        		try {
	        			//Syncing image to dropbox
						imageToSync = dbxFs.create(new DbxPath("/"+pictureFile.getName()));
						//Attempting to sync thumbnail
						//imageToMap = dbxFs.create(new DbxPath("/mapThumbs/"+pictureFile.getName()));
						
						imageToSync.writeFromExistingFile(pictureFile, false);
						//imageToMap.writeFromExistingFile(mapFile, false);
						imageToSync.close();
						
						//imageToMap.close();
					} catch (DbxException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}

				} catch (InvalidPathException e) {
					
					e.printStackTrace();
				}
	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        }
	    }

	   
	}

//Using the Google Geocoding API to look up city for image names
	public class Geocoding extends AsyncTask<Void,Void,Void>
	{
		ProgressDialog dialog;
		@Override
		protected void onPostExecute(Void result)
		{
			dialog.dismiss();
			
			super.onPostExecute(result);
			
		}
		
		@Override
		protected void onPreExecute()
		{
			dialog = new ProgressDialog(PostLink.this);
			dialog.setTitle("Processing location..");
			dialog.show();
			
			
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			  CurrentLocation d = new CurrentLocation();
			  Coordinates current = d.getCurrentLocation(locationManager);
			 
			  feedURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+Float.toString(current.getLattitude())+","+Float.toString(current.getLongitude())+"&sensor=false&result_type=locality&key=AIzaSyBwO3eVAp7L0VEy2GM-wvcQYtxNpvwD2OM"
					  ;
			  HttpClient client = new DefaultHttpClient();
			  HttpGet getRequest = new HttpGet(feedURL);
			  try {
			  org.apache.http.HttpResponse response =  client.execute(getRequest);
			  StatusLine statusline = response.getStatusLine();
			  int statusCode = statusline.getStatusCode();
			  
			  InputStream jsonStream = response.getEntity().getContent();
			  BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
			  StringBuilder builder = new StringBuilder();
			  String line;
			  while((line = reader.readLine())!= null)
				{
					builder.append(line);
				}
			  String jsonData = builder.toString();
			  JSONObject json = new JSONObject(jsonData);
			  JSONArray results = json.getJSONArray("results");
			  Log.i("results","");
			  JSONObject object = results.getJSONObject(0);
			  JSONArray addressComponents = object.getJSONArray("address_components");
			  JSONObject city = addressComponents.getJSONObject(0);
			  cityName = city.getString("long_name");
//Finally getting city name after JSON parsing			 
			  
		
				
			} catch (ClientProtocolException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			 
			return null;
		}
		
	}
}
