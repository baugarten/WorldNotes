package com.project.geonotes;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class TouchOverlay extends Overlay {

	int lastX = 0;
	int lastY = 0;
	double percentage = .05;
	int [] lastPoint = new int[]{0,0};
	MapFrag superMapFrag = null;
	private DBAdapter db;
	
	public TouchOverlay(MapFrag theFrag, DBAdapter db) {
		super();
		this.db = db;
		superMapFrag = theFrag;
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean onTap(GeoPoint point, MapView mapView){
		
		
		int [] newPoint = new int[] {point.getLatitudeE6(),point.getLongitudeE6()};
		
		//mapView.getLocationOnScreen(newPoint);
		
		Log.e("newPointx", String.valueOf(newPoint[0]));
		Log.e("newPointy", String.valueOf(newPoint[1]));
		Log.e("lastPointx", String.valueOf(lastPoint[0]));
		Log.e("lastPointy", String.valueOf(lastPoint[1]));
		
		Double radiusLat = percentage*mapView.getLatitudeSpan();
		Double radiusLong = percentage*mapView.getLongitudeSpan();
		
		if (lastPoint[0]-radiusLat < newPoint[0] && lastPoint[0]+radiusLat>newPoint[0] &&
				lastPoint[1]-radiusLong < newPoint[1] && lastPoint[1]+radiusLong>newPoint[1]){
			lastPoint = new int[] {0,0};
			Note test = new Note(-1, newPoint[0],newPoint[1],"","","", db, superMapFrag);
			test.createDialog(superMapFrag.getActivity(), false);

			return true;
		}
		
		else{
			lastPoint = newPoint;
			return false;
		}
	}
}
