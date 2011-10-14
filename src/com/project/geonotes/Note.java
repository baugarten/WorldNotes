package com.project.geonotes;

import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Representation of a note which has fields: name, cordinates( x and y), place,
 * and category.
 * 
 * @author Orange Humanoids
 * 
 */
public class Note extends Application {
	private int _id;
	/* Constants representing a note. */
	private int x;// x coordinate
	private int y; // y coordinate
	private String note = "";
	private String place = "";
	private String category = "";
	private DBAdapter db; // the database
	private MapFrag superMapFrag = null;
	/**
	 * Constructor, adding a new Note to the list of notes
	 * @param x: the X coordinate of the Note
	 * @param y: the Y coordinate of the Note
	 * @param note: comments made on the Note
	 * @param place: the place
	 * @param category: the category of the note
	 */
	private Dialog dialog = null;

	public Note(int x, int y, String note, String place, String category,
			DBAdapter db) {
		this._id = -1;
		this.x = x;
		this.y = y;
		this.note = note;
		this.place = place;
		this.category = category;
		this.db = db;

	}
	public Note(int id, int x, int y, String note, String place, String category,
			DBAdapter db, Fragment frag) {
		this._id = id;
		this.x = x;
		this.y = y;
		this.note = note;
		this.place = place;
		this.category = category;
		this.db = db;
		this.superMapFrag = (MapFrag)frag;
	}
	
	/**
	 * Constructor accepting a note _id parameter
	 * @param x: the X coordinate of the Note
	 * @param y: the Y coordinate of the Note
	 * @param note: comments made on the Note
	 * @param place: the place
	 * @param category: the category of the note
	 */
	public Note(int id, int x, int y, String note, String place,
			String category, DBAdapter db) {
		this.x = x;
		this.y = y;
		this.note = note;
		this.place = place;
		this.category = category;
		this._id = id;
		this.db = db;
	}

	private OnClickListener mCorkyListener = new OnClickListener() {
		/**
		 * Clicking on the save button, save and destroy the dialog
		 */
		public void onClick(View v) {
			saveAndDestroyDialog();
		}
	};
	
	private OnClickListener deleteListener = new OnClickListener() {
		/**
		 * Clicking on the save button, save and destroy the dialog
		 */
		public void onClick(View v) {
			deleteNote();
		}
	};

	/**
	 * Creates a dialogue with multiple text views with information about the
	 * note
	 * 
	 * @param mContext
	 *            : context of the dialog
	 */
	public void createDialog(Context mContext, boolean hideKeyboard) {
		dialog = new Dialog(mContext);

		dialog.setContentView(R.layout.dialog);
		dialog.setTitle("Edit a Location");
		
		dialog.setCanceledOnTouchOutside(true);

		TextView placetext = (TextView) dialog.findViewById(R.id.placetext);
		placetext.setText("Please edit the place name here:");
		EditText placeedit = (EditText) dialog.findViewById(R.id.placeedit);
		placeedit.setText(place);
		InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		TextView categorytext = (TextView) dialog
				.findViewById(R.id.categorytext);
		categorytext.setText("Please edit the category here:");
		EditText categoryedit = (EditText) dialog
				.findViewById(R.id.categoryedit);
		categoryedit.setText(category);

		TextView notetext = (TextView) dialog.findViewById(R.id.notetext);
		notetext.setText("Note:");
		EditText noteedit = (EditText) dialog.findViewById(R.id.noteedit);
		noteedit.setText(note);
		if (hideKeyboard) {
			Log.d("KEYBOARD", "Hide Keyboard");
			/*imm.hideSoftInputFromWindow(placeedit.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(categoryedit.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(noteedit.getWindowToken(), 0);*/
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
		Button button = (Button) dialog.findViewById(R.id.button);
		button.setOnClickListener(mCorkyListener);

		Button deleteButton = (Button) dialog.findViewById(R.id.delete_button);
		deleteButton.setOnClickListener(deleteListener);
		
		dialog.show();
	}

	protected void deleteNote() {
		Log.e("DeleteNote","Yup Deleting");
		
		db.delete(this._id);
		if (superMapFrag != null) {
			Log.d("Deleting actually", "deleting");
			((OnDeleteNoteListener)superMapFrag).onDeleteNote();
			
		} else {
			Log.e("ERROR_DELETING", "trying to delete without a reference to superMapFrag");
		}
		dialog.dismiss();
		
	}
	public interface OnDeleteNoteListener {
		public void onDeleteNote();
	}

	/**
	 * Saves the text in the dialog and closes the dialog
	 */
	private void saveAndDestroyDialog() {
		EditText placeedit = (EditText) dialog.findViewById(R.id.placeedit);
		place = placeedit.getText().toString();

		EditText categoryedit = (EditText) dialog
				.findViewById(R.id.categoryedit);
		category = categoryedit.getText().toString();

		EditText noteedit = (EditText) dialog.findViewById(R.id.noteedit);
		note = noteedit.getText().toString();

		this.saveNote();
		if (superMapFrag != null) {((OnNoteAddListener)superMapFrag).onNoteAdd(this);}
		dialog.dismiss();

	}

	/**
	 * Save a note, return true if note has been properly saved to database
	 * 
	 * @return boolean
	 */
	public boolean saveNote() {
		ContentValues cv = new ContentValues();
		cv.put(DBAdapter.KEY_PLACE, this.place);
		cv.put(DBAdapter.KEY_CATEGORY, this.category);
		cv.put(DBAdapter.KEY_NOTE, this.note);
		cv.put(DBAdapter.KEY_X_COORD, this.x);
		cv.put(DBAdapter.KEY_Y_COORD, this.y);
		cv.put(DBAdapter.KEY_ID, this._id);
		long rowID = this.db.saveLocation(cv);
		this._id = (int) rowID;
		return true;
	}

	// getters and setters for all private fields
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
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

}
