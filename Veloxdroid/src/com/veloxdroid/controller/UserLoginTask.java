package com.veloxdroid.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import com.veloxdroid.veloxdroid.LoginActivity;
import com.veloxdroid.veloxdroid.MainActivity;
import com.veloxdroid.veloxdroid.R;


	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		
		private EditText mEmailView;
		private EditText mPasswordView;
		private String loginResult;
		private String mEmail;
		private String mPassword;
		private SharedPreferences settings;
		private LoginActivity loginActivity;
		
		public void setmEmailView(EditText mEmailView) {
			this.mEmailView = mEmailView;
			
		}

		public void setmPasswordView(EditText mPasswordView) {
			this.mPasswordView = mPasswordView;
		}

		public void setSettings(SharedPreferences settings) {
			this.settings = settings;
		}
		
		public void setLoginActivity(LoginActivity loginActivity) {
			this.loginActivity = loginActivity;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			mEmail = mEmailView.getText().toString();
			mPassword = mPasswordView.getText().toString();
			
			String base64_MD5_password = null;
			try {
				MessageDigest digester = MessageDigest.getInstance("MD5");
				digester.update(mPassword.getBytes(), 0, mPassword.length()-1);
				byte[] rawDigest = digester.digest();
				byte[] encoded = Base64.encode(rawDigest, Base64.DEFAULT);
		        base64_MD5_password = new String(encoded);		        
			} catch (NoSuchAlgorithmException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}			
			
			StringEntity stringEnt = null;
			Boolean toReturn = false;
			try {
				stringEnt = new StringEntity("mail=" + mEmail + "&pw=" + base64_MD5_password);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HttpPost post = new HttpPost(MainActivity.urlRemoteServer + "login");
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
			post.setEntity(stringEnt);

			try {
				HttpResponse response = httpClient.execute(post);
				HttpEntity responseEntity = response.getEntity();
				String responseString = EntityUtils.toString(responseEntity);
				System.out.println(responseString);
				if (responseString.contains("OK") || responseString.contains("REG")){
					loginResult = responseString;
					toReturn = true;
				}
				else {
					loginResult = "NO";
					toReturn = false;					
				}
			}
			catch (ConnectTimeoutException cte ){
				loginResult = "Operation timed out";
				toReturn = false;
//				Intent intent = new Intent(null, MainActivity.class);
//				startActivity(intent);
			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return toReturn;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
//			mAuthTask = null;
			loginActivity.showProgress(false);
			Intent resultIntent = null;
			if (success) {
				resultIntent = new Intent();
				Uri temp = Uri.parse(loginResult);
				resultIntent.setData(temp);				
				loginActivity.setResult(Activity.RESULT_OK, resultIntent);
				settings.edit().putString("eMail", mEmail).commit();
				loginActivity.finish();										
			} else {
				if(loginResult.contains("timed out")){
					Toast.makeText(loginActivity.getApplicationContext(), loginResult, Toast.LENGTH_SHORT).show();					
				}
				else{
					mPasswordView.setError(loginActivity.getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				}
				
			}
		}

		@Override
		protected void onCancelled() {
//			mAuthTask = null;
		loginActivity.showProgress(false);
		}
	}