package com.narate.ar.fork_framework.place;

public class Place implements Comparable<Place>{
	String location_id;
	String location_cate_id;
	String title;
	String latitude;
	String longitude;
	float distance;
	String description;
	String owner_name;

	public Place() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Place(String location_id, String location_cate_id, String title,
			String latitude, String longitude, float distance,
			String description, String owner_id) {
		super();
		this.location_id = location_id;
		this.location_cate_id = location_cate_id;
		this.title = title;
		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;
		this.description = description;
		this.owner_name = owner_id;		
	}

	public String getLocation_id() {
		return location_id;
	}

	public void setLocation_id(String location_id) {
		this.location_id = location_id;
	}

	public String getLocation_cate_id() {
		return location_cate_id;
	}

	public void setLocation_cate_id(String location_cate_id) {
		this.location_cate_id = location_cate_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwner_name() {
		return owner_name;
	}

	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
	}

	@Override
	public int compareTo(Place arg0) {
		// TODO Auto-generated method stub		
		return (int)(this.getDistance()*1000) - (int)(arg0.getDistance()*1000);
	}
	
}
