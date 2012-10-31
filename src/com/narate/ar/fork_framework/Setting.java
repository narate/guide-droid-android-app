package com.narate.ar.fork_framework;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.narate.ar.facebook.LoginWithFacebook;
import com.narate.ar.fork_framework.R;

public class Setting extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		final Preference customPref = (Preference) findPreference("facebook_login");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Setting.this,
								LoginWithFacebook.class));						
						/*
						SharedPreferences customSharedPreference = getSharedPreferences(
								"facebook_login", Activity.MODE_PRIVATE);
						SharedPreferences.Editor editor = customSharedPreference
								.edit();
						editor.putString("user",
								"USER");
						editor.putString("user_name",
								"USER NAME");
						editor.commit();*/			
						return true;
					}

				});
	}
}