package com.project.geonotes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Dialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.android.maps.MapActivity;
import com.project.geonotes.R.id;

public class geoNotes extends MapActivity {
	private OnSearchSetListener mCategoryListenerList, mCategoryListenerMap;
	private String mUsername;
	private Intent intent;
    private String last;
    private String sID;
    private Dialog dialog = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.d("GEONOTES", "Back in geonotes");
        Button download = (Button)findViewById(R.id.reset);
        
		Intent downloadIntent = new Intent(geoNotes.this, CategoryDownloadService.class);
		downloadIntent.putExtra("username", mUsername);
		CategoryDownloadService dl = new CategoryDownloadService(mUsername, geoNotes.this);
		
        mUsername = Installation.id(geoNotes.this);
        
        download.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d("DOWNLOAD", "downloading");
				Intent download = new Intent(geoNotes.this, CategoryDownloadAsyncTask.class);
				download.putExtra("username", mUsername);
				download.putExtra("action", "download");
				int icon = android.R.drawable.stat_notify_sync;
				CategoryDownloadAsyncTask dl = new CategoryDownloadAsyncTask(geoNotes.this); //, new Notification(icon, null, when));
				dl.execute(download);
				
			}
        });
        
        Intent intent = getIntent();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	Log.d("SEARCH", "Now its searching");
        	String queryString = intent.getStringExtra(SearchManager.QUERY);
        	Fragment list = getFragmentManager().findFragmentById(R.id.list);
        	//Bundle query = new Bundle();
        	//query.putString("query", queryString);
        	//list.setArguments(query);
        	((OnSearchSetListener)list).onSearchSet(queryString);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        	Log.d("URI", "Action View");
        	Uri data = intent.getData();
        	if (data != null) {
        		Log.d("URI", data.toString());
        		last = data.getLastPathSegment();
        		
        		if (last.startsWith("place")) {
        			//Intent detailsPage = new Intent(this,DetailsActivity.class);			Not implemented yet
        			//startActivity(detailsPage);
        			DBAdapter db = new DBAdapter(this);
        			db.open();
        			Cursor cur = db.getLocation(null, -1, -1, Integer.parseInt(last.substring(last.indexOf("@")+1)), null);
        			cur.moveToFirst();
        			int _id = cur.getColumnIndex(DBAdapter.KEY_ID), _place = cur.getColumnIndex(DBAdapter.KEY_PLACE),
    					_category = cur.getColumnIndex(DBAdapter.KEY_CATEGORY), _note = cur.getColumnIndex(DBAdapter.KEY_NOTE),
    					_x = cur.getColumnIndex(DBAdapter.KEY_X_COORD), _y = cur.getColumnIndex(DBAdapter.KEY_Y_COORD);
        			Note specified = new Note(cur.getInt(_id), cur.getInt(_x), cur.getInt(_y), cur.getString(_note), cur.getString(_place), cur.getString(_category), db);
        			specified.createDialog(this, true);
        		} else {
        			Fragment list = getFragmentManager().findFragmentById(R.id.list);
        			Bundle query = new Bundle();
        			int catID = Integer.parseInt(last.substring(last.indexOf("@")+1));
        			Log.d("CATID", "" + catID);
        			mCategoryListenerList.onCategorySet(catID);
        			mCategoryListenerMap.onCategorySet(catID);
        			//query.putInt("category", Integer.parseInt(last.substring(last.indexOf("@")+1)));
        			//list.setArguments(query);
        		}
        	} 
        }
        
    }
    @Override
    public void onResume() {
    	super.onResume();
    	registerUser();
    }
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		Log.d("Creating Menu", "Inflating everything");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.searchbar, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) ((menu.findItem(R.id.search)).getActionView());
	    SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
	    searchView.setSearchableInfo(info);
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	    
	    
	    menu.getItem(3).setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				MapFrag mapFragment = (MapFrag) getFragmentManager().findFragmentById(id.map);
				//mapFragment.makeUseOfNewLocation(9);
				Location currentLoc = mapFragment.getCurrentLocation();
				mapFragment.moveToNewLocation(currentLoc, 9);
				return false;
			}
	    	
	    });
	    menu.getItem(2).setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				MapFrag mapFragment = (MapFrag) getFragmentManager().findFragmentById(id.map);
				mapFragment.showAll();
				return false;
			}
	    	
	    });
	    return super.onCreateOptionsMenu(menu);
	}
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.d("PREPARING MENU", "" + last);
    	if (last != null) {
            if (last.startsWith("place")) {
            	Log.d("Menu", "Place visible");
                    menu.setGroupVisible(R.id.invisible_category, false);
                    menu.setGroupVisible(R.id.invisible_place, true);
                    menu.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem arg0) {
                                            showDialog();
                                            return false;
                                    }
                    });
                    menu.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else if (last.startsWith("category")) {
            	Log.d("Menu", "Category visible");
                    menu.setGroupVisible(R.id.invisible_place, false);
                    menu.setGroupVisible(R.id.invisible_category, true);
                    menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem arg0) {
                                            // TODO Auto-generated method stub
                                            showDialog();
                                            return false;
                                    }
                    });
                    menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            } else {
            	Log.d("Menu", "Hiding everythig");
            	menu.setGroupVisible(R.id.invisible_place, false);
                menu.setGroupVisible(R.id.invisible_category, false);
            }
        } else {
        	Log.d("Menu", "Hiding ** everything");
            menu.setGroupVisible(R.id.invisible_place, false);
            menu.setGroupVisible(R.id.invisible_category, false);
        }
    	return super.onPrepareOptionsMenu(menu);
    }
    
    public interface OnSearchSetListener {
    	public void onCategorySet(int categoryID);
    	public void onSearchSet(String search);
    }

	@Override
	public void onAttachFragment(Fragment fragment) {
		try {
			if (fragment.getClass().toString().equals("class com.project.geonotes.PlaceList")) {
				mCategoryListenerList = (OnSearchSetListener) fragment;
			} else {
				Log.d("CLASS_NAME", fragment.getClass().toString());
				mCategoryListenerMap = (OnSearchSetListener) fragment;
			}
		} catch (ClassCastException e) {
			Log.e("ERROR", "SOmething went wrong");
		}
		super.onAttachFragment(fragment);
	}
	private void registerUser() {
		Installation install = new Installation(this);
		if (Installation.id(this) != null) {										//They are already logged in
			Log.d("Geonotes", Installation.id(this));
			mUsername = Installation.id(this);
		} else {
			Log.d("Geonotes", "Nothing found");
			Intent login = new Intent(geoNotes.this, UserLogin.class);
			startActivity(login);
			/*Intent getUsername = new Intent(geoNotes.this, UserRegistration.class);
			startActivity(getUsername);
			*/
		}
	}
	private void showDialog() {
		Log.d("DIALOG", "Showing sharing dialog");
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.sharedialog);
        dialog.setTitle("Share your map!");
        dialog.setCanceledOnTouchOutside(true);
        
        
        Button share = (Button) (dialog.findViewById(R.id.sharing));
        Log.d("SHARE", "" + dialog.findViewById(R.id.targetUsername));
        
        share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d("GEONOTES", last);
				CategoryUploadAsyncTask upload = new CategoryUploadAsyncTask(last, Installation.id(geoNotes.this), ((EditText)dialog.findViewById(R.id.targetUsername)).getText().toString(), new Notification(), (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE), new DBAdapter(geoNotes.this).open());
				Intent uploadIntent = new Intent(geoNotes.this, CategoryUploadAsyncTask.class);
				//uploadIntent.putExtra("category", last);
				upload.execute(uploadIntent);
				dialog.dismiss();
				
			}
        });
        dialog.show();
        
	}
}
