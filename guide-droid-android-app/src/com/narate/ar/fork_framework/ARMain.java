package com.narate.ar.fork_framework;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.LocalDataSource;
import com.jwetherell.augmented_reality.data.MyNavigatorDataSource;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
import com.narate.ar.fork_framework.place.Place;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;

/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class ARMain extends AugmentedReality {
	private static final String TAG = "AR MainActivity";
	private static final String locale = Locale.getDefault().getLanguage();
	private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
			1);
	private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(
			1, 1, 20, TimeUnit.SECONDS, queue);
	private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();
	private NetworkDataSource myDataSource;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
	private float slat = 0, slng = 0;

	// private LocationManager location = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocationManager location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					this);
		} else {
			GPSAlert();
		}
		if (location.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, this);
		}

		LocalDataSource localData = new LocalDataSource(this.getResources());
		ARData.addMarkers(localData.getMarkers());

		myDataSource = new MyNavigatorDataSource(this.getResources());
		sources.put("my_data_source", myDataSource);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();

		Location last = ARData.getCurrentLocation();
		slat = (float) last.getLatitude();
		slng = (float) last.getLongitude();
		updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);

		updateData(location.getLatitude(), location.getLongitude(),
				location.getAltitude());
		slat = (float) location.getLatitude();
		slng = (float) location.getLongitude();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void markerTouched(Marker marker) {
		/*
		 * Toast t = Toast.makeText(getApplicationContext(), marker.getName(),
		 * Toast.LENGTH_SHORT); t.setGravity(Gravity.CENTER, 0, 0); t.show();
		 */
		double distance = marker.getDistance();
		String description = marker.getDescription();

		if (distance < 1000.0) {
			description += " (~" + DECIMAL_FORMAT.format(distance) + " m.)";
		} else {
			double d = distance / 1000.0;
			description += " (~" + DECIMAL_FORMAT.format(d) + " km.)";
		}
		showDialogOpen(marker.getPlace());
		// showDialogOpen(marker.getId(), marker.getTitle(), description,
		// marker.getLatitude(), marker.getLongitude());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateDataOnZoom() {
		super.updateDataOnZoom();
		Location last = ARData.getCurrentLocation();
		updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
	}

	private void GPSAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS not enabled please enable your GPS service")
				.setCancelable(false)
				.setPositiveButton("OK, take it",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent gpsOptionsIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(gpsOptionsIntent);
							}
						})
				.setNegativeButton("No, Don't use GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});

		AlertDialog alert = builder.create();
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle("Enable GPS Service");
		alert.show();
	}

	private void updateData(final double lat, final double lon, final double alt) {
		try {
			exeService.execute(new Runnable() {
				@Override
				public void run() {
					for (NetworkDataSource source : sources.values())
						download(source, lat, lon, alt);
				}
			});
		} catch (RejectedExecutionException rej) {
			Log.w(TAG, "Not running new download Runnable, queue is full.");
		} catch (Exception e) {
			Log.e(TAG, "Exception running download Runnable.", e);
		}
	}

	private static boolean download(NetworkDataSource source, double lat,
			double lon, double alt) {
		if (source == null)
			return false;

		String url = null;
		try {
			url = source.createRequestURL(lat, lon, alt, ARData.getRadius(),
					locale);
		} catch (NullPointerException e) {
			return false;
		}

		List<Marker> markers = null;
		try {
			markers = source.parse(url);
		} catch (NullPointerException e) {
			return false;
		}

		ARData.addMarkers(markers);
		return true;
	}

	
	public void showDialogOpen(final Place place) {
		
		final SpannableString s = new SpannableString(place.getDescription());
		Linkify.addLinks(s, Linkify.ALL);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(s)
				.setCancelable(false)
				.setPositiveButton("Map View",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								Intent browserIntent = new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("http://maps.google.com/maps?saddr="
												+ slat
												+ ","
												+ slng
												+ "&daddr="
												+ place.getLatitude()
												+ ","
												+ place.getLongitude()));
								startActivity(browserIntent);
							}
						})
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						})
				.setNeutralButton("Details",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(ARMain.this,
										ShowSelectedPlace.class);
								
								intent.putExtra("id", place.getLocation_id());
								intent.putExtra("category",
										place.getLocation_cate_id());
								intent.putExtra("title", place.getTitle());
								intent.putExtra("latitude", place.getLatitude());
								intent.putExtra("longitude",
										place.getLongitude());
								intent.putExtra("description",
										place.getDescription());
								intent.putExtra("owner", place.getOwner_name());
								intent.putExtra("distance", place.getDistance());

								intent.putExtra("slat", slat + "");
								intent.putExtra("slng", slng + "");

								startActivityForResult(intent, 0);
							}
						});

		AlertDialog alert = dialog.create();
		alert.setTitle(place.getTitle());
		alert.show();
	}
}
