package com.veloxdroid.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.veloxdroid.beans.Autovelox;
import com.veloxdroid.beans.Autovelox.Type;
import com.veloxdroid.veloxdroid.MainActivity;
import com.veloxdroid.veloxdroid.R;

public class SearcherAutovelox implements Runnable {

	private SharedPreferences settings;
	private float distanceInDegrees;
	private Location currentLocation;
	private Autovelox autovelox = null;
	private Context context;
	private View view;

	public SearcherAutovelox(SharedPreferences settings, View view, Context context) {

		this.settings = settings;
		this.view = view;
		this.context = context;

		long distance = settings.getInt("distance", 4000);
		// Because 30.9 meters is 1 seconds in degrees
		this.distanceInDegrees = (float) ((float) distance / 30.9 / 60.0 / 60.0);
	}

	private ArrayList<Autovelox> findAutovelox(Location location, String path) throws IOException {

		ArrayList<Autovelox> autoveloxes = new ArrayList<Autovelox>();

		// creazione di un bufferedreader per file csv degli autovelox
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;

		Type type;
		if (path.contains("Fissi"))
			type = Type.FISSO;
		else if (path.contains("Mobile"))
			type = Type.MOBILE;
		else
			type = Type.ALTRO;

		// while per leggere dal bufferedreader
		while ((line = br.readLine()) != null) {

			StringTokenizer st2 = new StringTokenizer(line, " ");
			// il primo token contenente lat-long-altro dato
			String first = st2.nextToken();
			// creo uno stringtokenizer diverso che ha come parametro la virgola
			StringTokenizer st3 = new StringTokenizer(first, ",");
			Double longit = null;
			Double latit = null;
			Boolean found = false;
			if (st3.hasMoreElements()) {
				// il primo token di st3 è la longitudine
				longit = Double.parseDouble(st3.nextToken());
			}
			if (st3.hasMoreElements()) {
				// il secondo token è la latitudine
				latit = Double.parseDouble(st3.nextToken());
			}

			// check dell'autovelox nel range - adesso va bene perchè abbiamo messo questo valore in distanceInDegrees
			if (Math.abs(longit - location.getLongitude()) < distanceInDegrees && Math.abs(latit - location.getLatitude()) < distanceInDegrees)
				found = true;
			int speed;
			// se l'autovelox è presente nel range
			if (found == true) {
				// crea un nuovo oggetto Autovelox e setta la sua latitudine/longitudine
				Autovelox autovelox = new Autovelox();
				autovelox.getLocation().setLatitude(latit);
				autovelox.getLocation().setLongitude(longit);
				autovelox.setType(type);

				// cerca il limite di velocità di tale autovelox - contenuto nell'altro stringtokenizer
				while (st2.hasMoreElements()) {
					String nextElem = st2.nextElement().toString();
					if (nextElem.contains("@")) {
						speed = Integer.parseInt(nextElem.substring(nextElem.indexOf('@') + 1));
						// aggiungi il parametro di velocità all'autovelox
						autovelox.setMaxSpeed(speed);
						break;
					}
				}
				// aggiunge l'autovelox alla collezione
				autoveloxes.add(autovelox);
			}
		}
		br.close();

		return autoveloxes;
	}

	private Autovelox findNearestAutovelox(ArrayList<Autovelox> autoveloxes, Location location) {

		// ricerca l'autovelox più in prossimità tra quelli trovati rispetto
		// alla location fornita
		// come parametro di chiamata del metodo
		ArrayList<Float> distances = new ArrayList<Float>();
		// se non ci sono autovelox trovati ritorna null
		if (autoveloxes.size() == 0)
			return null;
		else {
			for (Autovelox a : autoveloxes) {
				// per ogni location aggiungi la distanza fornita dal metodo
				// distanceTo()
				distances.add(location.distanceTo(a.getLocation()));
			}
		}
		// calcola l'indice dell'arraylist distances che ha valore minore -
		// distanceTo() restituisce un float
		int minIndex = distances.indexOf(Collections.min(distances));

		return autoveloxes.get(minIndex);
	}

	public void updateCurrentLocation(Location location) {
		this.currentLocation = location;
	}

	@Override
	public void run() {

		try {
			// cerca l'autovelox più vicino tra quelli fissi
			autovelox = findNearestAutovelox(findAutovelox(this.currentLocation, MainActivity.avFissi_path), this.currentLocation);
			// check se sei loggato
			if (Utils.checkLogin(settings)) {
				// cerca l'autovelox più vicino tra quelli mobili
				Log.d("SearcherAutovelox", "find_AV_Mobile");
				Autovelox autovelox_mobile = findNearestAutovelox(findAutovelox(this.currentLocation, MainActivity.avMobili_path), this.currentLocation);
				// calcola la distanza assoluta rispetto ad entrambi i files
				if (autovelox != null && autovelox_mobile != null) {
					float distanceFromFisso = currentLocation.distanceTo(autovelox.getLocation());
					float distanceFromMobile = currentLocation.distanceTo(autovelox_mobile.getLocation());
					// se quello mobile è più vicino, riassegna la variabile autovelox a quello più vicino
					if (distanceFromMobile < distanceFromFisso)
						autovelox = autovelox_mobile;
				} else if (autovelox_mobile != null)
					autovelox = autovelox_mobile;
			}
			if (autovelox != null) {
				Log.d("Autovelox2", "Trovato");

				// Change the visibility of hiden button
				view.findViewById(R.id.btn_deleteAutovelox).setVisibility(View.VISIBLE);
				view.findViewById(R.id.btn_feedback).setVisibility(View.VISIBLE);
				
				// Add to view the max speed of autovelox
				((TextView) view.findViewById(R.id.valueAutoveloxSpeed)).setText(autovelox.getMaxSpeed() + "km/h");

				// Save in shared variables the last know autovelox
				settings.edit().putFloat("latit", (float) autovelox.getLocation().getLatitude()).commit();
				settings.edit().putFloat("longit", (float) autovelox.getLocation().getLongitude()).commit();
				settings.edit().putBoolean("position", true).commit();

				notifyAutovelox();
			} else {
				Log.d("Autovelox2", "Non trovato");
				// Change the visibility of hiden button
				view.findViewById(R.id.btn_deleteAutovelox).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.btn_feedback).setVisibility(View.INVISIBLE);

				// Reset the maximum autovelox speed to unknow value
				((TextView) view.findViewById(R.id.valueAutoveloxSpeed)).setText("None");
				
				// Save in shared variables the false indicator of autovelox expired
				settings.edit().putBoolean("position", false).commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void notifyAutovelox() {

		if (settings.getBoolean("sounds", true)) {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(context, notification);
			r.play();
			r.play();
		}

		// Vibrate only if the checkbos is enabled
		if (settings.getBoolean("vibration", true)) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			// Get the user choose on duration of vibration
			vibrator.vibrate(settings.getInt("seek_vibration", 500));
		}
	}
}
