package com.narate.ar.fork_framework;

import com.google.android.maps.MapActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class SlidingDrawer extends MapActivity {

	Button slideHandleButton;
	SlidingDrawer slidingDrawer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		//setContentView(R.layout.test);
		View inflatedSlidingDrawerLayout = getLayoutInflater().inflate(R.layout.sliding_drawer, null);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		getWindow().addContentView(inflatedSlidingDrawerLayout, params); 

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}