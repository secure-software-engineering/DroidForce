package com.example.smssenderOrig;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);
		
		TextView tv = (TextView) findViewById(R.id.resultInterComponent);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
		  return;
		}
		// get data via the key
		String destination = extras.getString("destination");
		String message = extras.getString("message");
		String imei = extras.getString("imei");
		
		tv.setText("dest: " + destination + "\nmessage: " + message + "\nimei: " + imei + "\n\nSMS sent...");
		
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(destination, null, message, null, null);
	}
	
	public void goBack(View view){
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}
}
