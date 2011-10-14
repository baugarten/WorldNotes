package com.project.geonotes.provider;


import java.util.ArrayList;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.project.geonotes.DBAdapter;

public class SuggestionContentProvider extends ContentProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://com.project.geonotes/places");
	public static final String SUGGEST_COLUMN_TEXT_1 = DBAdapter.KEY_PLACE;
	public static final String SUGGEST_COLUMN_TEXT_2 = DBAdapter.KEY_CATEGORY;
	
	private static final int ALL_PLACES_CODE = 0;
	//private static final int ALL_CATEGORIES_CODE = 1;
	private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.project.geonotes", "places", ALL_PLACES_CODE);
        //uriMatcher.addURI("com.project.geonotes", "categories", ALL_CATEGORIES_CODE);
        // uriMatcher.addURI("com.google.androidcamp", "note/#", 1);
    }
    private DBAdapter mDbAdapter;
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// not supported
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
			case ALL_PLACES_CODE:
				return "vnd.android.cursor.dir/vnd.project.geonotes.places";
			/*case ALL_CATEGORIES_CODE:
				return "vnd.android.cursor.dir/vnd.project.geonotes.categories";
			*/
			default:
	            throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues cv) {
		// TODO Auto-generated method stub
		if (uriMatcher.match(uri) == ALL_PLACES_CODE) { //|| uriMatcher.match(uri) == ALL_CATEGORIES_CODE) {
			mDbAdapter.saveLocation(cv);
			getContext().getContentResolver().notifyChange(uri, null);
            return uri;
		} 
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.d("Content Provider", "Making Content Provider");
		mDbAdapter = new DBAdapter(getContext());
		mDbAdapter.open();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String selectionOrder) {
		// TODO Auto-generated method stub
		String query = uri.getLastPathSegment().toLowerCase();
		Cursor returnCursor = mDbAdapter.getSelectColumns(new String[] {DBAdapter.KEY_ID, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_TEXT_2});
		MatrixCursor matCur = new MatrixCursor(new String[] {"_ID", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID});
		try {
			returnCursor.moveToFirst();
			int arg0 = returnCursor.getColumnIndex(DBAdapter.KEY_ID);
			int arg1 = returnCursor.getColumnIndex(SUGGEST_COLUMN_TEXT_1);
			int arg2 = returnCursor.getColumnIndex(SUGGEST_COLUMN_TEXT_2);
			int counter = 0;
			ArrayList<String> catNames = new ArrayList<String>();
			
			while (!returnCursor.isAfterLast()) {
				String temp = returnCursor.getString(arg1);
				if (temp.toLowerCase().startsWith(query) ) {
					Log.d("SUGGESTION", returnCursor.getString(arg1));
					matCur.addRow(new String[] {counter + "", temp, "Details", "place@" + Integer.toString(returnCursor.getInt(arg0))});
					counter++;
				}
				temp =returnCursor.getString(arg2); 
				if (temp.toLowerCase().startsWith(query)) {
					Log.d("SUGGESTION", returnCursor.getString(arg2));
					if (catNames.contains(temp.toLowerCase())) {returnCursor.moveToNext(); continue;}
					
					matCur.addRow(new String[] {counter + "", temp, "View this Category", "category@" + Integer.toString(returnCursor.getInt(arg0))});
					catNames.add(temp.toLowerCase());
					
					counter++;
				}
				returnCursor.moveToNext();
			}
			matCur.moveToFirst();
			return ((Cursor)matCur);
		} catch (Exception e) {
				//do nothing
		}
		return null;
		
	}

	@Override
	public int update(Uri uri, ContentValues cv, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		if (uriMatcher.match(uri) == ALL_PLACES_CODE) { // || uriMatcher.match(uri) == ALL_CATEGORIES_CODE) {
			mDbAdapter.saveLocation(cv);
		}
		return 0;
	}
	@Override
	public void shutdown() {
		super.shutdown();
		if (mDbAdapter != null) {
			mDbAdapter.close();
		}
	}

}
