package com.veloxdroid.veloxdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.veloxdroid.utils.DownloadTask;
import com.veloxdroid.utils.Utils;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "vdPreferences";
	public static String veloxdroid_sdcard_path = Environment.getExternalStorageDirectory() + "/" + "veloxdroid";
	public static String avFissi_fileName = "Autovelox_Fissi.csv";
	public static String avMobili_fileName = "Autovelox_Mobili.csv";
	public static String savedAsynchOperation_fileName = "SavedAsynchOperation.csv";
	public static String avFissi_path = veloxdroid_sdcard_path + "/" + avFissi_fileName;
	public static String avMobili_path = veloxdroid_sdcard_path + "/" + avMobili_fileName;
	public static String savedAsynchOperation_path = veloxdroid_sdcard_path + "/" + savedAsynchOperation_fileName;
	public static String urlRemoteServer = "http://10.0.2.2:8080/VDServer/";

	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Create a referment to shared preferencies
		settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);

		// check whether it's the first time you use the app
		// if the user is logged in, checkLogin() returns true else returns false
		if (Utils.checkLogin(getSharedPreferences(MainActivity.PREFS_NAME, 0))) {
			// Check if the user has selected to automatically download autovelox files
			if (checkAutoSynch()) {
				// automatically fires download file - we do not pass from downloadFiles
				// but instead we directly instantiate a new DownloadTask and execute it
				new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/fissi");
				new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/mobili");
			}
			Toast.makeText(getApplicationContext(), "Bentornato in Veloxdroid", Toast.LENGTH_SHORT).show();
			// redirect to NavigationActivity
			startActivity(new Intent(this, NavigationActivity.class));
		} else {
			if (checkAutoSynch())
				new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/fissi");
			Toast.makeText(getApplicationContext(), "Benvenuto in Veloxdroid", Toast.LENGTH_SHORT).show();
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
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	// checks whether the user has enabled/disabled auto synch of the Autovelox files
	private boolean checkAutoSynch() {
		return settings.getBoolean("autoSynch", true);
	}

	// method that downloads the file of the autoveloxes fires when clicking on the button Download on the GUI
	public void downloadFiles(View view) {
		Toast.makeText(getApplicationContext(), "Download file", Toast.LENGTH_SHORT).show();
		// execute this when the downloader must be fired
		// we have to do in an AsyncTask separately because Android doesn't allow to do this in a standard activity
		new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/fissi");
		// check whether you are logged or not -> you don't need the Autovelox_Mobili.csv
		if (Utils.checkLogin(getSharedPreferences(MainActivity.PREFS_NAME, 0)))
			new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/mobili");

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
		startActivityForResult(new Intent(this, LoginActivity.class), requestCode);
	}

	@Override
	protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
		switch (aRequestCode) {
		case 99: { // hardcoded in doLogin
			String result = aData.getData().toString();
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
			// final DownloadTask downloadTaskFissi = new DownloadTask(MainActivity.this);
			// downloadTaskFissi.execute(urlRemoteServer + "download/fissi");
			// final DownloadTask downloadTaskMobili = new DownloadTask(MainActivity.this);
			// downloadTaskMobili.execute(urlRemoteServer + "download/mobili");
			startActivity(new Intent(this, NavigationActivity.class));
			break;
		}

		}
	}

	// handler for button visitatore in the GUI
	public void doVisitatore(View view) {

		final Intent intent = new Intent(this, NavigationActivity.class);

		// Check the existence of the Autovelox Fissi file and the auto synch is off 
		if (!Utils.checkExistenceFile() && !checkAutoSynch()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to download the file?").setTitle("Missing file");
			
			// Add the buttons
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK button
					settings.edit().putString("eMail", "Visitatore").commit();
					new DownloadTask(MainActivity.this).execute(urlRemoteServer + "download/fissi");
					startActivity(intent);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			// redirect to NavigationActivity
			settings.edit().putString("eMail", "Visitatore").commit();
			startActivity(intent);
		}
	}

	// handler for button register in the GUI
	public void doRegister(View view) {
		// doRegister va in doLogin - è la stessa parte della GUI
		doLogin(view);
	}

	@Override
	public void onBackPressed(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to exit?").setTitle("Exit");
		
		// Add the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User clicked OK button
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		

	}
}
