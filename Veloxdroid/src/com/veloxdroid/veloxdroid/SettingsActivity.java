package com.veloxdroid.veloxdroid;

import org.apache.http.impl.conn.ProxySelectorRoutePlanner;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		final SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
		
		TextView tv_accountEmail = (TextView)findViewById(R.id.textViewAccountEmail);
		tv_accountEmail.setText(settings.getString("eMail", "Visitatore"));
		
		CheckBox cb_sounds = (CheckBox)findViewById(R.id.checkBoxSounds);
		cb_sounds.setChecked(settings.getBoolean("sounds", true));
		
		CheckBox cb_vibration = (CheckBox)findViewById(R.id.checkBoxVibration);
		cb_vibration.setChecked(settings.getBoolean("vibration", true));
		SeekBar sb_vibration = (SeekBar)findViewById(R.id.seekBarVibrationDuration);
		sb_vibration.setEnabled(settings.getBoolean("vibration", true));
		sb_vibration.setProgress(settings.getInt("seek_vibration", 500));
		sb_vibration.setMax(1000);
		sb_vibration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				Log.d ("SeekBar", ""+progress);
				settings.edit().putInt("seek_vibration", progress).commit();
			}
		});
		
		EditText et_distance = (EditText)findViewById(R.id.editTextSetDistance);
		et_distance.setText(String.valueOf(settings.getInt("distance", 4000)), TextView.BufferType.EDITABLE);
	
		cb_sounds.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.edit().putBoolean("sounds", isChecked).commit();
			}
		});
		
		cb_vibration.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.edit().putBoolean("vibration", isChecked).commit();
				findViewById(R.id.seekBarVibrationDuration).setEnabled(isChecked);
			}
		});
		
		et_distance.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equalsIgnoreCase(""))
					settings.edit().putInt("distance", 4000).commit();
				else
					settings.edit().putInt("distance", Integer.parseInt(String.valueOf(s.toString()))).commit();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	


	
	
}
