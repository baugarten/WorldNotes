package com.project.geonotes;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class UserRegistrationAsyncTask extends AsyncTask<Intent, Void, Void> {
	private Activity mContext;
	private String username;
	private String password;
	private String email;
	
	private boolean register;
	private boolean success;
	
	public UserRegistrationAsyncTask(Activity mContext, String[] info, boolean register) { // username, String password, String email) {
		this.mContext = mContext;
		this.username = info[0]; //username;
		this.password = info[1]; //password;
		if (info.length > 2) {
			this.email = email = info[2];
		} else {
			this.email = null;
		}
		this.register = register;
	}
	@Override
	protected Void doInBackground(Intent... arg0) {
		if (!register && validateInformation(username, password, null)) {
			try {
				password = UtilEncrypt.byteArrayToHexString(UtilEncrypt.computeHash(password));
				WebWrapper login = new WebWrapper(username, password);
				success = login.loginSuccess();
				Log.d("LOGIN", "" + success);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (validateInformation(username, password, email)){
			try {
				password = UtilEncrypt.byteArrayToHexString(UtilEncrypt.computeHash(password));
				WebWrapper register = new WebWrapper(username, password, email);
				success = register.loginSuccess();
				Log.d("REGISTER", "" + success);
	    	} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Log.d("ASYNCTASK", "Launching main");
		if (success) {
			try {
				Installation.writeInstallationFile(username);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (register) { 
				((UserRegistration)mContext).onRegistrationSuccess();
			} else {
				((UserLogin)mContext).onLoginSuccess();
			}
		} else {
			//login/registration failed
		}
		
	}
	/**
	 * Some type of validation (i.e. password >= 6 characters, email if of form *@*.*, idk
	 * @param username
	 * @param password
	 * @param email
	 * @return
	 */
	private boolean validateInformation(String username, String password, String email) {
		return true;
	}

	

}
