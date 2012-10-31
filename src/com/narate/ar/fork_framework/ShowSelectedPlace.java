package com.narate.ar.fork_framework;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.narate.ar.facebook.LoginWithFacebook;
import com.narate.ar.facebook.ShareToFacebook;
import com.narate.ar.fork_framework.photo_upload.Base64;
import com.narate.ar.imagedownloader.ImageAdapter;
import com.narate.ar.imagedownloader.ImageDownloader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer;

public class ShowSelectedPlace extends MapActivity implements LocationListener {
	//private static final String KEY_SERVER = "http://192.168.1.2/test_edit/";
	 private static final String KEY_SERVER =
	 "http://cpre-pjb.kmutnb.ac.th/project/navigator/";
	public static final String KEY_SERVER_GET_PHOTO = KEY_SERVER
			+ "mobile/get_photo_link.php";
	public static final String KEY_SERVER_UPLOAD_DIR = KEY_SERVER + "upload/";
	private static final String KEY_SERVER_PHOTO_UPLOAD = KEY_SERVER
			+ "mobile/mobile_photo_upload.php";
	private TextView title_text, description_text, show_photo;
	private ImageButton take_photo;
	private ImageView image_view;
	private Button add_photo, get_direction, handle;
	private InputStream inputStream;
	// private String location_id = "", location_cate_id = "";
	private String id = "", category = "", name = "", latitude = "",
			longitude = "", description = "", owner_name = "", owner_user_name = "", distance = "";
	private float slat = 0, slng = 0;
	private String dlat = "", dlng = "";
	public static String[] photo_name;
	private String photo_link = "";
	private Bitmap bitmap = null;
	private final int IMAGE_CAPTURE = 0, IMAGE_PICKER = 1;
	private ProgressDialog pg_dialog;
	private SharedPreferences sharedPreferences = null;

	private MapView mapview;
	private MapController mapcontrol;
	private List<Overlay> mapOverlays = null;
	private Drawable drawable = null;

	private SlidingDrawer slide = null;
	private ListView lv;
	private boolean isPhotoLoaded = false;
	private String filePath = "";
	private File file = null;
	private String image_str = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.selected_place);
		View inflatedSlidingDrawerLayout = getLayoutInflater().inflate(
				R.layout.sliding_drawer, null);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		getWindow().addContentView(inflatedSlidingDrawerLayout, params);

		slat = Float.parseFloat(getIntent().getExtras().getString("slat"));
		slng = Float.parseFloat(getIntent().getExtras().getString("slng"));

		id = getIntent().getExtras().getString("id");
		category = getIntent().getExtras().getString("category");
		name = getIntent().getExtras().getString("title");
		latitude = getIntent().getExtras().getString("latitude");
		longitude = getIntent().getExtras().getString("longitude");
		description = getIntent().getExtras().getString("description");
		owner_name = getIntent().getExtras().getString("owner");
		distance = getIntent().getExtras().getString("distance");

		title_text = (TextView) findViewById(R.id.title);
		description_text = (TextView) findViewById(R.id.description);
		take_photo = (ImageButton) findViewById(R.id.take_photo);
		add_photo = (Button) findViewById(R.id.add_photo);
		get_direction = (Button) findViewById(R.id.get_direction);
		handle = (Button) findViewById(R.id.handle);
		lv = (ListView) inflatedSlidingDrawerLayout.findViewById(R.id.list);

		slide = (SlidingDrawer) inflatedSlidingDrawerLayout
				.findViewById(R.id.slide);

		mapview = (MapView) findViewById(R.id.mapview);
		mapview.setClickable(true);
		mapcontrol = mapview.getController();
		mapcontrol.setZoom(16);

		mapOverlays = mapview.getOverlays();
		GeoPoint p = new GeoPoint((int) (Float.parseFloat(latitude) * 1E6),
				(int) (Float.parseFloat(longitude) * 1E6));

		GeoPoint pc = new GeoPoint((int) (slat * 1E6), (int) (slng * 1E6));

		mapcontrol.animateTo(p);

		drawable = this.getResources().getDrawable(R.drawable.marker);
		List<OverlayItem> place_position = new ArrayList<OverlayItem>();
		place_position.add(new OverlayItem(p, name, description));

		List<OverlayItem> place_position_c = new ArrayList<OverlayItem>();
		place_position_c.add(new OverlayItem(pc, "GET ADDRESS",
				GetAddress(pc)));

		MyMapOverlay itemizedoverlay = new MyMapOverlay(drawable,
				ShowSelectedPlace.this, place_position);
		drawable = this.getResources().getDrawable(
				R.drawable.android_my_location);
		MyMapOverlay itemizedoverlay_c = new MyMapOverlay(drawable,
				ShowSelectedPlace.this, place_position_c);

		mapOverlays.add(itemizedoverlay);
		mapOverlays.add(itemizedoverlay_c);

		mapview.invalidate();

		add_photo.setEnabled(false);

		String str = getServerData();
		title_text.setText(getIntent().getExtras().getString("title"));
		description_text.setText(getIntent().getExtras().getString(
				"description"));

		registerForContextMenu(take_photo);
		take_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				takePhoto();
			}

		});

		get_direction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://maps.google.com/maps?saddr=" + slat
								+ "," + slng + "&daddr=" + latitude + ","
								+ longitude));
				startActivity(browserIntent);
			}

		});

		add_photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sharedPreferences = getSharedPreferences("facebook_login",
						MODE_PRIVATE);
				String user = sharedPreferences.getString("name", "");
				if (user.equalsIgnoreCase("")) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							ShowSelectedPlace.this);
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setMessage(
							"Please login to facebook")
							.setCancelable(false)
							.setPositiveButton("Yeah, Log me in",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											// TODO Auto-generated method stub
											startActivityForResult(new Intent(
													ShowSelectedPlace.this,
													LoginWithFacebook.class), 0);
										}
									});

					AlertDialog alert = dialog.create();
					alert.setTitle("Login to facebook");
					alert.show();

				} else {
					PhotoUpload task = new PhotoUpload();
					task.execute(new String[] { KEY_SERVER_PHOTO_UPLOAD });
				}

			}

		});

		slide.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				// Toast.makeText(ShowSelectedPlace.this, "onDrawerOpened",
				// Toast.LENGTH_SHORT).show();
				handle.setBackgroundResource(R.drawable.handle_end);
				// handle.setBackgroundColor(0);
				// handle.setText("OPEN ?");
				if (!isPhotoLoaded) {
					isPhotoLoaded = true;
					lv.setAdapter(new ImageAdapter());
					ImageAdapter ima = new ImageAdapter();
					ImageAdapter.URLS = photo_name;
					ImageDownloader.Mode mode = ImageDownloader.Mode.CORRECT;
					// ImageAdapter.this.setURLS(photo_name);

					ima.getImageDownloader().setMode(mode);

					if (photo_name[0].equalsIgnoreCase("NULL")) {
						Toast.makeText(ShowSelectedPlace.this, "NO PHOTO YET",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		slide.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				// Toast.makeText(ShowSelectedPlace.this, "onDrawerClosed",
				// Toast.LENGTH_SHORT).show();
				handle.setBackgroundResource(R.drawable.handle);
			}

		});

		slide.setOnDrawerScrollListener(new OnDrawerScrollListener() {

			@Override
			public void onScrollEnded() {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScrollStarted() {
				// TODO Auto-generated method stub
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

	private String getServerData() {
		String returnString = "";
		InputStream is = null;
		String result = "";

		String request = KEY_SERVER_GET_PHOTO + "?location_id=" + id;

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
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

			HttpPost httppost = new HttpPost(request);

			// httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
			// "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			// Toast.makeText(this, "ERROR" + e.toString(), Toast.LENGTH_LONG)
			// .show();
			Log.e("log_tag", "Error in http connection " + e.toString());
			showDialog(
					"Error",
					e.toString());
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-11"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
		}

		try {
			JSONObject root = new JSONObject(result);
			JSONArray photo_array = null;
			JSONObject photo_data = null;

			if (!root.getString("photo").equalsIgnoreCase("null")) {
				photo_array = root.getJSONArray("photo");
				if (photo_array.length() != 0) {
					photo_name = new String[photo_array.length()];
				} else {
					photo_name = new String[1];
					photo_name[0] = "NULL";
				}
				for (int p = 0; p < photo_array.length(); p++) {
					photo_data = photo_array.getJSONObject(p);
					photo_name[p] = KEY_SERVER_UPLOAD_DIR + category + "/" + id
							+ "/" + photo_data.getString("photo_link");
					returnString += "- " + photo_data.getString("photo_link")
							+ "\n";
				}
			} else {
				photo_name = new String[1];
				photo_name[0] = "NULL";
			}

		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}

		return returnString;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Take or Pick a Photo");
		menu.add(0, v.getId(), 0, "Take Photo");
		menu.add(0, v.getId(), 0, "Pick Photo");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "Take Photo") {
			takePhoto();
		} else if (item.getTitle() == "Pick Photo") {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, IMAGE_PICKER);
		} else {
			return false;
		}
		return true;
	}

	public String convertResponseToString(HttpResponse response)
			throws IllegalStateException, IOException {

		String res = "";
		StringBuffer buffer = new StringBuffer();
		inputStream = response.getEntity().getContent();
		int contentLength = (int) response.getEntity().getContentLength();
		// Toast.makeText(AddForm.this, "contentLength : " + contentLength,
		// Toast.LENGTH_LONG).show();
		if (contentLength < 0) {
		} else {
			byte[] data = new byte[512];
			int len = 0;
			try {
				while (-1 != (len = inputStream.read(data))) {
					buffer.append(new String(data, 0, len));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			res = buffer.toString();

		}
		return res;
	}

	public void takePhoto() {

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStorageDirectory(),
				"temp_place_photo_upload.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		startActivityForResult(intent, IMAGE_CAPTURE);

	}

	public void showDialog(String title, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("ตกลง",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								finish();
							}
						});		

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}

	public void shareDialog(String title, String message, final String photo_id) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_menu_share);
		dialog.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("แชร์",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(
										ShowSelectedPlace.this,
										ShareToFacebook.class);
								intent.putExtra("TAG", "AddPhoto");
								intent.putExtra("name", name);
								intent.putExtra("description", description);
								intent.putExtra("url", KEY_SERVER
										+ "select_photo.php?photo_id="
										+ photo_id);
								startActivityForResult(intent, 0);
							}
						})

				.setNegativeButton("ไม่ ขอบคุณ",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								finish();
							}
						});

		AlertDialog alert = dialog.create();
		alert.setTitle(title);
		alert.show();
	}

	public void showConfirmDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(android.R.drawable.ic_dialog_info);
		dialog.setMessage("Dialog")
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								bitmap = BitmapFactory
										.decodeFile("/sdcard/temp_place_photo_upload.jpg");
								add_photo.setEnabled(true);
								filePath = "/sdcard/temp_place_photo_upload.jpg";
								file = new File(filePath);
							}
						});

		AlertDialog alert = dialog.create();
		alert.setTitle("select photo");
		alert.show();
	}

	private Drawable get_photo(String url) throws Exception {
		return Drawable.createFromStream(
				(InputStream) new URL(url).getContent(), "src");
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		slat = (float) location.getLatitude();
		slng = (float) location.getLongitude();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.share, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.share:
			Intent intent = new Intent(ShowSelectedPlace.this,
					ShareToFacebook.class);
			intent.putExtra("TAG", "ShowSelectedPlace");
			intent.putExtra("name", name);
			intent.putExtra("description", description);
			intent.putExtra("url", KEY_SERVER
					+ "preview_location.php?location=" + id);
			/*
			 * intent.putExtra("image_url", photo_name[(int) (Math.random() *
			 * photo_name.length)]);
			 */
			startActivityForResult(intent, 0);
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultcode,
			Intent intent) {
		super.onActivityResult(requestCode, resultcode, intent);
		switch (requestCode) {
		case IMAGE_CAPTURE:
			if (resultcode == RESULT_OK) {
				showConfirmDialog();
			} else {
				Log.d("Status:", "Cancel capture");
			}
			break;
		case IMAGE_PICKER:
			if (intent != null && resultcode == RESULT_OK) {

				Uri selectedImage = intent.getData();

				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);				
				cursor.close();

				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap = null;
				}

				bitmap = BitmapFactory.decodeFile(filePath);
				add_photo.setEnabled(true);
				file = new File(filePath);

			} else {
				Log.d("Status:", "Photopicker canceled");
			}
			break;
		default:
			break;
		}
	}

	private class PhotoUpload extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			// scale image to width or height is 800px
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			float scale = (float) (w * 1.0 / h * 1.0);

			if (w > h) {
				bitmap = Bitmap.createScaledBitmap(bitmap, 800,
						(int) Math.ceil((800.0 / scale)), true);
			} else {
				bitmap = Bitmap.createScaledBitmap(bitmap,
						(int) Math.ceil((800.0 * scale)), 800, true);
			}

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] byte_arr = stream.toByteArray();
			image_str = Base64.encodeBytes(byte_arr);

			pg_dialog = new ProgressDialog(ShowSelectedPlace.this);
			//pg_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pg_dialog.setTitle("Dialog title");
			pg_dialog.setMessage("Dialog message");
			pg_dialog.setCancelable(false);
			//pg_dialog.setMax(100); // (int) file.length()
			pg_dialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			String the_string_response = "";

			/*
			 * - category id - photo_upload - name - latitude - longitude -
			 * description - owner -> facebook name or facebook id
			 */

			String _user_name = "unknown", _user_username = "unknown", _user_id="unknown";
			sharedPreferences = getSharedPreferences("facebook_login",
					MODE_PRIVATE);
			_user_name = sharedPreferences.getString("name", "");
			_user_id = sharedPreferences.getString("id", "");
			_user_username = sharedPreferences.getString("user_name", "");

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("location_cate_id",
					category));
			nameValuePairs
					.add(new BasicNameValuePair("photo_upload", image_str));
			nameValuePairs.add(new BasicNameValuePair("location_id", id));
			nameValuePairs.add(new BasicNameValuePair("user_id", _user_id));
			nameValuePairs.add(new BasicNameValuePair("user_name", _user_name));			
			nameValuePairs.add(new BasicNameValuePair("user_username", _user_username));

			try {

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(urls[0]);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"utf-8"));
				HttpResponse response = httpclient.execute(httppost);
				the_string_response = convertResponseToString(response);
			

			} catch (Exception e) {
				Log.e("ShowSelectedPlace",
						"Error in http connection " + e.toString());
				return e.toString();
			}
			
			return the_string_response;
		}		

		@Override
		protected void onPostExecute(String result) {
			pg_dialog.dismiss();
			result = result.replace("\n", "");
			String id_str = result.substring(result.indexOf("#") + 1,
					result.indexOf("E"));

			if (!isNumber(id_str)) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						ShowSelectedPlace.this);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setMessage(result)
						.setCancelable(false)
						.setPositiveButton("Finish",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										finish();
									}
								});

				AlertDialog alert = dialog.create();
				alert.setTitle("FINISH");
				alert.show();
				add_photo.setEnabled(false);
			} else {
				add_photo.setEnabled(false);
				shareDialog("Add PHOTO",
						"ADD PHOTO facebook",
						id_str);
				// showDialog("TEST","photo_id" + result);
			}
			// showDialog("TEST", id_str + ", " + id_str.length());
		}
	}

	private boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}