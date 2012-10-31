package com.narate.ar.facebook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;
import com.narate.ar.fork_framework.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginWithFacebook extends Activity {
	

	private Facebook facebook;
	// private CheckBox mFacebookBtn;
	private ProgressDialog mProgress;
	private EditText status;
	private Button login_button;
	private TextView login_status;
	private Handler mRunOnUi = new Handler();
	private static final String[] PERMISSIONS = new String[] {
			"publish_stream", "read_stream", "offline_access", "user_photos",
			"publish_actions" };
	private static final String APP_ID = "396205990411489";	
	private String status_message = "";
	private boolean isLogin = false;
	private String isLogout = "false";
	private Bitmap bi;
	public String facebook_name = "", facebook_id = "", facebook_username = "";

	public String getFacebook_name() {
		return facebook_name;
	}

	public void setFacebook_name(String facebook_name) {
		this.facebook_name = facebook_name;
	}

	public String getFacebook_id() {
		return facebook_id;
	}

	public void setFacebook_id(String facebook_id) {
		this.facebook_id = facebook_id;
	}

	public String getFacebook_username() {
		return facebook_username;
	}

	public void setFacebook_username(String facebook_username) {
		this.facebook_username = facebook_username;
	}
	
	public LoginWithFacebook() {
		// TODO Auto-generated constructor stub		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.facebook_login);

		mProgress = new ProgressDialog(this);
		mProgress.setTitle("Finishing");
		
		login_button = (Button)findViewById(R.id.login);
		login_status = (TextView) findViewById(R.id.login_status);
		facebook = new Facebook(APP_ID);

		SessionStore.restore(facebook, this);

		if (facebook.isSessionValid()) {
			isLogin = true;
			String name = SessionStore.getName(this);
			name = (name.equals("")) ? "Unknown" : name;
			login_status.setText("[" + name + "]");
		}

		login_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						onFacebookClick();
					}
				});		
	}

	public void onFacebookClick() {
		if (facebook.isSessionValid()) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle("Logout");
			builder.setMessage("Logout from facebook ?")
					.setCancelable(false)
					.setPositiveButton("Log me out",
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
			SessionStore.save(facebook, LoginWithFacebook.this);
			login_status.setText("Unknown");
			isLogin = true;
			getFacebookName();
		}

		public void onFacebookError(FacebookError error) {
			Toast.makeText(LoginWithFacebook.this, "Sorry, we have a problem try again later.",
					Toast.LENGTH_SHORT).show();
			isLogin = false;
		}

		public void onError(DialogError error) {
			Toast.makeText(LoginWithFacebook.this, "Sorry, have a problem try again later",
					Toast.LENGTH_SHORT).show();
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
		mProgress.setMessage("Getting your facebook name");
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
					setFacebook_name(name);
					setFacebook_id(jsonObj.getString("id"));
					setFacebook_username(jsonObj.getString("username"));
					editor.putString("id", getFacebook_id());
					editor.putString("name", getFacebook_name());
					editor.putString("user_name", getFacebook_username());
					editor.commit();
					what = 0;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
			}
		}.start();
	}
	private void login(){
		isLogin = false;
		facebook.authorize(this, PERMISSIONS, -1,
				new FacebookLoginDialogListener());
	}

	public void logout() {
		mProgress.setMessage("Logout from facebook.");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				SessionStore.clear(LoginWithFacebook.this);
				int what = 1;
				try {
					facebook.logout(LoginWithFacebook.this);
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
				SessionStore.saveName(username, LoginWithFacebook.this);
				login_status.setText("[" + username + "]");
				Toast.makeText(LoginWithFacebook.this, "Your name's " + username,
						Toast.LENGTH_SHORT).show();
				login_button.setText("Logout from facebook");
			} else {
				Toast.makeText(LoginWithFacebook.this, "Login failed",
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
				Toast.makeText(LoginWithFacebook.this, "What is this message ?",
						Toast.LENGTH_SHORT).show();
			} else {
				// mFacebookBtn.setChecked(false);
				isLogin = false;
				login_status.setText("No user login now!");
				login_button.setText("Login to facebook");
				Toast.makeText(LoginWithFacebook.this, "Loged out",
						Toast.LENGTH_SHORT).show();
				
				login_button.setText("Login to facebook");
				editor.putString("name", "");
				editor.putString("user_name", "");
				editor.commit();
			}
		}
	};

	public void postToFacebook(String message) throws URISyntaxException {
		mProgress.setMessage("Post message to facebook wall");
		mProgress.show();

		AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(facebook);

		Bundle params = new Bundle();
		params.putString("message", message);
		// params.putString("href", URL);
		mAsyncFbRunner.request("me/feed", params, "POST",
				new WallPostListener());
	}

	public void loginAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Login");
		builder.setMessage("Login to facebook")
				.setCancelable(false)
				.setPositiveButton("Log me in",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								onFacebookClick();
							}
						});
		builder.setNegativeButton("Cancel",
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

		status_message = status.getText().toString();

		byte[] data = null;

		bi = BitmapFactory.decodeFile("/sdcard/oh_wow.jpg");

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

					Toast.makeText(LoginWithFacebook.this, "wall post listener",
							Toast.LENGTH_SHORT).show();					
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

						Toast.makeText(LoginWithFacebook.this,
								"Upload listener", Toast.LENGTH_SHORT)
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

}
