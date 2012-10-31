package com.narate.ar.fork_framework;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.sax.StartElementListener;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MyMapOverlay extends ItemizedOverlay<OverlayItem> {
	//private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private List<OverlayItem> mOverlays;
	Context mContext;

	public MyMapOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public MyMapOverlay(Drawable defaultMarker, Context context, List<OverlayItem> items) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		this.mOverlays = items;
		boundCenterBottom(defaultMarker);
		populate();
	}	

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(final int index) {
		final OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setIcon(R.drawable.android_my_location);
		dialog.setTitle(item.getTitle());
		final String text = item.getSnippet().length() == 0 ? "Unknown"
				: item.getSnippet();
		dialog.setMessage(text);
		dialog.setNegativeButton("Close", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				//Toast.makeText(mContext, index + "", Toast.LENGTH_LONG).show();
			}

		});
		
		if (!item.getTitle().equalsIgnoreCase("Current Location")) {
			
			dialog.setIcon(R.drawable.marker);
			/*dialog.setPositiveButton("Open", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mContext.startActivity(new Intent(mContext,
							ShowListView.class));
				}

			});*/
		}
		dialog.show();
		return true;
	}

}
