package com.project.geonotes;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserRegistration extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		
		final EditText username = (EditText)findViewById(R.id.username);
		final EditText password = (EditText)findViewById(R.id.password);
		final EditText email = (EditText)findViewById(R.id.email);
		Button register = (Button)findViewById(R.id.register);
		
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Installation.registerUser();
				Intent register = new Intent(UserRegistration.this, UserRegistrationAsyncTask.class);
				UserRegistrationAsyncTask task = new UserRegistrationAsyncTask(UserRegistration.this, new String[] { username.getText().toString(), password.getText().toString(),email.getText().toString()}, true);
				task.execute();
			}
		});
	}
	protected void onRegistrationSuccess() {
		Intent mainActivity = new Intent(UserRegistration.this, geoNotes.class);
		startActivity(mainActivity);
	}
	


}
