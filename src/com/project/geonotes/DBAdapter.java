package com.project.geonotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {
	/* Constants to be used for fields in Database */
	public static final String KEY_ID = "_id";
	public static final String KEY_PLACE = "place";
	public static final String KEY_X_COORD = "x_coord";
	public static final String KEY_Y_COORD = "y_coord";
	public static final String KEY_NOTE = "note";
	public static final String KEY_CATEGORY = "category";
	
	/* Constants for Database setup */
	private static final String DATABASE_NAME = "geonotes.db";
    private static final String PLACES_TABLE = "places";
    private static final int DATABASE_VERSION = 2;
    
    /* Fields for use */
    private SQLiteDatabase mDb;
    private DBHelper mDBHelper;
    private final Context mContext;
    private boolean isOpen = false;
    
    public DBAdapter(Context other) {
    	this.mContext = other;
    	this.mDBHelper = new DBHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public boolean isOpen() {
    	return isOpen;
    }
    public DBAdapter open() throws SQLiteException {
    	Log.d("DB", "Opening");
        mDb = mDBHelper.getWritableDatabase();
        isOpen = true;
        Log.d("DB", "Opened");
        return this;
    }
    public void close() throws SQLiteException {
        mDb.close();
        isOpen = false;
    }
    /**
     * 
     * This is taken almost verbatim from lecture_4
     * @param locationData: The Content Values to store in the SQLite db
     * @return: rowID
     */
    public long saveLocation(ContentValues locationData) throws SQLiteException {
    	if (locationData.getAsInteger(KEY_ID) != -1) {
	    	long updatedRows = mDb.update(PLACES_TABLE, locationData, 
	    			KEY_ID + "=" + locationData.getAsInteger(KEY_ID), null);
	    	Log.d("DB", "Updated Row");
	    	return updatedRows;
    	}
    	
    	else {
    		Log.d("DB", "Created Row");
    		locationData.remove(KEY_ID);
            return mDb.insert(PLACES_TABLE, null, locationData);
        }
    	
    }
    /**
     * Returns a Cursor to Places that match the category
     * @param category: the category to return
     * @return: Cursor to Places that match category
     */
    public Cursor getLocationsByCategory(String category) {
        return mDb.query(PLACES_TABLE,
                         new String[] {KEY_ID, KEY_PLACE, KEY_X_COORD, KEY_Y_COORD, KEY_NOTE, KEY_CATEGORY},
                         KEY_CATEGORY + "='" + category + "'",
                         null,
                         null,
                         null,
                         KEY_PLACE + " DESC");
    }
    /**
     * Get specific location from database -- any argument except x_coord, y_coord and category should
     * give a unique result. If not, it gives the first one in the database
     * @param place: Place name, default null
     * @param x_coord: X coordinate of place, default -1
     * @param y_coord: Y coordinate of place, default -1
     * @param id: ID of entry in database, default -1
     * @param category: Category of place, default null
     * @return: Cursor to row
     */
    public Cursor getLocation(String place, double x_coord, double y_coord, long id, String category) {
    	String whereclause = "";
    	/* Build a whereclause condition if and only if the arguments are set */
    	if (!(place == null)) {
    		whereclause += KEY_PLACE + "=" + place; 
    	}
    	if (!(x_coord == -1)) {
    		whereclause += KEY_X_COORD + "=" + x_coord;
    	}
    	if (!(y_coord == -1)) {
    		whereclause += KEY_Y_COORD + "=" + y_coord;
    	}
    	if (!(id == -1)) {
    		whereclause += KEY_ID + "=" + id;
    	}
    	if (!(category == null)) {
    		whereclause += KEY_CATEGORY + "=" + category;
    	}
    	if (whereclause == "") {
    		whereclause = null;
    	}
    	return mDb.query(PLACES_TABLE,
    					new String[] {KEY_ID, KEY_PLACE, KEY_X_COORD, KEY_Y_COORD, KEY_NOTE, KEY_CATEGORY},
    					whereclause,
    					null,
    					null,
    					null,
    					null,
    					"1");
    					
    }
    public Cursor getLocations() {
    	return mDb.query(PLACES_TABLE, 
    			new String[] {KEY_ID, KEY_PLACE, KEY_X_COORD, KEY_Y_COORD, KEY_NOTE, KEY_CATEGORY},
    			null, 
    			null, 
    			null, 
    			null, 
    			null);
    }
    public Cursor getCategories() {
    	return mDb.query(PLACES_TABLE,
    			new String[] {KEY_CATEGORY},
    			null,
    			null,
    			KEY_CATEGORY,
    			null,
    			null,
    			null);
    }
    public Cursor getSelectColumns(String[] cols) {
    	return mDb.query(PLACES_TABLE,
    			cols,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null);
    }
    public int delete(int rowID) {
    	return mDb.delete(PLACES_TABLE, 
    			KEY_ID + "=" + rowID, null);
    }
    private static class DBHelper extends SQLiteOpenHelper {
    	private static final String DATABASE_CREATE =
    			String.format("CREATE TABLE %s " +
    					"(%s integer PRIMARY KEY autoincrement, " +
    					"%s text not null," +
    					"%s text not null," +
    					"%s text not null," +
    					"%s real not null," +
    					"%s real not null);",
    					PLACES_TABLE, KEY_ID, KEY_PLACE, KEY_NOTE, KEY_CATEGORY, KEY_X_COORD, KEY_Y_COORD);
    			
		public DBHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
            Log.w("DBAdapter", String.format("Upgrading from version %d to %d. All old data will be gone!",
                    oldVersion, newVersion));
                // Drop the old table.
                db.execSQL(String.format("drop table if exists %s", PLACES_TABLE));
                // Create a new one.
                onCreate(db);
		}
    	
    }
    /**
     * This is a helper method to populate the database for testing
     */
    public void populateDatabase() {
    	ContentValues cv = new ContentValues();
    	cv.put(KEY_ID, -1);
    	cv.put(KEY_PLACE, "Harry's Bar");
    	cv.put(KEY_CATEGORY, "Vaction");
    	cv.put(KEY_X_COORD, 37);
    	cv.put(KEY_Y_COORD, 46);
    	cv.put(KEY_NOTE, "Remember that crazy time we had there... oh the memories");
    	
    	this.saveLocation(cv);
    }
    public void addCategory(Category adding) {
    	String categoryName = adding.getName();
    	String[] notes = adding.getNote();
    	String[] places = adding.getNotePlace();
    	String[] latitudes = adding.getNoteLatitude();
    	String[] longitudes = adding.getNoteLongitude();
    	for (int i = 0; i < latitudes.length; i++) {
    		Log.d("DBADAPTER", "Adding category");
    		ContentValues cv = new ContentValues();
    		cv.put(DBAdapter.KEY_ID, -1);
    		cv.put(DBAdapter.KEY_CATEGORY, categoryName);
    		cv.put(DBAdapter.KEY_NOTE, notes[i]);
    		cv.put(DBAdapter.KEY_PLACE, places[i]);
    		cv.put(DBAdapter.KEY_X_COORD, latitudes[i]);
    		cv.put(DBAdapter.KEY_Y_COORD, longitudes[i]);
    		this.saveLocation(cv);
    	}
    }
	public boolean contains(Category category) {
		return this.getLocationsByCategory(category.getName()).getCount() > 0;
	}
}
