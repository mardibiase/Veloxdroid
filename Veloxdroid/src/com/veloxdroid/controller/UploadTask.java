package com.veloxdroid.controller;

import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.veloxdroid.veloxdroid.MainActivity;

import android.os.AsyncTask;
import android.util.Log;

//Represents an asynchronous upload task used to send the Autovelox
public class UploadTask extends AsyncTask<String, Void, Boolean> {

	private StringEntity request;
	private String remotePath;

	public void setPostRequest(String request) {
		try {
			this.request = new StringEntity(request);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void setRemotePath(String path) {
		this.remotePath = path;
	}

	@Override
	protected Boolean doInBackground(String... str) {

		HttpPost post = new HttpPost(MainActivity.urlRemoteServer + remotePath);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = 5000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		post.addHeader("content-type", "application/x-www-form-urlencoded");
		post.setEntity(request);

		try {
			HttpResponse response = httpClient.execute(post);
			HttpEntity responseEntity = response.getEntity();
			String responseString = EntityUtils.toString(responseEntity);

			Log.d("Server", responseString.contains("OK") ? "Ok" : "Error");
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}