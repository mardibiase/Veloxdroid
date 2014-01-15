package com.veloxdroid.veloxdroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.veloxdroid.beans.Autovelox;
import com.veloxdroid.utils.SearcherAutovelox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
	private ArrayList<Autovelox> autoveloxes;
	long distance;
	private boolean sounds, vibration;
	private Vibrator vibrator;
	private View myView;
	private Button btn_feedback, btn_delete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);

		autoveloxes = new ArrayList<Autovelox>();

		final SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		distance = settings.getInt("distance", 4000);
		sounds = settings.getBoolean("sounds", true);
		vibration = settings.getBoolean("vibration", true);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		btn_feedback = (Button) findViewById(R.id.btn_feedback);
		btn_delete = (Button) findViewById(R.id.btn_deleteAutovelox);
		this.myView = findViewById(R.layout.activity_navigation);
	}

	@Override
	protected void onResume() {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		lm.removeUpdates(this);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation, menu);
		return true;
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

		SearcherAutovelox sa = new SearcherAutovelox(autoveloxes, txtInfo, btn_feedback, btn_delete, distance,
				getSharedPreferences(MainActivity.PREFS_NAME, 0), getApplicationContext());
		sa.execute(location);

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

	// Represents an asynchronous upload task used to send the Autovelox
	public class UploadTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Toast toast;

			if (lastKnowLocation != null) {

				double lat = lastKnowLocation.getLatitude();
				double lon = lastKnowLocation.getLongitude();
				StringEntity ent = null;
				try {
					ent = new StringEntity("latit=" + lat + "&longit=" + lon);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://10.0.2.2:8080/VDServer/upload");
				post.addHeader("content-type", "application/x-www-form-urlencoded");
				post.setEntity(ent);

				try {
					HttpResponse response = client.execute(post);
					HttpEntity responseEntity = response.getEntity();
					String responseString = EntityUtils.toString(responseEntity);
					System.out.println(responseString);
					if (responseString.contains("OK")) {
						toast = Toast.makeText(getApplicationContext(), "Uploaded", 10);
						toast.show();
					} else {
						toast = Toast.makeText(getApplicationContext(), "Error", 10);
						toast.show();
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				toast = Toast.makeText(getApplicationContext(), "No location pixed", 10);
				toast.show();
			}
			return true;
		}

	}

	//listener for the send Autvelox button
	private void doSendAutovelox(View view) {
		UploadTask task = new UploadTask();
		task.execute();
	}
	
	//listener for the sendFeedback button 
	private void doSendFeedback(View view){
		/********************
		 * // TODO - completare l'implementazione del metodo
		 ********************/
	}

	//listener for the delete button 
	private void doDeleteAutovelox(View view){
		/********************
		 * // TODO - completare l'implementazione del metodo
		 ********************/
	}
	
	//listener for the vibration event
	public void doVibration(View view) {

		final SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		// Vibrate only if the checkbos is enabled
		if (settings.getBoolean("vibration", true)) {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			// Get the user choose on duration of vibration
			vibrator.vibrate(settings.getInt("seek_vibration", 500));
		}
	}

	//listener for the settings menu on the top right corner
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

}
