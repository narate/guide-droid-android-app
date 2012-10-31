package com.narate.ar.fork_framework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.narate.ar.fork_framework.place.Place;
import com.narate.ar.fork_framework.place.PlaceAdapter;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ShowListView extends ListActivity implements LocationListener {
	// Server URL
	public static final String KEY_SERVER = "http://cpre-pjb.kmutnb.ac.th/project/navigator/mobile/request.php";
	// public static final String KEY_SERVER =
	// "http://192.168.1.2/test_edit/mobile/request.php";
	// LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
	ArrayList<Place> listItems = new ArrayList<Place>();

	// DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
	PlaceAdapter adapter;

	private Spinner radius;
	private double lat = 0.0, lng = 0.0;
	private Button show;

	List<Place> place;
	private String id = "", category = "", name = "", latitude = "",
			longitude = "", description = "", owner = "", distance = "";
	private LocationManager location = null;
	private ProgressDialog pg_dialog;
	private boolean isFoundLocation = false;
	private GetPlaceList task = null;
	private String _radius = "";
	private Toast t = null;
	private final int ENABLE_GPS = 0, SHOW_SELECTED = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);

		show = (Button) findViewById(R.id.show);

		radius = (Spinner) findViewById(R.id.radius);
		ArrayAdapter<CharSequence> radius_adapter = ArrayAdapter
				.createFromResource(this, R.array.radius,
						android.R.layout.simple_spinner_item);
		radius_adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		radius.setAdapter(radius_adapter);

		adapter = new PlaceAdapter(this, R.layout.place_list, listItems);
		setListAdapter(adapter);

		_radius = radius.getSelectedItem().toString()
				.substring(0, radius.getSelectedItem().toString().indexOf(" "));

		location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		/*
		 * Check location service if GPS enable load latitude, longitude from
		 * GPS else load latitude, longitude from network location service
		 */
		if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					this);
			/*
			 * Get last known location from GPS to initial pre-load list of
			 * place
			 */
			lat = location.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER).getLatitude();
			lng = location.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER).getLongitude();

			/*
			 * **** Get list of places ****
			 * 
			 * First clear list view and notify data change then execute
			 * GetPlaceList to show list of place in ListView by send Server
			 * URL, latitude, longitude and radius to server and server will
			 * process and response json data back to device
			 * 
			 * Server using $_POST, $_REQUEST to get device sending parameter
			 */
			listItems.clear();
			adapter.notifyDataSetChanged();
			pg_dialog = ProgressDialog.show(ShowListView.this, "Loading",
					"Loading message show list view");
			task = new GetPlaceList();
			task.execute(new String[] { KEY_SERVER, lat + "", lng + "", _radius });
		} else {
			// If GPS disabled alert to prompt user to enable GPS
			GPSAlert();
		}

		// Set event to show button
		show.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Get radius from spinner widget
				_radius = radius
						.getSelectedItem()
						.toString()
						.substring(
								0,
								radius.getSelectedItem().toString()
										.indexOf(" "));
				// Toast.makeText(ShowListView.this, "latitude = " + lat +
				// ", longitude = " + lng, Toast.LENGTH_LONG).show();
				/* Get list of places */
				listItems.clear();
				adapter.notifyDataSetChanged();
				pg_dialog = ProgressDialog.show(ShowListView.this, "Loading",
						"Getting place list...");
				task = new GetPlaceList();
				task.execute(new String[] { KEY_SERVER, lat + "", lng + "",
						_radius });
			}

		});
	}

	/*
	 * GPS alert for prompt user to enable GPS if user accept to enable GPS load
	 * place list by using GPS location else using network location [previous
	 * activity by get extras string intent]
	 */
	private void GPSAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("GPS not enabled please enable your GPS service")
				.setCancelable(false)
				.setPositiveButton("OK, take it",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent gpsOptionsIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(gpsOptionsIntent,
										ENABLE_GPS);
							}
						})
				.setNegativeButton("No, Don't use GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// finish();
								t = Toast
										.makeText(
												ShowListView.this,
												"Use Network Location service",
												Toast.LENGTH_SHORT);
								t.show();
								lat = Double.parseDouble(getIntent()
										.getExtras().getString("latitude"));
								lng = Double.parseDouble(getIntent()
										.getExtras().getString("longitude"));

								isFoundLocation = true;
								listItems.clear();
								adapter.notifyDataSetChanged();
								pg_dialog = ProgressDialog.show(
										ShowListView.this, "Loading",
										"Loaing from network location");
								task = new GetPlaceList();
								task.execute(new String[] { KEY_SERVER,
										lat + "", lng + "", _radius });

							}
						});

		AlertDialog alert = builder.create();
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle("Enable GPS Service");
		alert.show();
	}

	// Show dialog not yet to use
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
						});/*
							 * .setNegativeButton("Close", new
							 * DialogInterface.OnClickListener() {
							 * 
							 * @Override public void onClick(DialogInterface
							 * arg0, int arg1) { // TODO Auto-generated method
							 * stub
							 * 
							 * } });
							 */

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}

	/*
	 * On list item click open new activity to present information by put extra
	 * intent to next activity for pre-load view
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(ShowListView.this, ShowSelectedPlace.class);
		// intent.putExtra("location_id", id_table[(int) id]);
		intent.putExtra("id", place.get((int) id).getLocation_id());
		intent.putExtra("category", place.get((int) id).getLocation_cate_id());
		intent.putExtra("title", place.get((int) id).getTitle());
		intent.putExtra("latitude", place.get((int) id).getLatitude());
		intent.putExtra("longitude", place.get((int) id).getLongitude());
		intent.putExtra("description", place.get((int) id).getDescription());
		intent.putExtra("owner", place.get((int) id).getOwner_name());
		intent.putExtra("distance", place.get((int) id).getDistance());

		intent.putExtra("slat", lat + "");
		intent.putExtra("slng", lng + "");

		startActivityForResult(intent, SHOW_SELECTED);
	}

	// Update latitude, longitude on location change
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		lat = location.getLatitude();
		lng = location.getLongitude();
		if (!isFoundLocation) {
			isFoundLocation = true;
		}
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

	/*
	 * On GPSAlert activity result the result of activity if 0 both enabled GPS
	 * or not to check that using
	 * isProviderEnabled(LocationManager.GPS_PROVIDER) again if enabled load
	 * latitude, longitude from GPS else load from network location and get list
	 * of places
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultcode,
			Intent intent) {
		super.onActivityResult(requestCode, resultcode, intent);
		switch (requestCode) {
		case ENABLE_GPS:
			if (resultcode == 0) { // Using GPS location
				if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					t = Toast.makeText(ShowListView.this,
							"Use GPS location service", Toast.LENGTH_SHORT);
					t.show();
					location.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 0, 0, this);
					lat = location.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER).getLatitude();
					lng = location.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER).getLongitude();

					listItems.clear();
					adapter.notifyDataSetChanged();
					pg_dialog = ProgressDialog.show(ShowListView.this,
							"Load", "Load message");
					task = new GetPlaceList();
					task.execute(new String[] { KEY_SERVER, lat + "", lng + "",
							_radius });

				} else { // Using network location
					t = Toast.makeText(ShowListView.this,
							"Use Network location service",
							Toast.LENGTH_SHORT);
					t.show();
					location.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 0, 0, this);

					lat = location.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER).getLatitude();
					lng = location.getLastKnownLocation(
							LocationManager.NETWORK_PROVIDER).getLongitude();

					listItems.clear();
					adapter.notifyDataSetChanged();
					pg_dialog = ProgressDialog.show(ShowListView.this,
							"Load location", "Load location from network");
					task = new GetPlaceList();
					task.execute(new String[] { KEY_SERVER, lat + "", lng + "",
							_radius });
				}
			}
			break;
		}

	}

	/*
	 * Get place list using AsyncTask to do in background during load data from
	 * server
	 */
	private class GetPlaceList extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			String response_json = "";
			String request = urls[0] + "?" + "lat=" + urls[1] + "&" + "lng="
					+ urls[2] + "&radius=" + urls[3];

			// String returnString = "";
			InputStream is = null;
			String result = "";

			JSONObject json_data, root;
			JSONArray jArray = null;
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			// nameValuePairs.add(new BasicNameValuePair("test", "test"));
			try {
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

				DefaultHttpClient httpclient = new DefaultHttpClient(
						httpParameters);

				// HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(request);

				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
				return "Error with : " + e.toString();
			}

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-11"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				// showDialog("TEST", "NULL request reponse : " + result);
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
				return "Error with : " + e.toString();
			}

			try {
				root = new JSONObject(result);
				if (!root.getString("request").equalsIgnoreCase("null")) {
					jArray = root.getJSONArray("request");
				} else {
					showDialog("Data", ":-)"
							+ radius + " something");
					return "NO MESSAGE";
				}

				String read_json = "";

				place = new ArrayList<Place>();

				for (int i = 0; i < jArray.length(); i++) {
					json_data = jArray.getJSONObject(i);

					Log.i("log_tag",
							"\n" + "ID : " + json_data.getString("id")
									+ "\nCATEGOTY : "
									+ json_data.getString("category")
									+ "\nTITLE : "
									+ json_data.getString("title")
									+ "\nLATITUDE : "
									+ json_data.getString("lat")
									+ "\nLONGITUDE : "
									+ json_data.getString("lng")
									+ "\nDESCRIPTION : "
									+ json_data.getString("description")
									+ "\nOWNER : "
									+ json_data.getString("owner")
									+ "\nDISTANCE : "
									+ json_data.getString("distance"));
					// id_table[i] = json_data.getString("id");
					id = json_data.getString("id");
					category = json_data.getString("category");
					name = json_data.getString("title");
					latitude = json_data.getString("lat");
					longitude = json_data.getString("lng");
					description = json_data.getString("description");
					owner = json_data.getString("owner");
					distance = json_data.getString("distance");

					place.add(new Place(id, category, name, latitude,
							longitude, Float.parseFloat(distance), description,
							owner));

					read_json = name + "\n" + description + "\n";
					// returnString += read_json;
				}
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
				return "Error with : " + e.toString();
			}

			Collections.sort(place);
			for (int i = 0; i < place.size(); i++) {
				listItems.add(place.get(i));
			}

			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			adapter.notifyDataSetChanged();
			pg_dialog.dismiss();
			if (!result.equalsIgnoreCase("")) {
				showDialog("No place list", "Please increase the radius ");
			}
		}
	}
}