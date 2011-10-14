package com.project.geonotes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class CategoryDownloadService {//extends AsyncTask<Void, Void, Void> {
	private String username;
	private Context mContext;
	private Handler mPeriodicEventHandler;
	private static final int PERIODIC_EVENT_TIMEOUT = 900000;
	public CategoryDownloadService(String mUsername, Context context) {
		username = mUsername;
		mContext = context;
		mPeriodicEventHandler = new Handler();
	    mPeriodicEventHandler.postDelayed(periodic, PERIODIC_EVENT_TIMEOUT);
	}
	
	
	private Runnable periodic = new Runnable() {
		public void run() {
			Log.e("SERVICE", "IN THE SERVICE");
			Intent download = new Intent(mContext, CategoryDownloadAsyncTask.class);
			download.putExtra("username", username);
			download.putExtra("action", "download");
			int icon = android.R.drawable.stat_notify_sync;
			CategoryDownloadAsyncTask dl = new CategoryDownloadAsyncTask(mContext); //, new Notification(icon, null, when));
			dl.execute(download);
			//return null;
		}
	};

}
