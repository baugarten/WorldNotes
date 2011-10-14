package com.project.geonotes;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Category {
	 private static final String JSON_NAME_ATTRIBUTE = "category_name";
     private static final String JSON_NOTE_PLACE_ATTRIBUTE = "note_place";
     private static final String JSON_NOTE_NOTE_ATTRIBUTE = "note_note";
     private static final String JSON_NOTE_LAT_ATTRIBUTE = "note_lat";
     private static final String JSON_NOTE_LONG_ATTRIBUTE = "note_long";
     private String categoryname, sourceUser;
     private String[] noteplace, notenote, notelat, notelong;

     /**
      * Creates a new Category.
      * 
      * @param name
      *            - the name of the Contact.
      * @param phoneNumber
      *            - the phone number of the Contact.
      * @param email
      *            - the email address of the Contact.
      */
     public Category(String name, String[] notePlace, String[] noteNote, String[] noteLat, String[] noteLong) {
    	 this.noteplace = new String[notePlace.length];
    	 this.notenote = new String[noteNote.length];
    	 this.notelat = new String[noteLat.length];
    	 this.notelong = new String[noteLong.length];

         this.categoryname = name;
         this.noteplace = notePlace;
         this.notenote = noteNote;
         this.notelat = noteLat;
         this.notelong = noteLong;
    	 Log.d("CATEGORY", Arrays.toString(noteplace) + " -- " + Arrays.toString(notenote) + " -- " + Arrays.toString(notelat) + " -- " + Arrays.toString(notelong));
    	 for (int i = 0; i < noteplace.length; i++) {
   			 noteplace[i] = noteplace[i].replaceAll("%", "");
   			 notenote[i] = notenote[i].replaceAll("%", "");
   			 notelat[i] = notelat[i].replaceAll("%", "");
   			 notelong[i] = notelong[i].replaceAll("%", "");

    	 }
    	 Log.d("CATEGORY", Arrays.toString(noteplace) + " -- " + Arrays.toString(notenote) + " -- " + Arrays.toString(notelat) + " -- " + Arrays.toString(notelong));         
     }
     /**
      * @return the name of this Category
      */
     public String getName() {
             return categoryname;
     }

     /**
      * @return the places contained in this category
      */
     public String[] getNotePlace() {
             return noteplace;
     }

     /**
      * @return the notes contained in this category
      */
     public String[] getNote() {
             return notenote;
     }
     public String[] getNoteLatitude() {
    	 return notelat;
     }
     public String[] getNoteLongitude() {
    	 return notelong;
     }
     /**
      * Returns a JSON encoded version of this contact
      * @return A JSON encoded string representation of this object
      * @throws JSONException 
      */
     public String toJSON() throws JSONException {
    	 JSONObject json = new JSONObject();
         //json.put(JSON_NAME_ATTRIBUTE, categoryname);
         json.put(JSON_NOTE_PLACE_ATTRIBUTE, implode(noteplace, "%"));
         json.put(JSON_NOTE_NOTE_ATTRIBUTE, implode(notenote, "%"));
         json.put(JSON_NOTE_LAT_ATTRIBUTE, implode(notelat, "%"));
         json.put(JSON_NOTE_LONG_ATTRIBUTE, implode(notelong,"%"));
         return json.toString();
     }

     /** 
      * Creates a new contact by through the provided JSON object
      * @param json JSON encoded string
      * @return Contact instance from json
      * @throws JSONException 
      */
     public static Category fromJSON(String json) throws JSONException {
             
             // Parse JSON
             JSONObject jsonObject = new JSONObject(json);
             String name = jsonObject.getString(JSON_NAME_ATTRIBUTE);
             /*String[] notePlaces = JSONArrayToString(jsonObject.getJSONArray(JSON_NOTE_PLACE_ATTRIBUTE));
             String[] noteNote = JSONArrayToString(jsonObject.getJSONArray(JSON_NOTE_NOTE_ATTRIBUTE));
             String[] noteLat = JSONArrayToString(jsonObject.getJSONArray(JSON_NOTE_LAT_ATTRIBUTE));
             String[] noteLong = JSONArrayToString(jsonObject.getJSONArray(JSON_NOTE_LONG_ATTRIBUTE));
             */
             String[] notePlaces = (jsonObject.getString(JSON_NOTE_PLACE_ATTRIBUTE)).split("%");
             String[] noteNote = jsonObject.getString(JSON_NOTE_NOTE_ATTRIBUTE).split("%");
             String[] noteLat = jsonObject.getString(JSON_NOTE_LAT_ATTRIBUTE).split("%");
             String[] noteLong = jsonObject.getString(JSON_NOTE_LONG_ATTRIBUTE).split("%");
             
             
             return new Category(name, notePlaces, noteNote, noteLat, noteLong);
     }
     private static String[] JSONArrayToString(JSONArray jsonArray) throws JSONException {
    	 String[] ret = new String[jsonArray.length()];
    	 if (jsonArray != null) { 
    		   for (int i=0;i<jsonArray.length();i++) { 
    			   ret[i] = (jsonArray.getString(i).toString()); 
    		   }
    	 }
    	 return null;
     }
     private static String implode(String[] ary, String delim) {
    	    String out = "";
    	    for(int i=0; i<ary.length; i++) {
    	        if(i!=0) { out += delim; }
    	        out += ary[i];
    	    }
    	    return out;
     }
     public void addSourceUser(String source) {
    	 this.sourceUser = source;
     }
	@Override
	public String toString() {
		return this.categoryname + " " + Arrays.toString(this.getNote());
	}



}
