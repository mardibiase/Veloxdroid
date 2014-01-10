package com.veloxdroid.veloxdroid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.veloxdroid.beans.Autovelox;
import com.veloxdroid.beans.Autovelox.Type;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/*
 * To Do:
 * 
 * Aggiungere la gestione degli autovelox mobili (secondo file)
 * Ricerca dei fissi e dei mobili al fine di ottenere un unico risultato (autovelox pi� vicino)
 * */

public class NavigationActivity extends Activity implements LocationListener{

	private Location lastKnowLocation;
	private LocationManager lm;
	private TextView txtInfo;
	private ArrayList<Autovelox> autoveloxes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		lm =(LocationManager) getSystemService(LOCATION_SERVICE);
		
		autoveloxes = new ArrayList<Autovelox>();
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
		
		// Aggiorna l'ultima posizione
		lastKnowLocation = new Location(location);
		
		txtInfo.setText("Latitude: " + location.getLatitude() + " Logitude: " + location.getLongitude());
		Autovelox autovelox = null;
		try {
			findAutovelox(location, "/sdcard/veloxdroid/Autovelox_Fissi.csv");
			//findAutovelox(location, "/sdcard/veloxdroid/Autovelox_Fissi.csv");
			autovelox = findNearestAutovelox(autoveloxes, location);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(autovelox != null){
			txtInfo.setText("maxSpeed: " + autovelox.getMaxSpeed());
		}
	}


	/*
	 * Bisogna togliere 0.035 dall'if che controlla la distanza ed inserire in una variabile di settings
	 * */
	private void findAutovelox(Location location, String path) throws IOException{
		//creazione di un bufferedreader per file csv degli autovelox
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		
		Type type;
		if (path.contains("Fissi"))
			type = Type.FISSO;
		else if (path.contains("Mobile"))
			type = Type.MOBILE;
		else
			type = Type.ALTRO;
			

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
			int speed;
			//se l'autovelox è presente nel range
			if (found == true ){
				//crea un nuovo oggetto "location" e setta la sua latitudine/longitudine
				Autovelox autovelox = new Autovelox();
				autovelox.getLocation().setLatitude(latit);
				autovelox.getLocation().setLongitude(longit);
				autovelox.setType(type);
				
				//cerca il limite di velocità di tale autovelox - contenuto nell'altro stringtokenizer 
				while (st2.hasMoreElements()) {
					String nextElem = st2.nextElement().toString();
					if (nextElem.contains("@")){
						speed = Integer.parseInt(nextElem.substring(nextElem.indexOf('@')+1));
						//aggiungi il parametro di velocità all'autovelox
						autovelox.setMaxSpeed(speed);
						break;
					}				
				}
				//aggiunge l'autovelox alla collezione
				autoveloxes.add(autovelox);
			}
		}
		br.close();
		
	}
	
	private Autovelox findNearestAutovelox(ArrayList<Autovelox> autoveloxes, Location location){

		//ricerca l'autovelox più in prossimità tra quelli trovati rispetto alla location fornita 
		//come parametro di chiamata del metodo
		ArrayList<Float> distances = new ArrayList<Float>();
		//se non ci sono autovelox trovati ritorna null
		if(autoveloxes.size() == 0) return null;
		else{
			for (Autovelox a : autoveloxes){
				//per ogni location aggiungi la distanza fornita dal metodo distanceTo()
				distances.add(location.distanceTo(a.getLocation()));
			}
		}
		//calcola l'indice dell'arraylist distances che ha valore minore - distanceTo() restituisce un float
		int minIndex = distances.indexOf(Collections.min(distances));

		return autoveloxes.get(minIndex);
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
	
	public void doSendAutovelox(View view){
		
		Toast toast;
		
		
		
		if (lastKnowLocation != null){
			toast = Toast.makeText(getApplicationContext(), "Uploaded: " + lastKnowLocation.getLatitude(), 10);
			toast.show();
			
			double lat = lastKnowLocation.getLatitude();
			double lon = lastKnowLocation.getLongitude();
			StringEntity ent = null;
			try {
				ent = new StringEntity("latit=" + lat + "longit=" + lon);
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
				if (responseString.contains("OK")){
					toast = Toast.makeText(getApplicationContext(), "Uploaded", 10);
					toast.show();
				}
				else {
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
			}
		} else {
			toast = Toast.makeText(getApplicationContext(), "No location pixed", 10);
			toast.show();
		}
		
	}

}
