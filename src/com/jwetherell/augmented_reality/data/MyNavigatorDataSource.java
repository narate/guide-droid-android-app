package com.jwetherell.augmented_reality.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;
import com.narate.ar.fork_framework.R;

public class MyNavigatorDataSource extends NetworkDataSource {
	private static final String URL = "http://cpre-pjb.kmutnb.ac.th/project/navigator/mobile/request.php";

	private static Bitmap icon = null;

	public MyNavigatorDataSource(Resources res) {
		if (res == null)
			throw new NullPointerException();

		createIcon(res);
	}

	protected void createIcon(Resources res) {
		if (res == null)
			throw new NullPointerException();

		icon = BitmapFactory.decodeResource(res, R.drawable.androidmarker);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createRequestURL(double lat, double lon, double alt,
			float radius, String locale) {
		return URL + "?lat=" + lat + "&lng=" + lon + "&radius=" + radius;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Marker> parse(JSONObject root) {
		if (root == null)
			return null;

		JSONObject jo = null;
		JSONArray dataArray = null;
		List<Marker> markers = new ArrayList<Marker>();

		try {
			if (root.has("request"))
				dataArray = root.getJSONArray("request");
			if (dataArray == null)
				return markers;
			int top = Math.min(MAX, dataArray.length());
			for (int i = 0; i < top; i++) {
				jo = dataArray.getJSONObject(i);
				Marker ma = processJSONObject(jo);
				if (ma != null)
					markers.add(ma);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return markers;
	}

	private Marker processJSONObject(JSONObject jo) {
		if (jo == null)
			return null;

		Marker ma = null;
		if (jo.has("title") && jo.has("lat") && jo.has("lng")) {
			try {
				ma = new IconMarker("# " + jo.getString("title") + " #\n"
						+ jo.getString("description"), jo.getDouble("lat"),
						jo.getDouble("lng"), 0.0, Color.RED, icon);
				/*ma.setId(jo.getString("id"));
				ma.setCategory(jo.getString("category"));
				ma.setTitle(jo.getString("title"));
				ma.setLatitude(jo.getString("lat"));
				ma.setLongitude(jo.getString("lng"));
				ma.setDescription(jo.getString("description"));
				ma.setOwner(jo.getString("owner"));
				ma.setDescription(jo.getString("description"));*/
				ma.setPlace(
						jo.getString("id"), 
						jo.getString("category"),
						jo.getString("title"), 
						jo.getString("lat"),
						jo.getString("lng"),
						Float.parseFloat(jo.getString("distance")),
						jo.getString("description"), 
						jo.getString("owner"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ma;
	}
}