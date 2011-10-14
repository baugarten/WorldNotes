package com.project.geonotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Installation {
	private static String sID = null;
	private static final String INSTALLATION = "INSTALLATION";
	private static Context mContext;
	public Installation(Context context) {
		mContext = context;
	}
	public synchronized static String id(Context context) {
		try {
			sID = readInstallationFile(new File(INSTALLATION));
		} catch (IOException e) {
			sID = null;
			e.printStackTrace();
		}
		return sID;
    }
	 private static String readInstallationFile(File installation) throws IOException {
	        //RandomAccessFile f = new RandomAccessFile(installation, "r");
		 	Log.d("INSTALLTION", "" + mContext);
		 	if (mContext != null) {
		        FileInputStream f = mContext.openFileInput(INSTALLATION);
		        byte[] bytes = new byte[(int) f.available()];
		        f.read(bytes);
		        f.close();
		        String tmp = new String(bytes);
		        Log.d("Installation", "Reading string: " + tmp);
		        return tmp;
		 	} else {
		 		return null;
		 	}
	    }
	 public static void writeInstallationFile(String username) throws IOException {
	        //FileOutputStream out = new FileOutputStream(new File(INSTALLATION));
		 	sID = username;
		 	FileOutputStream out = mContext.openFileOutput(INSTALLATION, Context.MODE_PRIVATE);
	        Log.d("Installation", "Writing username to file: " + sID);
	        String id = sID;
	        out.write(id.getBytes());
	        out.close();
	    }
	 public static void deleteFile() {
		 File f = new File(INSTALLATION);
		 f.delete();
	 }
	 /**
	  * Wrapper to the WebWrapper to register a user. Information should be validated BEFORE
	  * this method is called
	  * @param username: user'ss username
	  * @param password: user's password
	  * @param email: user's email address
	  */
	 public static void registerUser(String username, String password, String email) {
		 try {
			WebWrapper registration = new WebWrapper(username, password, email);
		} catch (Exception e) {
			Log.d("Installation", e.toString());
			Toast.makeText(mContext, "Problem registering username", 1000);
		} 			
	 }
	    
}
