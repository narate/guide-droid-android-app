package com.narate.ar.fork_framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.narate.ar.facebook.LoginWithFacebook;
import com.narate.ar.fork_framework.place.Place;

public class MainActivity extends MapActivity implements LocationListener {
	private Button ar_view, list_view, add;
	public static final String KEY_SERVER = "http://cpre-pjb.kmutnb.ac.th/project/navigator/mobile/request.php";
	// public static final String KEY_SERVER =
	// "http://192.168.1.2/test_edit/mobile/request.php";
	private MapView mapview;
	private MapController mapcontrol;
	// private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	List<OverlayItem> items = new ArrayList<OverlayItem>();

	public static float latitude, longitude;

	List<Place> place;
	private String id = "", category = "", name = "", _latitude = "",
			_longitude = "", description = "", owner = "", distance = "";

	private ProgressDialog pg_dialog;
	private Drawable drawable = null, drawable_c = null;

	private MyMapOverlay itemizedoverlay = null;
	private List<Overlay> mapOverlays = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		LocationManager location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					this);
		}
		if (location.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 0, this);
		}

		/* Get last known location */
		Location last = location
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		latitude = (float) last.getLatitude();
		longitude = (float) last.getLongitude();

		/* find layout id */
		ar_view = (Button) findViewById(R.id.ar_view);
		list_view = (Button) findViewById(R.id.list_view);
		add = (Button) findViewById(R.id.add_location);
		add.setEnabled(false);

		mapview = (MapView) findViewById(R.id.mapview);
		mapview.setClickable(true);
		mapcontrol = mapview.getController();
		mapcontrol.setZoom(16);
		mapview.setBuiltInZoomControls(true);
		// mapview.setSatellite(true);
		// mapview.setStreetView(true);

		mapOverlays = mapview.getOverlays();

		drawable = this.getResources().getDrawable(R.drawable.marker);
		drawable_c = this.getResources().getDrawable(
				R.drawable.android_my_location);

		pg_dialog = ProgressDialog.show(MainActivity.this, "Loading",
				"Loading current location wait a few second");
		pg_dialog.setCanceledOnTouchOutside(true);
		GetOverlayData task = new GetOverlayData();
		task.execute(new String[] { KEY_SERVER + "?" + "lat=" + latitude + "&"
				+ "lng=" + longitude + "&radius=" + "10" });

		// LoadData load = new LoadData();
		// load.execute("Hello");

		mapview.invalidate();

		ar_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(MainActivity.this, ARMain.class));
			}

		});
		list_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						ShowListView.class);
				intent.putExtra("latitude", latitude + "");
				intent.putExtra("longitude", longitude + "");
				startActivityForResult(intent, 0);
			}

		});

		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// LoadData load = new LoadData();
				// load.execute();

				startActivity(new Intent(MainActivity.this, AddForm.class));
			}

		});

	}

	public String GetAddress(GeoPoint point) {
		String address = "";
		Locale locale = new Locale("th", "TH");
		Locale.setDefault(locale);
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocation(
					point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6,
					1);

			if (addresses.size() > 0) {
				for (int index = 0; index < addresses.get(0)
						.getMaxAddressLineIndex(); index++)
					address += addresses.get(0).getAddressLine(index) + " ";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return address;
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6)));
	}

	private List<OverlayItem> getPlaceData(String json) {

		List<OverlayItem> items_return = new ArrayList<OverlayItem>();
		JSONObject json_data, root;
		JSONArray jArray = null;

		try {
			root = new JSONObject(json);
			if (!root.getString("request").equalsIgnoreCase("null")) {
				jArray = root.getJSONArray("request");
			} else {
				return null;
			}

			for (int i = 0; i < jArray.length(); i++) {
				json_data = jArray.getJSONObject(i);

				Log.i("log_tag",
						"\n" + "ID : " + json_data.getString("id")
								+ "\nCATEGOTY : "
								+ json_data.getString("category")
								+ "\nTITLE : " + json_data.getString("title")
								+ "\nLATITUDE : " + json_data.getString("lat")
								+ "\nLONGITUDE : " + json_data.getString("lng")
								+ "\nDESCRIPTION : "
								+ json_data.getString("description")
								+ "\nOWNER : " + json_data.getString("owner")
								+ "\nDISTANCE : "
								+ json_data.getString("distance"));

				id = json_data.getString("id");
				category = json_data.getString("category");
				name = json_data.getString("title");
				_latitude = json_data.getString("lat");
				_longitude = json_data.getString("lng");
				description = json_data.getString("description");
				owner = json_data.getString("owner");
				distance = json_data.getString("distance");

				items_return.add(new OverlayItem(getPoint(
						Double.parseDouble(_latitude),
						Double.parseDouble(_longitude)), name,
						GetAddress(getPoint(Double.parseDouble(_latitude),
								Double.parseDouble(_longitude)))));
			}
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		return items_return;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latitude = (float) location.getLatitude();
		longitude = (float) location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && pg_dialog.isShowing()) {
			pg_dialog.dismiss();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			pg_dialog.dismiss();
		}
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
			startActivity(new Intent(MainActivity.this, Setting.class));
			break;
		case R.id.logout:
			final Intent intent = new Intent(MainActivity.this,
					LoginWithFacebook.class);
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setMessage(
					"Login or Logout from facebook ?")
					.setCancelable(true)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub
									startActivity(intent);
								}
							});

			AlertDialog alert = dialog.create();
			alert.setTitle("Login or Logout ?");
			alert.show();

			break;
		case R.id.about:

			String about = "Guide Droid is a Augmented Reality Navigator\n"
					+ "About";
			showDialog("Guide Droid", about);

			break;
		case R.id.bug:
			Intent emailIntent = new Intent(Intent.ACTION_VIEW);
			emailIntent.setClassName("com.google.android.gm",
					"com.google.android.gm.ComposeActivityGmail");
			emailIntent.setType("text/plain");
			emailIntent.setData(Uri.parse("koonnarate@gmail.com"));
			emailIntent.putExtra(Intent.EXTRA_EMAIL, "koonnarate@gmail.com");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"BUG REPORT - Guide Droid Project");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					"Bug list\n-> ");
			startActivity(emailIntent);
			break;
		}
		return true;
	}

	public void showDialog(String title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub

							}
						});

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}

	private class GetOverlayData extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {

				HttpGet httpGet = new HttpGet(url);

				HttpParams httpParameters = new BasicHttpParams();
				// Set the timeout in milliseconds until a connection is
				// established.
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				// Set the default socket timeout (SO_TIMEOUT)
				// in milliseconds which is the timeout for waiting for data.
				int timeoutSocket = 5000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);

				DefaultHttpClient client = new DefaultHttpClient(httpParameters);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(
							new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
					e.printStackTrace();
					return "Exception ?";
				}
			}

			GeoPoint p = new GeoPoint((int) (latitude * 1E6),
					(int) (longitude * 1E6));

			/* Current location */
			List<OverlayItem> current = new ArrayList<OverlayItem>();
			current.add(new OverlayItem(p, "You are here", GetAddress(p)));

			MyMapOverlay itemizedoverlay_c = new MyMapOverlay(drawable_c,
					MainActivity.this, current);

			items = getPlaceData(response);

			mapcontrol.animateTo(p);
			itemizedoverlay = new MyMapOverlay(drawable, MainActivity.this,
					items);
			mapOverlays.add(itemizedoverlay);
			mapOverlays.add(itemizedoverlay_c);

			// return response;
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			pg_dialog.dismiss();
			if (!result.equalsIgnoreCase("")) {
				add.setEnabled(false);
				showDialog("Post Execute", result);
				GeoPoint p = new GeoPoint((int) (latitude * 1E6),
						(int) (longitude * 1E6));

				/* Current location */
				List<OverlayItem> current = new ArrayList<OverlayItem>();
				current.add(new OverlayItem(p, "Current location", GetAddress(p)));

				MyMapOverlay itemizedoverlay_c = new MyMapOverlay(drawable_c,
						MainActivity.this, current);

				mapcontrol.animateTo(p);
				mapOverlays.add(itemizedoverlay_c);
			} else {
				add.setEnabled(true);
			}
		}
	}

}