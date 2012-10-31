package com.narate.ar.fork_framework;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;
import android.os.Handler;

public class SplashScreen extends Activity {
	/** Called when the activity is first created. */	
	private Handler mHandler;
	public static int width;
	static Display display;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);		
		display = getWindowManager().getDefaultDisplay();
		mHandler = new Handler();

		// Call postDelayed Method for running process after delay time
		mHandler.postDelayed(new Runnable() {

			public void run() {
				finish();
				startActivity(new Intent(SplashScreen.this,
						MainActivity.class));
			}

		}, 500);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			finish();
			startActivity(new Intent(SplashScreen.this,
					MainActivity.class));
		}
		return true;
	}
	
	public static int getWidth(){				
		width = display.getWidth();
		return width;
	}
}
