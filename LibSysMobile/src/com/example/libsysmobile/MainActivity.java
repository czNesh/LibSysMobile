package com.example.libsysmobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private CameraService cs;

	// LAYOUT VARIABLES
	TextView infoString, titleString, authorString;
	Button mainButton;
	SocketClient sc;
	MainActivity a = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (sp.getBoolean("FIRSTRUN", true)) {
			Intent myIntent = new Intent(MainActivity.this,
					SettingsActivity.class);
			MainActivity.this.startActivity(myIntent);
		}

		// LAYOUT VARIABLES
		mainButton = (Button) findViewById(R.id.mainButton);
		infoString = (TextView) findViewById(R.id.infoString);
		titleString = (TextView) findViewById(R.id.bookTitleString);
		authorString = (TextView) findViewById(R.id.bookAuthorString);

		cs = new CameraService(this, this);

		((FrameLayout) findViewById(R.id.preview)).addView(cs);
		findViewById(R.id.preview).setVisibility(View.VISIBLE);

		MyOnCLickListener m = new MyOnCLickListener();
		mainButton.setOnClickListener(m);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case R.id.action_settings:
			Intent myIntent = new Intent(MainActivity.this,
					SettingsActivity.class);
			MainActivity.this.startActivity(myIntent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}

	public void setTitleS(String title) {
		titleString.setText(title);
	}

	public void setAuthor(String author) {
		authorString.setText(author);
	}

	public void setInfoString(String info) {
		infoString.setText(info);
	}

	public void setMainButtonLabel(String string) {
		mainButton.setText(string);
	}

	private class MyOnCLickListener implements View.OnClickListener {

		public MyOnCLickListener() {
			super();
		}

		@Override
		public void onClick(View v) {
			if (v == mainButton) {
				if (cs.hasCode()) {
					sc = new SocketClient(a);
					sc.send(String.valueOf(cs.getlastCode()));
					cs.setHasCode(false);
					setMainButtonLabel("Èíst kód");
				} else {
					cs.readCode();
				}
			}

		}

	}

	public void setButtonActive(boolean b) {
		mainButton.setEnabled(b);

	}
}
