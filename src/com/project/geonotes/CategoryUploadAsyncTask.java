package com.project.geonotes;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class CategoryUploadAsyncTask extends AsyncTask<Intent, Void, Void> {
	private WebWrapper mWrapper;
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private Context mContext;
	private DBAdapter mDb;
	private String targetUsername;
	private String sourceUsername;
	private String categoryName;
	private Category category;
	
	
	public CategoryUploadAsyncTask(String categoryName, String source, String target, Notification not, NotificationManager notMan, DBAdapter db) {
		sourceUsername = source;
		targetUsername = target;
		mNotification = not;
		mNotificationManager = notMan;
		mDb = db;
		
		int index = Integer.parseInt(categoryName.substring(categoryName.lastIndexOf("@")+1));
		Cursor value = mDb.getLocation(null, -1, -1, index, null);		//Contains the specific category value
		if (value != null) {
			value.moveToFirst();
		} else {
			Log.d("DAMN", "FUCK THIS");
		}
		this.categoryName = value.getString(value.getColumnIndex(DBAdapter.KEY_CATEGORY));
		Log.d("UPLOAD", categoryName);

		
	}
	@Override
	protected Void doInBackground(Intent... arg0) {
		Log.d("Upload", "do ma thing");
		
		Cursor cur = mDb.getLocationsByCategory(categoryName);
		cur.moveToFirst();
		
		ArrayList<String> places = new ArrayList<String>();
		ArrayList<String> notes = new ArrayList<String>();
		ArrayList<String> lats = new ArrayList<String>();
		ArrayList<String> longs = new ArrayList<String>();

		int placeIndex = cur.getColumnIndex(DBAdapter.KEY_PLACE);
		int noteIndex = cur.getColumnIndex(DBAdapter.KEY_NOTE);
		int latIndex = cur.getColumnIndex(DBAdapter.KEY_X_COORD);
		int longIndex = cur.getColumnIndex(DBAdapter.KEY_Y_COORD);
		
		while (!cur.isAfterLast()) {
			places.add(cur.getString(placeIndex));
			notes.add(cur.getString(noteIndex));
			lats.add("" + cur.getFloat(latIndex));
			longs.add("" + cur.getFloat(longIndex));
			cur.moveToNext();
		}
		category = new Category(categoryName, (String[])places.toArray(new String[0]), (String[])notes.toArray(new String[0]), (String[])lats.toArray(new String[0]), (String[])longs.toArray(new String[0]));
		try {
			Log.d("Upload", "sending to webwrapper");
			mWrapper = new WebWrapper(sourceUsername, targetUsername, category); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
