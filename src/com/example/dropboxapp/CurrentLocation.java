package com.example.dropboxapp;


import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class CurrentLocation{

	private Criteria criteria;
	String provider;
	private Location location;
	private Coordinates c;
	

	
	public Coordinates getCurrentLocation(LocationManager locationManager)
	{
		//Getting current GPS coordinates
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);
		float lat = (float) location.getLatitude();
		float lon =  (float) location.getLongitude();
		c = new Coordinates(lat, lon);
		return c;
	}
	
	
}
