package com.veloxdroid.veloxdroid;

import java.math.RoundingMode;
import java.text.NumberFormat;

import com.veloxdroid.utils.SearcherAutovelox;
import com.veloxdroid.utils.UploadTask;
import com.veloxdroid.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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
	private TextView textLatit, textLongit, textSpeed, textAutoveloxSpeed;
	private SearcherAutovelox searcherAutovelox;
	private Thread threadAutovelox;
	private SharedPreferences settings;
	private NumberFormat numForm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_navigation);
		textLatit = (TextView) findViewById(R.id.valueLatit);
		textLongit = (TextView) findViewById(R.id.valueLongit);
		textSpeed = (TextView) findViewById(R.id.valueSpeed);
		textAutoveloxSpeed = (TextView) findViewById(R.id.valueAutoveloxSpeed);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);

		findViewById(R.id.btn_deleteAutovelox).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_feedback).setVisibility(View.INVISIBLE);

		// New runnable searcher autovelox class
		searcherAutovelox = new SearcherAutovelox(settings, getWindow().getDecorView().getRootView(), this);
		threadAutovelox = new Thread(searcherAutovelox);
		// number format expression for latit and longit when doing delete/feedback/signal autovelox
		numForm = NumberFormat.getInstance();
		numForm.setMinimumFractionDigits(5);
		numForm.setMaximumFractionDigits(5);
		numForm.setRoundingMode(RoundingMode.HALF_DOWN);
		
		// Set the appropriate default value to text fields indicators
		textLatit.setText("Waiting fix position");
		textLongit.setText("Waiting fix position");
		textSpeed.setText("Waiting fix position");
		textAutoveloxSpeed.setText("Waiting fix position");
	}

	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.navigation, menu);
		return true;
	}

	// listener for the settings menu on the top right corner
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.Logout:
			Utils.doLogout(settings);
			startActivity(new Intent(this, MainActivity.class));
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

		Log.d("Location", "New Location received");
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

		textLatit.setText(numForm.format(location.getLatitude()));
		textLongit.setText(numForm.format(location.getLongitude()));

		double km = location.getSpeed() * 3.6;
		textSpeed.setText((int) km + "km/h");
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
		if (!Utils.checkLogin(getSharedPreferences(MainActivity.PREFS_NAME, 0))) {
			Toast.makeText(getApplicationContext(), "Visitatore! Non puoi inviare autovelox", Toast.LENGTH_SHORT).show();
		} else {
			if (lastKnowLocation != null) {
				double latit = lastKnowLocation.getLatitude();
				double longit = lastKnowLocation.getLongitude();

				if (settings.getBoolean("autoSynch", true)) {
					Toast.makeText(getApplicationContext(), "Send autovelox", Toast.LENGTH_SHORT).show();
					UploadTask task = new UploadTask();
					task.setPostRequest("latit=" + numForm.format(latit) + "&longit=" + numForm.format(longit));
					task.setRemotePath("upload");
					task.execute();
				} else {
					Toast.makeText(getApplicationContext(), "Send autovelox buffered", Toast.LENGTH_SHORT).show();
					Utils.saveAsynchOperation("Send", latit, longit);
				}
			}
		}

	}

	// listener for the sendFeedback button
	public void doSendFeedback(View view) {
		if (!Utils.checkLogin(getSharedPreferences(MainActivity.PREFS_NAME, 0))) {
			Toast.makeText(getApplicationContext(), "Visitatore! Non puoi inviare feedback", Toast.LENGTH_SHORT).show();
		} else {

			if (settings.getBoolean("position", false)) {
				double latit = settings.getFloat("latit", 0);
				double longit = settings.getFloat("longit", 0);
				if (settings.getBoolean("autoSynch", true)) {
					Toast.makeText(getApplicationContext(), "Feedback autovelox", Toast.LENGTH_SHORT).show();
					UploadTask task = new UploadTask();

					task.setPostRequest("latit=" + numForm.format(latit) + "&longit=" + numForm.format(longit));
					task.setRemotePath("feedback");
					task.execute();
				} else {
					Toast.makeText(getApplicationContext(), "Feedback autovelox buffered", Toast.LENGTH_SHORT).show();
					Utils.saveAsynchOperation("Feed", latit, longit);
				}
			}
		}
		findViewById(R.id.btn_deleteAutovelox).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_feedback).setVisibility(View.INVISIBLE);

	}

	// listener for the delete button
	public void doDeleteAutovelox(View view) {
		if (!Utils.checkLogin(getSharedPreferences(MainActivity.PREFS_NAME, 0))) {
			Toast.makeText(getApplicationContext(), "Visitatore! Non puoi cancellare autovelox", Toast.LENGTH_SHORT).show();
		} else {
			if (settings.getBoolean("position", false)) {

				double latit = settings.getFloat("latit", 0);
				double longit = settings.getFloat("longit", 0);
				if (settings.getBoolean("autoSynch", true)) {
					Toast.makeText(getApplicationContext(), "Delete autovelox", Toast.LENGTH_SHORT).show();
					UploadTask task = new UploadTask();
					task.setPostRequest("latit=" + numForm.format(latit) + "&longit=" + numForm.format(longit));
					task.setRemotePath("deleteAV");
					task.execute();
				} else {
					Toast.makeText(getApplicationContext(), "Delete autovelox buffered", Toast.LENGTH_SHORT).show();
					Utils.saveAsynchOperation("Del", latit, longit);
				}

			}

		}
		findViewById(R.id.btn_deleteAutovelox).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_feedback).setVisibility(View.INVISIBLE);

	}

}
