package com.veloxdroid.veloxdroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.veloxdroid.utils.DownloadTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "vdPreferences";
	private static String file_url = "http://www.robotstxt.org/robotstxt.html";
	private ProgressDialog mProgressDialog;


	 	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//se l'utente è già registrato ed ha già fatto login
		//direttamente passa nella schermata di "navigazione" 
		//altrimenti: checkFirstTimeUsage
		checkFirstTimeUsage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	// checks if the app is starting for the first time
	public void checkFirstTimeUsage() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if (settings.getBoolean("first_time", true)) {
			// the app is being launched for first time, do something
			Log.d("Comments", "First time");
			// first time task
			Toast toast = Toast.makeText(getApplicationContext(),
					"Benvenuto in Veloxdroid", 5);
			toast.show();
			// record the fact that the app has been started at least once
			settings.edit().putBoolean("first_time", false).commit();
		}else{
			Toast toast = Toast.makeText(getApplicationContext(), "Bentornato in Veloxdroid", 5);
			toast.show();
			Intent intent = new  Intent(this, NavigationActivity.class);
			startActivity(intent);
		}
	}

	public void downloadFiles(View view) {
		Toast toast = Toast.makeText(getApplicationContext(),
				"Clicked on the button!", 5);
		toast.show();

		// execute this when the downloader must be fired
		final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
		downloadTask.execute(file_url);
			
		mProgressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						downloadTask.cancel(true);
					}
				});

	}
	
	public void doLogin(View view){
		int requestCode = 99; //hardcoded
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData){
		switch(aRequestCode){
		case 99: { //hardcoded in doLogin
			String result = aData.getData().toString();
			Toast toast = Toast.makeText(getApplicationContext(),
					result, 10);
			toast.show();
			Intent intent = new Intent(this, NavigationActivity.class);
			startActivity(intent);
			break;
			}
		}
	}
	
//	public void doRegister(View view){
//		Intent intent = new Intent(this, RegisterActivity.class);
//		startActivity(intent);
//	}
	

}
