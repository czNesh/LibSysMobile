package com.example.libsysmobile;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener {

	EditText serverIP;
	EditText serverPort;

	Button saveSettings;

	boolean firstRun;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		serverIP = (EditText) findViewById(R.id.serverIP);
		serverPort = (EditText) findViewById(R.id.serverPort);
		saveSettings = (Button) findViewById(R.id.saveButton);
		saveSettings.setOnClickListener(this);
		Log.d("LibSys", "Setting");
		loadPrefs();
	}

	private void loadPrefs() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String serverIPValue = sp.getString("IP", "192.168.1.1");
		String serverPortValue = sp.getString("PORT", "6789");

		serverIP.setText(serverIPValue);
		serverPort.setText(serverPortValue);
	}

	private void savePrefs(String key, String value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Editor edit = sp.edit();
		edit.putString(key, value);
		edit.commit();
	}

	private void savePrefs(String key, boolean value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Editor edit = sp.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	@Override
	public void onClick(View arg0) {
		boolean valid = true;
		String validationString = "";

		if (!validIP(serverIP.getText().toString())) {
			valid = false;
			validationString += "Vložte platnou IP adresu\n";
		}

		String port = serverPort.getText().toString().trim();
		if (!validPort(port)) {
			valid = false;
			validationString += "Vložte platné èíslo portu\n";
		}

		if (valid) {
			savePrefs("IP", serverIP.getText().toString());
			savePrefs("PORT", serverPort.getText().toString());
			savePrefs("FIRSTRUN", false);
			Log.d("Settings", "SAVING SETTINGS");
			finish();
		} else {
			Toast.makeText(this, validationString, Toast.LENGTH_LONG).show();
		}

	}

	private boolean validIP(String ip) {
		String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

		if (ip.matches(IPADDRESS_PATTERN)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validPort(String port) {
		try {
			int d = Integer.parseInt(port);
			if (d > 0 && d < 65535) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}
