package com.veloxdroid.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import com.veloxdroid.veloxdroid.MainActivity;

import android.content.SharedPreferences;
import android.util.Log;

public class Utils {
		
	public static boolean checkLogin(SharedPreferences settings){
		String s = settings.getString("eMail", "");
		Log.d("checkLogin", s);
		if (s.equalsIgnoreCase("") || s.equalsIgnoreCase("visitatore"))
			return false;
		else
			return true;
	}
	
	public static void doLogout(SharedPreferences settings){
		settings.edit().putString("eMail", "").commit();
	}

	
	public static void saveAsynchOperation(String opType, double latit, double longit){
		
		PrintWriter out;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(MainActivity.savedAsynchOperation_path, true)));
		    out.print(opType + "," + latit + "," + longit + "\n");
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		

	}
	
	public static void flushAsynchOperation(){
		try {
			NumberFormat numForm = NumberFormat.getInstance();
			numForm.setMinimumFractionDigits(5);
			numForm.setMaximumFractionDigits(5);
			numForm.setRoundingMode(RoundingMode.HALF_DOWN);
			
			BufferedReader br = new BufferedReader(new FileReader(MainActivity.savedAsynchOperation_path));
			String line;
			while ((line = br.readLine()) != null) {
				UploadTask task = new UploadTask();
				
				StringTokenizer st = new StringTokenizer(line, ",");
				
				String strOpType = st.nextToken();
				String strLatit = st.nextToken();
				String strLongit = st.nextToken();

				task.setPostRequest("latit=" + numForm.format(Double.parseDouble(strLatit)) + "&longit=" + numForm.format(Double.parseDouble(strLongit)));
				
				if (strOpType.equalsIgnoreCase("Send"))
					task.setRemotePath("upload");
				else if (strOpType.equalsIgnoreCase("Feed"))
					task.setRemotePath("feedback");
				else
					task.setRemotePath("deleteAV");
				
				task.execute();
			}
			br.close();
			
			// Cancello tutto il file dopo il flush sul server
			File file = new File(MainActivity.savedAsynchOperation_path);
			file.delete();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
