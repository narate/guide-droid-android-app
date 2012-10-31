package com.narate.ar.fork_framework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.narate.ar.fork_framework.place.Place;

import android.os.AsyncTask;
import android.util.Log;

public class GetServerData extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... url) {
		// TODO Auto-generated method stub
		String json = "";
		
		InputStream is = null;
		String result = "";
		/*String request = KEY_SERVER + "?" + "lat=" + lat + "&" + "lng=" + lng
				+ "&radius=" + radius;*/
		JSONObject json_data, root;
		JSONArray jArray = null;
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		// nameValuePairs.add(new BasicNameValuePair("test", "test"));
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url[0]);

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
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
			// showDialog("TEST", "NULL request reponse : " + result);
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
		}

		try {
			root = new JSONObject(result);
			if (!root.getString("request").equalsIgnoreCase("null")) {
				jArray = root.getJSONArray("request");
			} else {
				return null;
			}

			String read_json = "";

			// id_table = new String[jArray.length()];
			//place = new ArrayList<Place>();

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
				// id_table[i] = json_data.getString("id");
				/*id = json_data.getString("id");
				category = json_data.getString("category");
				name = json_data.getString("title");
				latitude = json_data.getString("lat");
				longitude = json_data.getString("lng");
				description = json_data.getString("description");
				owner = json_data.getString("owner");
				distance = json_data.getString("distance");

				place.add(new Place(id, category, name, latitude, longitude,
						Float.parseFloat(distance), description, owner));

				read_json = name + "\n" + description + "\n";*/
				// returnString += read_json;
			}
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}

		/*Collections.sort(place);
		for (int i = 0; i < place.size(); i++) {			
			listItems.add(place.get(i));
		}*/
		return json;
	}
	@Override
	protected void onPostExecute(String result) {
		
	}
}
