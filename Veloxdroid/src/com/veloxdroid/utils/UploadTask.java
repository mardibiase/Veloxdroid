package com.veloxdroid.utils;

import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.os.AsyncTask;
import android.util.Log;

//Represents an asynchronous upload task used to send the Autovelox
	public class UploadTask extends AsyncTask<String, Void, Boolean> {

		// Embedded URL Address
		final static String urlRemoteServer = "http://10.0.2.2:8080/";
		
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

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urlRemoteServer + remotePath);
			
			post.addHeader("content-type", "application/x-www-form-urlencoded");
			post.setEntity(request);

			try {
				HttpResponse response = client.execute(post);
				HttpEntity responseEntity = response.getEntity();
				String responseString = EntityUtils.toString(responseEntity);
				
				Log.d("Server", responseString.contains("OK")?"Ok":"Error");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

	}