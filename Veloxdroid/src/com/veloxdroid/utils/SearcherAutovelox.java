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
import android.os.AsyncTask;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.veloxdroid.beans.Autovelox;
import com.veloxdroid.beans.Autovelox.Type;
import com.veloxdroid.veloxdroid.MainActivity;

public class SearcherAutovelox extends AsyncTask<Location, Integer, String> {

	private ArrayList<Autovelox> autoveloxes;
	private TextView txtInfo;
	private Autovelox autovelox = null;
	private float distanceInDegrees;
	private boolean sounds, vibration;
	private View view;
	private Vibrator vibrator;
	private SharedPreferences settings;
	private Context context;
	private Button btn_delete, btn_feedback;

	public SearcherAutovelox(ArrayList<Autovelox> autoveloxes, TextView txtInfo, Button btn_feedback, Button btn_delete, long distance,
			SharedPreferences settings, Context context) {
		this.autoveloxes = autoveloxes;
		this.txtInfo = txtInfo;
		this.btn_delete = btn_delete;
		this.btn_feedback = btn_feedback;

		// Because 30.9 meters is 1 seconds in degrees
		distanceInDegrees = (float) ((float) distance / 30.9 / 60.0 / 60.0);

		this.settings = settings;
		this.context = context;
	}

	private void findAutovelox(Location location, String path) throws IOException {
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

	@Override
	protected String doInBackground(Location... params) {

		try {
			findAutovelox(params[0], MainActivity.avFissi_fileName);
			autovelox = findNearestAutovelox(autoveloxes, params[0]);

			if (autovelox != null) {
				txtInfo.setText("maxSpeed: " + autovelox.getMaxSpeed());
				btn_delete.setVisibility(View.VISIBLE);
				btn_feedback.setVisibility(View.VISIBLE);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {

		if (autovelox != null) {
			txtInfo.setText("maxSpeed: " + autovelox.getMaxSpeed());
			notifyAutovelox();
		} else {
			txtInfo.setText("Autovelox non trovato");
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
