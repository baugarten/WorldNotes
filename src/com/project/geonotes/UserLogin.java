package com.project.geonotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserLogin extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		final EditText username = (EditText)findViewById(R.id.loginusername);
		final EditText password = (EditText)findViewById(R.id.loginpassword);
		Button register = (Button)findViewById(R.id.register);
		Button login = (Button)findViewById(R.id.login);
		
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Installation.registerUser();
				Intent login = new Intent(UserLogin.this, UserRegistrationAsyncTask.class);
				UserRegistrationAsyncTask task = new UserRegistrationAsyncTask(UserLogin.this, new String[] {username.getText().toString(), password.getText().toString()}, false);
				task.execute();
			}
		});
		
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent register = new Intent(UserLogin.this, UserRegistration.class);
				startActivity(register);
			}
			
		});
	}
	public void onLoginSuccess() {
		Intent main = new Intent(this, geoNotes.class);
		startActivity(main);
	}
	
}
