package com.project.geonotes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
/**
 * PlaceList is the Left Fragment in the Maps view. It represents the list of added places
 *
 * @author Ben
 *
 */
public class PlaceList extends ListFragment implements geoNotes.OnSearchSetListener, OnNoteAddListener, Note.OnDeleteNoteListener {
	private String category = "";			//category to display in sideview
	private String place = ""; 				//place to display in sideview if clicked on map (exact key word name)
	private String searchString = "";		//string to search list of places for
	private DBAdapter mDb;
//	private List<String> places;				//Array of place names to display
	private EditText mEditText;
	private Cursor mPlaces;
	private OnCursorChangeListener mOnCursorChange;
	private CursorSearch mSearch;
	private SimpleCursorAdapter mAdaptor;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("PlaceList", "In PlaceList");
		super.onActivityCreated(savedInstanceState);
		
		mDb = new DBAdapter(getActivity());
		mDb.open();
		
		//We pass search information to this fragment
		
		String[] searchParams = CursorSearch.parseBundle(savedInstanceState);
		place = searchParams[0];
		category = searchParams[1];
		searchString = searchParams[2];
		
		//set up the search if nothing was set
		if (mSearch == null) {
			mSearch = new CursorSearch(place, category, searchString, mDb, this);
		}
		
		mPlaces = mSearch.getCursorToSearch();		//retrieveListCursor();

		mAdaptor = new SimpleCursorAdapter(getActivity(), R.layout.placelist, mPlaces,
				new String[] {DBAdapter.KEY_PLACE, DBAdapter.KEY_ID},
				new int[] {R.id.place, R.id.id_tag},
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER | CursorAdapter.FLAG_AUTO_REQUERY);
		
		setListAdapter(mAdaptor);
		
		//Make sure its white, to white-out the whole left column
		getView().setBackgroundColor(0xFFFFFFFF);
	}
	/**
	 * I don't think this does anything..
	 * @param menu
	 * @param inflater
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d("Creating Menu", "Inflating everything");
		inflater.inflate(R.layout.searchbar, menu);
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	    super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	public void onCategorySet(int categoryID) {
		// TODO Auto-generated method stub
		Log.d("CATID", categoryID + "");
		if (mDb == null) {mDb = new DBAdapter(getActivity());}
		if (!mDb.isOpen()) {mDb.open();}
		
		Cursor value = mDb.getLocation(null, -1, -1, categoryID, null);		//Contains the specific category value
		if (value != null) {
			value.moveToFirst();
		} else {
			Log.d("DAMN", "FUCK THIS");
		}
		category = value.getString(value.getColumnIndex(DBAdapter.KEY_CATEGORY));
		if (mSearch == null) {
			mSearch = new CursorSearch("", category, "", mDb, this);
		} else {
			mSearch.setCategory(category);
		}
		mPlaces = mSearch.getCursorToSearch(); //retrieveListCursor();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);
		Note detailsNote;
		TextView idTag = (TextView)(((LinearLayout)(l.getChildAt(pos))).getChildAt(1));
		Cursor cur = mDb.getLocation(null, -1, -1, Integer.parseInt((String) idTag.getText()), null);
		try {
			int _id = cur.getColumnIndex(DBAdapter.KEY_ID), _place = cur.getColumnIndex(DBAdapter.KEY_PLACE),
				_category = cur.getColumnIndex(DBAdapter.KEY_CATEGORY), _note = cur.getColumnIndex(DBAdapter.KEY_NOTE),
				_x = cur.getColumnIndex(DBAdapter.KEY_X_COORD), _y = cur.getColumnIndex(DBAdapter.KEY_Y_COORD);
			cur.moveToFirst();
			detailsNote = new Note(cur.getInt(_id), cur.getInt(_x), cur.getInt(_y), cur.getString(_note), cur.getString(_place), cur.getString(_category), mDb);
			detailsNote.createDialog(getActivity(), true);
		} catch (Exception e) {
			//nothing
		}
	}
	@Override
	public void onNoteAdd(Note added) {
		Log.d("ONNOTEADD", "adding note");
		Cursor cur = mSearch.getCursorToSearch(); //retrieveListCursor();
		mAdaptor.changeCursor(cur);
		mAdaptor.notifyDataSetChanged();
	}
	@Override
	public void onDeleteNote() {
		Log.d("ONNOTEDELETE", "deleting note");
		category = "";
		place = "";
		searchString = "";
		mSearch.setCategory(category);
		mSearch.setPlace(place);
		mSearch.setSearchStr(searchString);
		Cursor cur = mSearch.getCursorToSearch();
		mAdaptor.changeCursor(cur);
		mAdaptor.notifyDataSetChanged();
	}
	@Override
	public void onSearchSet(String search) {
		Toast.makeText(getActivity(), "Custom Searches Not Currently Supported", 1000).show();
		
	}
	public interface OnCursorChangeListener {
		public void onCursorChange();
	}
}
