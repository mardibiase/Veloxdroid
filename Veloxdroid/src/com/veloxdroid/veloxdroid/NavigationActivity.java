package com.veloxdroid.veloxdroid;

import com.veloxdroid.utils.SearcherAutovelox2;
import com.veloxdroid.utils.UploadTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/*
 * To Do:
 * 
 * Aggiungere la gestione degli autovelox mobili (secondo file)
 * Ricerca dei fissi e dei mobili al fine di ottenere un unico risultato (autovelox piu' vicino)
 * */

public class NavigationActivity extends Activity implements LocationListener {

	private Location lastKnowLocation;
	private LocationManager lm;
	private TextView txtInfo;
	private SearcherAutovelox2 searcherAutovelox;
	private Thread threadAutovelox;
	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_navigation);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		
		findViewById(R.id.btn_deleteAutovelox).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_feedback).setVisibility(View.INVISIBLE);

		// New runnable searcher autovelox class
		searcherAutovelox = new SearcherAutovelox2(settings, getWindow().getDecorView().getRootView(), this);
		threadAutovelox = new Thread(searcherAutovelox);
	}

	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// listener for the settings menu on the top right corner
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
	
	@Override
	protected void onResume() {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this);
		super.onResume();
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// 4km è 0.035 di distanza in double usando latitudine e logitudine
		// 5 decimali
		// 0.03500 = 4km -> 3 decimali
		// ipotizza 1km = 0,00875
		// dunque 875 x 4 = 3500 cicli
		// ipotizzando che venga rilevato un autovelox dentro il range prefissato,
		// dobbiamo passare il controllo al metodo public float distanceTo (Location dest)
		// in modo da calcolare precisamente la distanza e dunque mostrare a video tutte le info

		// Aggiorna l'ultima posizione
		lastKnowLocation = new Location(location);

		// SearcherAutovelox sa = new SearcherAutovelox(autoveloxes, txtInfo, btn_feedback, btn_delete, distance,
		// getSharedPreferences(MainActivity.PREFS_NAME, 0), getApplicationContext());
		// SearcherAutovelox sa = new SearcherAutovelox(autoveloxes, this, distance,
		// getSharedPreferences(MainActivity.PREFS_NAME, 0), getApplicationContext());
		// sa.execute(location);

		// update the with current location
		searcherAutovelox.updateCurrentLocation(location);
		// Finds the current location
		if (!threadAutovelox.isAlive())
			this.runOnUiThread(searcherAutovelox);

		txtInfo.setText("Latitude: " + location.getLatitude() + " Logitude: " + location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		// call the settings for GPS when disabled
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}


	// listener for the send Autvelox button
	public void doSendAutovelox(View view) {
		Toast.makeText(getApplicationContext(), "Send autovelox", Toast.LENGTH_SHORT).show();

		if (lastKnowLocation != null) {
			UploadTask task = new UploadTask();
			task.setPostRequest("latit=" + lastKnowLocation.getLatitude() + "&longit=" + lastKnowLocation.getLongitude());
			task.setRemotePath("VDServer/upload");
			task.execute();
		}
	}

	// listener for the sendFeedback button
	public void doSendFeedback(View view) {
		Toast.makeText(getApplicationContext(), "Feedback autovelox", Toast.LENGTH_SHORT).show();

		if (settings.getBoolean("position", false)) {
			UploadTask task = new UploadTask();
			double latit = settings.getFloat("latit", 0);
			double longit = settings.getFloat("longit", 0);
			task.setPostRequest("latit=" + latit + "&longit=" + longit);
			task.setRemotePath("VDServer/feedback");
			task.execute();
		}
	}

	// listener for the delete button
	public void doDeleteAutovelox(View view) {
		if (settings.getBoolean("position", false)) {
			Toast.makeText(getApplicationContext(), "Delete autovelox", Toast.LENGTH_SHORT).show();

			UploadTask task = new UploadTask();
			double latit = settings.getFloat("latit", 0);
			double longit = settings.getFloat("longit", 0);
			task.setPostRequest("latit=" + latit + "&longit=" + longit);
			task.setRemotePath("VDServer/deleteAV");
			task.execute();
		}
	}

}
