package com.veloxdroid.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import com.veloxdroid.veloxdroid.MainActivity;

/*
 * To Do: 
 * Prevedere il download multiplo dei file (fissi e mobili)
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

	private Context context;
	private ProgressDialog mProgressDialog;

	public DownloadTask(Context context) {
		this.context = context;

		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setMessage("Downloading...");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
	}

	@Override
	protected String doInBackground(String... sUrl) {
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		wl.acquire();

		try {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {

				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(3000);
				connection.connect();
				// expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();
				// download the file
				input = connection.getInputStream();
				
				File folder = new File(MainActivity.veloxdroid_sdcard_path);
				if (!folder.exists()) {
					folder.mkdir();
				}
				
				if(url.getPath().contains("fissi"))
				output = new FileOutputStream(MainActivity.avFissi_path);
				else output = new FileOutputStream(MainActivity.avMobili_path);

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
			}catch (SocketTimeoutException ste){
				return "Server unavailable";
			}
			catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

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
			Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
	}

}
