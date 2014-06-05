package com.example.smssenderOrig;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.smssenderOrig.MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void sendMessageIMEI(View view) {
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		 
		EditText destiantionEditText = (EditText) findViewById(R.id.destination);
		String destination = destiantionEditText.getText().toString();
		
		EditText messageEditText = (EditText) findViewById(R.id.message);
		String message = messageEditText.getText().toString();
		
		EditText frequencyEditText = (EditText) findViewById(R.id.frequency);
		int frequency = Integer.parseInt(frequencyEditText.getText().toString());
		
		SmsManager smsManager = SmsManager.getDefault();
		for(int i = 0; i < frequency; i++)
			smsManager.sendTextMessage(destination, null, message + telephonyManager.getDeviceId(), null, null);
	
	}
	public void sendMessageNoIMEI(View view) {
		
		EditText destiantionEditText = (EditText) findViewById(R.id.destination);
		String destination = destiantionEditText.getText().toString();
		
		EditText messageEditText = (EditText) findViewById(R.id.message);
		String message = messageEditText.getText().toString();
		
		EditText frequencyEditText = (EditText) findViewById(R.id.frequency);
		int frequency = Integer.parseInt(frequencyEditText.getText().toString());
		
		SmsManager smsManager = SmsManager.getDefault();
		for(int i = 0; i < frequency; i++)
			smsManager.sendTextMessage(destination, null, message, null, null);
		
	}
	
	public void doInterComponentLeak(View view) {
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		
		EditText destiantionEditText = (EditText) findViewById(R.id.destination);
		String destination = destiantionEditText.getText().toString();
		
		EditText messageEditText = (EditText) findViewById(R.id.message);
		String message = messageEditText.getText().toString();
		
		EditText frequencyEditText = (EditText) findViewById(R.id.frequency);
		int frequency = Integer.parseInt(frequencyEditText.getText().toString());
		
		Set<String> tmp = new HashSet<String>();
		tmp.add("aaa");
		System.out.println(tmp.toString());
		
		Intent i = new Intent(this, DisplayMessageActivity.class);
		
		i.putExtra("destination", destination);
		i.putExtra("message", message);
		i.putExtra("imei", telephonyManager.getDeviceId());
		
		startActivity(i); 
	}
	

}
