package com.veloxdroid.veloxdroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

	final String PREFS_NAME = "vdPreferences";
	private static String file_url = "http://www.robotstxt.org/robotstxt.html";
	private ProgressDialog mProgressDialog;

	 	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setMessage("Downloading...");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
			
	}

	@Override
	protected void onStart() {
		super.onStart();
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
		int requestCode = 99;
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData){
		switch(aRequestCode){
		case 99: {
			String result = aData.getData().toString();
			Toast toast = Toast.makeText(getApplicationContext(),
					result, 10);
			toast.show();
			break;
			}
		}
	}
	
//	public void doRegister(View view){
//		Intent intent = new Intent(this, RegisterActivity.class);
//		startActivity(intent);
//	}
	
	
	private class DownloadTask extends AsyncTask<String, Integer, String> {

	    private Context context;

	    public DownloadTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected String doInBackground(String... sUrl) {
	        // take CPU lock to prevent CPU from going off if the user 
	        // presses the power button during download
	        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
	             getClass().getName());
	        wl.acquire();

	        try {
	            InputStream input = null;
	            OutputStream output = null;
	            HttpURLConnection connection = null;
	            try {
	                URL url = new URL(sUrl[0]);
	                connection = (HttpURLConnection) url.openConnection();
	                connection.connect();

	                // expect HTTP 200 OK, so we don't mistakenly save error report 
	                // instead of the file
	                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
	                     return "Server returned HTTP " + connection.getResponseCode() 
	                         + " " + connection.getResponseMessage();

	                // this will be useful to display download percentage
	                // might be -1: server did not report the length
	                int fileLength = connection.getContentLength();

	                // download the file
	                input = connection.getInputStream();
	                
	                File folder = new File(Environment.getExternalStorageDirectory() + "/veloxdroid");
	                if (!folder.exists()) {
	                    folder.mkdir();
	                }	              
	                
	                output = new FileOutputStream(folder.getAbsolutePath().toString() + "/temp.html");

	                byte data[] = new byte[4096];
	                long total = 0;
	                int count;
	                while ((count = input.read(data)) != -1) {
	                    // allow canceling with back button
	                    if (isCancelled())
	                        return null;
	                    total += count;
	                    // publishing the progress....
	                    if (fileLength > 0) // only if total length is known
	                        publishProgress((int) (total * 100 / fileLength));
	                    output.write(data, 0, count);
	                }
	            } catch (Exception e) {
	                return e.toString();
	            } finally {
	                try {
	                    if (output != null)
	                        output.close();
	                    if (input != null)
	                        input.close();
	                } 
	                catch (IOException ignored) { }

	                if (connection != null)
	                    connection.disconnect();
	            }
	        } finally {
	            wl.release();
	        }
	        return null;
	    }
	    
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        mProgressDialog.show();
	    }

	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        // if we get here, length is known, now set indeterminate to false
	        mProgressDialog.setIndeterminate(false);
	        mProgressDialog.setMax(100);
	        mProgressDialog.setProgress(progress[0]);
	    }

	    @Override
	    protected void onPostExecute(String result) {
	        mProgressDialog.dismiss();
	        if (result != null)
	            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
	        else
	            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
	    }
	}

}
