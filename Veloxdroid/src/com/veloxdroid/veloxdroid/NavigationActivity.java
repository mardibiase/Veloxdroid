package com.veloxdroid.veloxdroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class NavigationActivity extends Activity implements LocationListener{


	private LocationManager lm;
	TextView txtInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		lm =(LocationManager) getSystemService(LOCATION_SERVICE);
	}

	@Override
	protected void onResume(){
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this);
		super.onResume();
	}

	@Override
	protected void onPause(){
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
		//4km è 0.035 di distanza in double usando latitudine e logitudine		
		//		5 decimali
		//		0.03500 = 4km -> 3 decimali
		//		ipotizza 1km =  0,00875
		//		dunque 875 x 4  = 3500 cicli
		// ipotizzando che venga rilevato un autovelox dentro il range prefissato, 
		// dobbiamo passare il controllo al metodo public float distanceTo (Location dest)
		// in modo da calcolare precisamente la distanza e dunque mostrare a video tutte le info
		
		txtInfo.setText("Latitude: " + location.getLatitude() + " Logitude: " + location.getLongitude());
		Location autovelox = null;
		try {
			autovelox = findAutovelox(location);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(autovelox != null){
			txtInfo.setText("distanza: " + autovelox.getSpeed());
		}
	}

	private Location findAutovelox(Location location) throws IOException{
		//creazione di un bufferedreader per file csv degli autovelox
		BufferedReader br = new BufferedReader(new FileReader("/sdcard/veloxdroid/Autovelox_Fissi.csv"));
		String line;
		//crea un arraylist di location per salvare tutti gli autovelox nel range predefinto
		ArrayList<Location> locations = new ArrayList<Location>();
		//while per leggere dal bufferedreader 
		while((line = br.readLine()) != null){

			StringTokenizer st2 = new StringTokenizer(line, " ");
			// il primo token contenente lat-long-altro dato
			String first = st2.nextToken(); 
			//creo uno stringtokenizer diverso che ha come parametro la virgola
			StringTokenizer st3 = new StringTokenizer(first, ",");
			Double longit = null;
			Double latit = null;
			Boolean found = false;
			if (st3.hasMoreElements()){
				//il primo token di st3 è la longitudine
				longit = Double.parseDouble(st3.nextToken());	
			}
			if (st3.hasMoreElements()){
				//il secondo token è la latitudine
				latit = Double.parseDouble(st3.nextToken());	
			}

			//check dell'autovelox nel range - adesso hardcoded a 0.035 che sono 4km
			if (Math.abs(longit - location.getLongitude()) < 0.035 && Math.abs(latit - location.getLatitude()) < 0.035) found = true;
			long speed;
			//se l'autovelox è presente nel range
			if (found == true ){
				//crea un nuovo oggetto "location" e setta la sua latitudine/longitudine
				Location autovelox = new Location("autov");
				autovelox.setLatitude(latit);
				autovelox.setLongitude(longit);				
				//cerca il limite di velocità di tale autovelox - contenuto nell'altro stringtokenizer 
				while (st2.hasMoreElements()) {
					String nextElem = st2.nextElement().toString();
					if (nextElem.contains("@")){
						speed = Long.parseLong(nextElem.substring(nextElem.indexOf('@')+1));
						//aggiungi il parametro di velocità all'autovelox
						autovelox.setSpeed(speed);
						break;
					}				
				}
				//aggiunge l'autovelox alla collezione
				locations.add(autovelox);
			}
		}
		br.close();
		
		//ricerca l'autovelox più in prossimità tra quelli trovati rispetto alla location fornita 
		//come parametro di chiamata del metodo
		ArrayList<Float> distances = new ArrayList<Float>();
		//se non ci sono autovelox trovati ritorna null
		if(locations.size() == 0) return null;
		else{
			for (Location l : locations){
				//per ogni location aggiungi la distanza fornita dal metodo distanceTo()
				 distances.add(location.distanceTo(l));
			}
		}
		//calcola l'indice dell'arraylist distances che ha valore minore - distanceTo() restituisce un float
		int minIndex = distances.indexOf(Collections.min(distances));
		//ritorna la location più prossima alla location attuale
		return locations.get(minIndex);
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

}
