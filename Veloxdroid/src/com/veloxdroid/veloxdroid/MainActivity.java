package com.veloxdroid.veloxdroid;

import com.veloxdroid.utils.DownloadTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "vdPreferences";
	public static String veloxdroid_sdcard_path = Environment.getExternalStorageDirectory() + "/" + "veloxdroid";
	public static String avFissi_fileName = "Autovelox_Fissi.csv";
	public static String avMobili_fileName = "Autovelox_Mobili.csv";
	public static String avFissi_path = veloxdroid_sdcard_path + "/" + avFissi_fileName;
	public static String avMobili_path = veloxdroid_sdcard_path + "/" + avMobili_fileName;

	private static String file_url = "http://10.0.2.2:8080/VDServer/download";
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// check whether it's the first time you use the app
		// if the user is logged in, checkLogin() returns true else returns false
		if (checkLogin()) {
			// Check if the user has selected to automatically download autovelox files
			if (checkAutoSynch()) {
				// automatically fires download file - we do not pass from downloadFiles
				// but instead we directly instantiate a new DownloadTask and execute it
				final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
				downloadTask.execute(file_url);
			}
			Toast toast = Toast.makeText(getApplicationContext(), "Bentornato in Veloxdroid", 5);
			toast.show();
			// redirect to NavigationActivity
			Intent intent = new Intent(this, NavigationActivity.class);
			startActivity(intent);
		} else {
			Toast toast = Toast.makeText(getApplicationContext(), "Benvenuto in Veloxdroid", 5);
			toast.show();
		}
	}

	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// fire this method when select Settings from the option menu on the top
	// right of the GUI
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	// static reference to checkLogin - because we need a context from where
	// recover getSharedPrefereces in the private method below
	public static boolean checkLogin(Context context) {
		MainActivity ma = (MainActivity) context;
		return ma.checkLogin();
	}

	// checks whether the user is logged with his email
	private boolean checkLogin() {
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		String s = settings.getString("eMail", "");
		Log.d("checkLogin", s);
		if (s.equalsIgnoreCase(""))
			return false;
		else
			return true;
	}

	// checks whether the user has enabled/disabled auto synch of the Autovelox files
	private boolean checkAutoSynch() {
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		boolean autoSynch = settings.getBoolean("autoSynch", true);
		Log.d("autoSynch", String.valueOf(autoSynch));
		return autoSynch;
	}

	// method that downloads the file of the autoveloxesfires when clicking on the button Download on the GUI
	public void downloadFiles(View view) {
		Toast toast = Toast.makeText(getApplicationContext(), "Download file", 5);
		toast.show();
		// execute this when the downloader must be fired
		// we have to do in an AsyncTask separately because Android doesn't allow to do this in a standard activity
		final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
		downloadTask.execute(file_url);

		/********************
		 * // TODO - controllare perchè questa mProgressDialog dava dei problemi
		 ********************/
		// mProgressDialog
		// .setOnCancelListener(new DialogInterface.OnCancelListener() {
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// downloadTask.cancel(true);
		// }
		// });

	}

	// listener on the Login button of the GUI
	public void doLogin(View view) {
		int requestCode = 99; // hardcoded
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
		switch (aRequestCode) {
		case 99: { // hardcoded in doLogin
			String result = aData.getData().toString();
			Toast toast = Toast.makeText(getApplicationContext(), result, 10);
			toast.show();
			Intent intent = new Intent(this, NavigationActivity.class);
			startActivity(intent);
			break;
		}
		}
	}

}
