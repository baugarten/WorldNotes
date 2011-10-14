package com.project.geonotes;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class NoteOverlayItem extends OverlayItem {

	private Note note = null;
	
	public NoteOverlayItem(Note newNote) {
		super(new GeoPoint(newNote.getX(), newNote.getY()), newNote.getPlace(), newNote.getCategory());
		setNote(newNote);
	}

	public void setNote(Note note) {
		this.note = note;
	}

	public Note getNote() {
		return note;
	}
	

}
