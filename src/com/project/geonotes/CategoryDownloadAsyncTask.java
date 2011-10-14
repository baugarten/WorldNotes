package com.project.geonotes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CategoryDownloadAsyncTask extends AsyncTask<Intent, Void, Void> {
	private AccountManager mAccountManager;
	private Account[] accounts;
	private String uniqueID;
	private WebWrapper mWrapper;
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private Context mContext;
	private DBAdapter mDb;
	public CategoryDownloadAsyncTask(Context mContext) {//, //AccountManager manager, Notification not, NotificationManager notMan) {
//		mAccountManager = manager;
//		mNotification = not;
//		mNotificationManager = notMan;
		this.mContext = mContext;
		mDb = new DBAdapter(mContext);
		mDb.open();
		Log.e("DOWNLOAD", "Creating");
	}


	@Override
	protected Void doInBackground(Intent... arg0) {
		Log.e("ASYNCTASK", "Heeeeeellllo");
		uniqueID = Installation.id(mContext);
		Intent intent = arg0[0];
		Bundle extras = intent.getExtras();
		if (extras.getString("action").equals("download")) {
			try {
				mWrapper = new WebWrapper(uniqueID);
				ArrayList<Category> toAdd;
				toAdd = mWrapper.getCategories();
				for (int i = 0; i < toAdd.size(); i++) {
					if (!mDb.contains(toAdd.get(i))) {
						mDb.addCategory(toAdd.get(i));
					}
				}
			} catch (ClientProtocolException e) {
				makeErrorToast("Client Protocol Exception");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				makeErrorToast("URI Syntax Exception");
				e.printStackTrace();
			} catch (IOException e) {
				makeErrorToast("Input/Output Exception");
				e.printStackTrace();
			} catch (JSONException e) {
				makeErrorToast("JSON Exception");
				e.printStackTrace();
			} catch (ServerException e) {
				makeErrorToast("Problems connecting to server");
				e.printStackTrace();
			}
			mDb.close();
			/*} catch (Exception e) {
	            Toast.makeText(mContext, "Could not connect to the server.", 1000).show();
	            return null;
			}*/
		} else {
			//parse extras, get info for WebWrapper call and initialize 
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		//produce notification on top bar for new categories
	}	
	
	private void makeErrorToast(String errorMessage) {
		Toast.makeText(mContext, errorMessage, 1000);
	}

}
