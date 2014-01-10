package com.veloxdroid.beans;

import android.location.Location;

public class Autovelox  {

	private Location location;
	private int maxSpeed;
	//private String type;
	public enum Type {FISSO, MOBILE, ALTRO};
	private Type type;
	
	public Autovelox(Location location, int maxSpeed, Type t) {
		super();
		this.location = location;
		this.maxSpeed = maxSpeed;
		this.type = t;
	}
	
	public Autovelox(){
		location = new Location("Custom");
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	
	
}
