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
import com.jwetherell.augmented_reality.data.TwitterDataSource;
import com.jwetherell.augmented_reality.data.WikipediaDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
import com.narate.ar.facebook.LoginWithFacebook;
import com.narate.ar.imagedownloader.ImageListActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class ForkARFrameworkActivity extends ShowRadar {
	private static final String TAG = "ForkARFrameworkActivity";
	private static final String locale = Locale.getDefault().getLanguage();
	private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
			1);
	private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(
			1, 1, 20, TimeUnit.SECONDS, queue);
	private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String, NetworkDataSource>();
	private NetworkDataSource myDataSource;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
	private Button ar_view, list_view, add;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ar_view = (Button) findViewById(R.id.ar_view);
		list_view = (Button) findViewById(R.id.list_view);
		add = (Button) findViewById(R.id.add_location);
		// Local
		LocalDataSource localData = new LocalDataSource(this.getResources());
		ARData.addMarkers(localData.getMarkers());

		myDataSource = new MyNavigatorDataSource(this.getResources());
		sources.put("my_data_source", myDataSource);

		ar_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ForkARFrameworkActivity.this,
						ARMain.class));
			}

		});
		list_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ForkARFrameworkActivity.this,
						ShowListView.class));
			}

		});

		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ForkARFrameworkActivity.this,
						AddForm.class));
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart() {
		super.onStart();

		Location last = ARData.getCurrentLocation();
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void markerTouched(Marker marker) {
		/*
		 * String textStr = null; double distance = marker.getDistance(); String
		 * name = marker.getName(); if (distance < 1000.0) { textStr = name +
		 * " (~" + DECIMAL_FORMAT.format(distance) + " à¸¡.)"; } else { double d =
		 * distance / 1000.0; textStr = name + " (~" + DECIMAL_FORMAT.format(d)
		 * + " à¸�à¸¡.)"; } showDialogOpen("AR Search", textStr);
		 */
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setting:
			startActivity(new Intent(ForkARFrameworkActivity.this,
					Setting.class));
			break;
		case R.id.logout:
			final Intent intent = new Intent(ForkARFrameworkActivity.this,
					LoginWithFacebook.class);
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage("Logout from facebook")
					.setCancelable(false)
					.setPositiveButton("Logout",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub
									startActivity(intent);
								}
							});

			AlertDialog alert = dialog.create();
			alert.setTitle("Login or Logout from facebook ?");
			alert.show();

			break;
		case R.id.about:
			String about = "Guide Droid is a Augmented Reality Navigator\n"
					+ "About message";
			showDialog("About Guide Droid", about);
			break;
		}
		return true;
	}

	public void showDialog(String title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(message).setCancelable(false)
				.setPositiveButton("Dialog", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub

					}
				});

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}

	public void showDialogOpen(String title, String message) {
		final SpannableString s = new SpannableString(message);
		Linkify.addLinks(s, Linkify.ALL);
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(s)
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(
										ForkARFrameworkActivity.this,
										ForkARFrameworkActivity.class);
								startActivityForResult(intent, 0);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}
}
