package com.example.dropboxapp;

public class Coordinates {

	private float lattitude;
	private float longitude;
	
	public Coordinates(float lat,float lon)
	{
		lattitude = lat;
		longitude = lon;	
	}
	
	public float getLattitude()
	{
		return lattitude;
	}
	
	public float getLongitude()
	{
		return longitude;
	}

}
