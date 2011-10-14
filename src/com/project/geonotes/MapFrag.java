package com.project.geonotes;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.project.geonotes.Note.OnDeleteNoteListener;
/**
 * Creates a fragment which contains a map.
 * 
 * @author Orange Humanoids
 * 
 */
public class MapFrag extends Fragment implements geoNotes.OnSearchSetListener, OnNoteAddListener, Note.OnDeleteNoteListener {
	public MapView mapView = null;
	private AnItemizedOverlay mItemizedOverlay = null;
	public View superView = null;
	private DBAdapter mDb;
	private CursorSearch mSearch;
	private ArrayList<Note> noteList;
	private String category = null;
	private String searchStr = null;
	private String place = null;
	private Overlay touchOverlay = null;
	protected Location localLocation;
	private Drawable drawable;
	private int numCategories = 0;
	private OnNoteAddListener mOnAddListen;
	private OnDeleteNoteListener mOnDeleteListen;
	private int amountOfPins = 10;
	private int[] PinIDs = {R.drawable.pin1,R.drawable.pin2,
			R.drawable.pin3,R.drawable.pin4,R.drawable.pin5,
			R.drawable.pin6,R.drawable.pin7,R.drawable.pin8,
			R.drawable.pin9,R.drawable.pin10};
	private ArrayList<Drawable> thePins= new ArrayList<Drawable>();
	private ArrayList<AnItemizedOverlay> theOverlays= new ArrayList<AnItemizedOverlay>();
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		mOnAddListen = (OnNoteAddListener) getActivity().getFragmentManager().findFragmentById(R.id.list);
		mOnDeleteListen = (OnDeleteNoteListener) getActivity().getFragmentManager().findFragmentById(R.id.list);
		
		//super.onActivityCreated(savedInstanceState);
		superView = inflater.inflate(R.layout.maplayout, container, false);
		
		mapView = (MapView) superView.findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mDb = new DBAdapter(getActivity());
		mDb.open();
		
		noteList = new ArrayList<Note>();
		String[] searchStrings;// = new String[3];
			
		searchStrings = CursorSearch.parseBundle(savedInstanceState);
		mSearch = new CursorSearch(searchStrings[0], searchStrings[1], searchStrings[2], mDb, this);
		noteList = mSearch.getListOfNotes();
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		
		touchOverlay = new TouchOverlay(this, mDb);
        mapOverlays.add(touchOverlay);
        
        for(int i=0;i<amountOfPins;i++){
        	thePins.add(superView.getResources().getDrawable(PinIDs[i]));
        	theOverlays.add(new AnItemizedOverlay(thePins.get(i), superView.getContext()));
        }
        
        Drawable drawable = thePins.get(2);
        mItemizedOverlay = new AnItemizedOverlay(drawable, superView.getContext());

        addToMap(noteList);
        findLocation();        
		return superView;
	}
	/**
	 * Adds a point to the map
	 * @param addNote: The note to add
	 * @param callback: whether or not to dispatch at Note Added event -- i.e. if we are reading old points to the map (say on startup), this arg is false
	 * @return
	 */
	public boolean addPoint(Note addNote, boolean callback){
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		Log.d("ADDING POINT", "" + addNote);
		NoteOverlayItem overlayItem = new NoteOverlayItem(addNote);
		int index = getOverlay(addNote);
		theOverlays.get(index).addOverlay(overlayItem);//, thePins.get(2));//specificDraw);
		mapOverlays.clear();
		mapOverlays.add(touchOverlay);
		for(int i=0;i<theOverlays.size();i++){
			if(theOverlays.get(i).size() != 0)
				mapOverlays.add(theOverlays.get(i));
		}
		
		if (callback) {
			Log.d("CALLBACK", "Getting ListFragment");
			this.noteList.add(addNote);
			mOnAddListen.onNoteAdd(addNote);
		}
		return true;
	}
	/**
	 * Given a note, getOverlay returns the index in theOverlays that corresponds to the orrect AnItemizedOverlay
	 * to add the given note to. This ensures each category has a distinct color pin
	 * @param addNote: The note to add
	 * @return: The correct index in theOverlays to add addNote to
	 */
	private int getOverlay(Note addNote) {
		ArrayList<String> allCategories = new ArrayList<String>();
		Cursor c = mDb.getCategories();
		c.moveToFirst();
		int index = c.getColumnIndex(DBAdapter.KEY_CATEGORY);
		while(!c.isAfterLast()){
			allCategories.add(c.getString(index));
			c.moveToNext();
		}
		int categorySpot = -1;
		for(int i=0; i < allCategories.size(); i++){
			if (allCategories.get(i).equals(addNote.getCategory())){
				categorySpot = i;
			}
		}
		if (categorySpot == -1)
			Log.e("WTF", "FOR SERIAL");
		
		AnItemizedOverlay overlay = theOverlays.get(categorySpot%amountOfPins);
		return categorySpot%amountOfPins;
	}
	
	/**
	 * Handler for when a user searches for a specific category
	 */
	@Override
	public void onCategorySet(int categoryID) {
		Log.d("CATID", categoryID + "");
		DBAdapter db = new DBAdapter(getActivity());
		if (!db.isOpen()) {
			db.open();
		}
		
		Cursor value = db.getLocation(null, -1, -1, categoryID, null);		//Contains the specific category value
		if (value != null) {
			value.moveToFirst();
		} else {
			Log.d("DAMN", "FUCK THIS");
		}
		String category = value.getString(value.getColumnIndex(DBAdapter.KEY_CATEGORY));

		mSearch.setCategory(category);
		noteList = mSearch.getListOfNotes();
		addToMap(noteList);
	}
	/**
	 * Helper function to update the mapView. It removes all items and then adds lst
	 * @param lst: list of Notes to add
	 */
	private void addToMap(ArrayList<Note> lst) {
		removeAll();
		for (int i = 0; i < lst.size(); i++) {
			addPoint(lst.get(i), false);
		}
	}
	/**
	 * Removes all notes from map
	 */
	public void removeAll(){
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		mapOverlays.add(touchOverlay);
        mapOverlays.clear();
		mapOverlays.add(touchOverlay);
		
		theOverlays.clear();
		
        for(int i=0;i<amountOfPins;i++){
        	thePins.add(superView.getResources().getDrawable(PinIDs[i]));
        	theOverlays.add(new AnItemizedOverlay(thePins.get(i), superView.getContext()));
        }
        
		Log.e("RemoveAll","actually calling remove All");
	}
	/**
	 * Note added handler
	 */
	@Override
	public void onNoteAdd(Note added) {
		//showAll();
		addPoint(added, true);
		
	}
	/**
	 * Finds the current location of the user
	 */
	private void findLocation() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      Log.e("Location", "is updated!!!");
		      localLocation = location;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {
		    }

		    public void onProviderDisabled(String provider) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		
	}
	/**
	 * Moves the map view to the specified location
	 * @param loc: location to move to
	 * @param zoomLevel: Zoom level to use
	 */
	public void moveToNewLocation(Location loc, int zoomLevel) {
		if (loc != null) {
			MapController mc = mapView.getController();
			GeoPoint point = new GeoPoint((int) (loc.getLatitude()*1000000),(int) (loc.getLongitude()*1000000));
			mc.animateTo(point);
			mc.setZoom(zoomLevel);
		}
		else{
			Toast.makeText(this.getActivity().getApplicationContext(), "No GPS data available", 1000).show(); 
		}
	}
	/**
	 * Deprecated method
	 * @param zoomLevel
	 */
	public void makeUseOfNewLocation(int zoomLevel) {
		if(localLocation != null){
			MapController mc = mapView.getController();
			GeoPoint point = new GeoPoint((int) (localLocation.getLatitude()*1000000),(int) (localLocation.getLongitude()*1000000));
			mc.animateTo(point);
			mc.setZoom(zoomLevel);
		}
		else{
			Toast.makeText(this.getActivity().getApplicationContext(), "No GPS data available", 1000).show();
		}
	}
	/**
	 * 
	 * @return the current location of the user
	 */
	public Location getCurrentLocation() {
		return localLocation;
	}
	/**
	 * Removes all filters from cursorsearch and adds all place to the map
	 */
	public void showAll(){
		mSearch.setCategory("");
		mSearch.setPlace("");
		mSearch.setSearchStr("");
		noteList = mSearch.getListOfNotes();

		addToMap(noteList);
		mapView.removeAllViews();
		mOnDeleteListen.onDeleteNote();
		Log.e("asdf", "asdfasdfasdfasdf");
	}
	/**
	 * Delete note handler
	 */
	@Override
	public void onDeleteNote() {
		mSearch.setCategory("");
		mSearch.setPlace("");
		mSearch.setSearchStr("");
		
		noteList = mSearch.getListOfNotes();
		addToMap(noteList);
		mOnDeleteListen.onDeleteNote();
	}
	/**
	 * When the user searches and the main activity realizes, this function handles that
	 */
	@Override
	public void onSearchSet(String search) {
		//search
		mSearch.setSearchStr(search);
		noteList = mSearch.getListOfNotes();
		this.addToMap(noteList);
		// TODO Auto-generated method stub
		
	}
}
