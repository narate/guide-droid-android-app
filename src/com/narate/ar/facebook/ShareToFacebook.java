package com.narate.ar.facebook;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.SessionStore;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.narate.ar.facebook.LoginWithFacebook.FacebookLoginDialogListener;
import com.narate.ar.facebook.LoginWithFacebook.SampleUploadListener;
import com.narate.ar.facebook.LoginWithFacebook.WallPostListener;
import com.narate.ar.fork_framework.AddForm;
import com.narate.ar.fork_framework.MainActivity;
import com.narate.ar.fork_framework.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ShareToFacebook extends Activity {
	private EditText text_post;
	private Button share;
	private TextView description;
	String name = "", description_text = "", url = "", image_url = "",
			TAG = "";

	/*
	 * For facebook check is loged in if loged in ready to share else login and
	 * prepare to share
	 */
	private Facebook facebook;
	private static final String APP_ID = "396205990411489";
	private static final String[] PERMISSIONS = new String[] {
			"publish_stream", "read_stream", "offline_access", "user_photos",
			"publish_actions" };
	private boolean isLogin = false;
	private ProgressDialog mProgress;
	private Handler mRunOnUi = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_to_facebook);

		mProgress = new ProgressDialog(this);
		mProgress.setTitle("Share to facebook");

		facebook = new Facebook(APP_ID);
		SessionStore.restore(facebook, this);
		if (facebook.isSessionValid()) {
			isLogin = true;
			Toast.makeText(ShareToFacebook.this, "Loged in", Toast.LENGTH_LONG)
					.show();
		} else {
			isLogin = false;
			Toast.makeText(ShareToFacebook.this, "Not Loged in",
					Toast.LENGTH_LONG).show();
		}

		text_post = (EditText) findViewById(R.id.text_post);
		share = (Button) findViewById(R.id.share);
		description = (TextView) findViewById(R.id.description);

		TAG = getIntent().getExtras().getString("TAG");
		if (TAG.equalsIgnoreCase("AddForm")) {

			name = " new place name : "
					+ getIntent().getExtras().getString("name");
			description_text = getIntent().getExtras().getString("description");
			url = getIntent().getExtras().getString("url");
			description.setText(name + ", " + description_text);

		} else if (TAG.equalsIgnoreCase("ShowSelectedPlace")) {

			name = "name : " + getIntent().getExtras().getString("name");
			description_text = getIntent().getExtras().getString("description");
			url = getIntent().getExtras().getString("url");
			// image_url = getIntent().getExtras().getString("image_url");
			description.setText(name + ", " + description_text);
			/*
			 * new DownloadImageTask((ImageView) findViewById(R.id.image))
			 * .execute(image_url);
			 */

		} else if (TAG.equalsIgnoreCase("AddPhoto")) {

			name = "name  : " + getIntent().getExtras().getString("name");
			description_text = getIntent().getExtras().getString("description");
			url = getIntent().getExtras().getString("url");
			description.setText(name + ", " + description_text);

		} else {

		}

		share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isLogin) {
					login();
				} else {
					try {
						postToFacebook(text_post.getText().toString());
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
	}

	public void onFacebookClick() {
		if (facebook.isSessionValid()) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle("onFacebook click");
			builder.setMessage("on facebook click message")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									logout();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									isLogin = true;
								}
							});

			final AlertDialog alert = builder.create();
			alert.show();
		} else {
			isLogin = false;
			facebook.authorize(this, PERMISSIONS, -1,
					new FacebookLoginDialogListener());
		}
	}

	public final class FacebookLoginDialogListener implements DialogListener {

		public void onComplete(Bundle values) {
			SessionStore.save(facebook, ShareToFacebook.this);
			isLogin = true;
			getFacebookName();
		}

		public void onFacebookError(FacebookError error) {
			Toast.makeText(ShareToFacebook.this, "Facebook error",
					Toast.LENGTH_SHORT).show();
			isLogin = false;
		}

		public void onError(DialogError error) {
			Toast.makeText(ShareToFacebook.this, "On error", Toast.LENGTH_SHORT)
					.show();
			isLogin = false;
		}

		public void onCancel() {
			isLogin = false;
		}
	}

	public void getFacebookName() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"facebook_login", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		mProgress.setMessage("getFacebookName");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				String name = "";
				String id = "";
				int what = 1;
				try {
					String me = facebook.request("me");
					JSONObject jsonObj = (JSONObject) new JSONTokener(me)
							.nextValue();
					name = jsonObj.getString("name");
					editor.putString("id", jsonObj.getString("id"));
					editor.putString("name", jsonObj.getString("name"));
					editor.putString("user_name", jsonObj.getString("username"));
					editor.commit();
					what = 0;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
			}
		}.start();
	}

	private void login() {
		isLogin = false;
		facebook.authorize(this, PERMISSIONS, -1,
				new FacebookLoginDialogListener());
	}

	public void logout() {
		mProgress.setMessage("Logout");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				SessionStore.clear(ShareToFacebook.this);
				int what = 1;
				try {
					facebook.logout(ShareToFacebook.this);
					what = 0;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}

	public Handler mFbHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgress.dismiss();

			if (msg.what == 0) {
				String username = (String) msg.obj;
				username = (username.equals("")) ? "Unknown" : username;
				SessionStore.saveName(username, ShareToFacebook.this);
				Toast.makeText(ShareToFacebook.this,
						"handler message  " + username, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(ShareToFacebook.this, "else handler message",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			SharedPreferences sharedPreferences = getSharedPreferences(
					"facebook_login", MODE_PRIVATE);
			final SharedPreferences.Editor editor = sharedPreferences.edit();
			mProgress.dismiss();

			if (msg.what == 1) {
				Toast.makeText(ShareToFacebook.this, "wgat == 1",
						Toast.LENGTH_SHORT).show();
			} else {
				// mFacebookBtn.setChecked(false);
				isLogin = false;

				Toast.makeText(ShareToFacebook.this, "else of what == 1",
						Toast.LENGTH_SHORT).show();

				editor.putString("name", "");
				editor.putString("user_name", "");
				editor.commit();
			}
		}
	};

	public void postToFacebook(String message) throws URISyntaxException {
		mProgress.setMessage("Posting to facebook");
		mProgress.show();

		AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(facebook);

		Bundle params = new Bundle();
		params.putString("message", message);
		// params.putString("source",
		// "https://fbcdn-sphotos-a.akamaihd.net/hphotos-ak-snc7/318285_2980677483425_1456569621_32006335_424866767_n.jpg");
		params.putString("link", url);
		params.putString("name", name);
		params.putString("caption", description_text);
		/*
		 * params.putString( "description",
		 * "à¸—à¸”à¸ªà¸­à¸šà¹‚à¸žà¸ªà¸¡à¸²à¸¢à¸±à¸‡ facebook à¹‚à¸”à¸¢à¸�à¸²à¸£à¹€à¸‚à¸µà¸¢à¸™à¹�à¸­à¸žà¸šà¸™à¹�à¸­à¸™à¸”à¸£à¸­à¸¢à¸™à¹Œ à¸£à¹ˆà¸§à¸¡à¸�à¸±à¸šà¸�à¸²à¸£à¹ƒà¸Šà¹‰ facebook Adnroid SDK"
		 * );
		 */
		mAsyncFbRunner.request("me/feed", params, "POST",
				new WallPostListener());
	}

	public void loginAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Login alert");
		builder.setMessage("Login to facebook")
				.setCancelable(false)
				.setPositiveButton("Log me in",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								onFacebookClick();
							}
						});
		builder.setNegativeButton("No, Thanks",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void enableGPS() {
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(gpsOptionsIntent);
	}

	public void takePhoto() {

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStorageDirectory(),
				"oh_wow.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		startActivityForResult(intent, 0);
	}

	public void postPhoto() {
		mProgress.setMessage("Post photo to facebook");
		mProgress.show();

		byte[] data = null;

		Bitmap bi = BitmapFactory.decodeFile("/sdcard/oh_wow.jpg");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		Bundle params = new Bundle();
		params.putString(Facebook.TOKEN, facebook.getAccessToken());
		params.putString("method", "photos.upload");
		params.putString("caption", "PHOTO_CAPTION");
		params.putByteArray("picture", data);

		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
		mAsyncRunner.request(null, params, "POST", new SampleUploadListener());
	}

	public void postPhoto(Bitmap bitmap, String message) {
		mProgress.setMessage("Post photo to facebook");
		mProgress.show();

		byte[] data = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		Bundle params = new Bundle();
		params.putString(Facebook.TOKEN, facebook.getAccessToken());
		params.putString("method", "photos.upload");
		params.putString("caption", message);
		params.putByteArray("picture", data);

		AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
		mAsyncRunner.request(null, params, "POST", new SampleUploadListener());
	}

	void test() {
		Toast.makeText(this, "Test", Toast.LENGTH_LONG).show();
	}

	public final class WallPostListener extends BaseRequestListener {
		public void onComplete(final String response) {
			mRunOnUi.post(new Runnable() {
				@Override
				public void run() {
					mProgress.cancel();

					Toast.makeText(ShareToFacebook.this, "wall post listener",
							Toast.LENGTH_SHORT).show();
					if (TAG.equalsIgnoreCase("AddForm")) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ShareToFacebook.this);
						builder.setIcon(android.R.drawable.ic_menu_share);
						builder.setTitle("share menu");
						builder.setMessage("share message")
								.setCancelable(false)
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												Intent intent = new Intent(
														ShareToFacebook.this,
														MainActivity.class);
												startActivityForResult(intent,
														0);
											}
										});

						AlertDialog alert = builder.create();
						alert.show();

					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								ShareToFacebook.this);
						builder.setIcon(android.R.drawable.ic_menu_share);
						builder.setTitle("share title");
						builder.setMessage("share message")
								.setCancelable(false)
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												finish();
											}
										});

						AlertDialog alert = builder.create();
						alert.show();
					}
				}
			});
		}
	}

	public final class SampleUploadListener extends BaseRequestListener {

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			try {
				// process the response here: (executed in background thread)
				Log.d("Facebook-Example", "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String src = json.getString("src");

				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."

				mRunOnUi.post(new Runnable() {
					@Override
					public void run() {
						mProgress.cancel();

						Toast.makeText(ShareToFacebook.this,
								"sample upload listener", Toast.LENGTH_SHORT)
								.show();
					}
				});

			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
			}
		}

	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}
}