package com.project.geonotes;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;

/**
 * An itemised overlay which shows a pin on the map when user taps on it.
 * @author 
 */
public class AnItemizedOverlay extends ItemizedOverlay{
	private ArrayList<NoteOverlayItem> mOverlays = new ArrayList<NoteOverlayItem>();
	private Context mContext;
	/**
	 * Constructor with only a default marker
	 * @param defaultMarker: the type of marker
	 */
	public AnItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	/**
	 *  Constructor with a default marker and a Context
	 */
	public AnItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		}
	public ArrayList<NoteOverlayItem> getAllItems() {
		return mOverlays;
	}

/**
 * returns a NoteOverlayItem ate index i
 * @param i: overlay at integer i
 * @return a NoteOverlayItem
 */
	@Override
	protected NoteOverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	/**
	 * Return the size of the overlay
	 */
	public int size() {
		return mOverlays.size();
	}
	/**
	 * Add an overlay
	 * @param overlay: An overlay to add
	 */
	public void addOverlay(NoteOverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	/**
	 * On a tap, get the note represented by the marker
	 * @param index: overlay at a particular index
	 * @return
	 */
	@Override
	protected boolean onTap(int index) {
	  NoteOverlayItem item = mOverlays.get(index);
	  item.getNote().createDialog(mContext, true);
	  //item.setMarker();
	  return true;
	}

}
