package com.project.geonotes;

import java.util.ArrayList;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class CursorSearch {
	private String place;
	private String category;
	private String searchStr;
	private static DBAdapter db;
	private Fragment superMapFrag;
	public CursorSearch(String place, String category, String searchstr, DBAdapter db, Fragment mFragment) {
		this.place = place;
		this.category = category;
		this.searchStr = searchStr;
		this.db = db;
		this.superMapFrag = mFragment;
	}
	
	public Cursor getCursorToSearch() {
		if (!TextUtils.isEmpty(place)) {		//if they are only filtering by category, not place name
			Log.d("DB", "Get Specific Location");
			return null;
		} else if (!TextUtils.isEmpty(category) && TextUtils.isEmpty(place)) {		//Search for a specific place
			//Need some sort of searching algorithm
			Log.d("DB", "Get Locations By Category: " + category);
			return db.getLocationsByCategory(category);
			
		} else if (!TextUtils.isEmpty(searchStr)) {
			//Search...
			Log.d("DB", "Gotta do some type of search");
			return null;
		} else {										//Get all places
			Log.d("DB", "Get all locations");
			Cursor temp = db.getLocations();
			Log.d("DB", "Got ALL Locations");
			return temp;
		}
	}
	public ArrayList<Note> getListOfNotes() {
		Cursor cur = getCursorToSearch();
		ArrayList<Note> ret = new ArrayList<Note>();
		
			int _id = cur.getColumnIndex(DBAdapter.KEY_ID), _place = cur.getColumnIndex(DBAdapter.KEY_PLACE),
				_category = cur.getColumnIndex(DBAdapter.KEY_CATEGORY), _note = cur.getColumnIndex(DBAdapter.KEY_NOTE),
				_x = cur.getColumnIndex(DBAdapter.KEY_X_COORD), _y = cur.getColumnIndex(DBAdapter.KEY_Y_COORD);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				ret.add(new Note(cur.getInt(_id), cur.getInt(_x), cur.getInt(_y), cur.getString(_note), cur.getString(_place), cur.getString(_category), db, superMapFrag));
				cur.moveToNext();
			}
			Log.d("RETURN ARRAY", ret.toString());
			return ret;
		
	}
	public static String[] parseBundle(Bundle extras) {
		String[] returnStr = {"", "", ""};
		if (extras == null) {return returnStr;}
		String query = extras.getString("query");
		int categoryIndex = extras.getInt("category", -1);
		String searchstr = extras.getString("searchstr");
		if (query != null) {
			//search.setSearchString(query);
			//search
			returnStr[0] = query;
			Cursor temp = db.getLocations();
		
		} else if (categoryIndex != -1) {
			Cursor value = db.getLocation(null, -1, -1, categoryIndex, null);		//Contains the specific category value
			value.moveToFirst();
			returnStr[1] = value.getString(value.getColumnIndex(DBAdapter.KEY_CATEGORY));
		} else if (searchstr != null) {
			returnStr[2] = searchstr;
		}
		return returnStr;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSearchStr() {
		return searchStr;
	}

	public void setSearchStr(String searchStr) {
		this.searchStr = searchStr;
	}
	
}
