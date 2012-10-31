package com.narate.ar.fork_framework.place;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.narate.ar.fork_framework.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaceAdapter extends ArrayAdapter<Place>{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");
	private ArrayList<Place> objects;
	public PlaceAdapter(Context context, int textViewResourceId,
			ArrayList<Place> listItems) {
		super(context, textViewResourceId, listItems);
		this.objects = listItems;
		// TODO Auto-generated constructor stub
	}

	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.place_list, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Place i = objects.get(position);

		if (i != null) {

			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.

			TextView title = (TextView) v.findViewById(R.id.item_title);
			TextView detail = (TextView) v.findViewById(R.id.item_details);
			

			// check to see if each individual textview is null.
			// if not, assign some text!
			float distance = i.getDistance() * 1000;
			String description = i.getDescription();
			if (distance < 1000.0) {			
				description += " (~" + DECIMAL_FORMAT.format(distance) + " m.)";
			} else {
				double d = distance / 1000.0;			
				description += " (~" + DECIMAL_FORMAT.format(d) + " km.)";
			}
			
				title.setText(i.getTitle());			
		
				detail.setText( description);
			
			
		}

		// the view must be returned to our activity
		return v;

	}
}
