package de.fraunhofer.iese.apppdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import de.ecspride.Settings;
import de.util.FileUtil;

public class appPDP extends Activity {
	private static final String TAG = "appPDP";
	private static FileObserver observer;
	
	private File policyFile = null;
	
	private static RemoteServiceConnection deployPolicyConnection =
			new RemoteServiceConnection();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apppdpmain);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	    policyFile = copyFileFromAssetsToInternalStorage(Settings.policyFileName);
	    
	    Log.d(TAG, "Preparing pdpService");
		Intent start = new Intent("de.fraunhofer.iese.apppdp.pdpService");
		startService(start);
		Log.d(TAG, "pdpService instantiated");
			    
		// Create the service connection if it's not already there
		if (!deployPolicyConnection.isBound()) {
			Intent intent = new Intent();
		    intent.setClassName("de.fraunhofer.iese.apppdp",
		    		"de.fraunhofer.iese.apppdp.pdpService");
		    intent.setAction(pdpService.ACTION_PDP_SETPOLICY);
		    getApplicationContext().bindService(intent,
		    		deployPolicyConnection, Context.BIND_AUTO_CREATE);
		    
		    deployPolicyConnection.addOnServiceBoundListener(new ServiceBoundListener() {
				
				@Override
				public void serviceBound(RemoteServiceConnection connection) {
					deployPolicy();

					// register observer for policy.xml file and react on modifcations
					observer = new FileObserver(policyFile.getAbsolutePath()) {
						@Override
						public void onEvent(int event, String file) {
							if (event == FileObserver.MODIFY) {
								try {
									deployPolicy();
								} catch (Exception ex) {
									Toast.makeText(getApplicationContext(),
											"Could not load policy file", Toast.LENGTH_LONG)
											.show();
									;
								}
							}
						}
					};
					observer.startWatching();
				}
				
			});
		}
	}

	private File copyFileFromAssetsToInternalStorage(String fileName) {
		String fileInternally = this.getFilesDir().toString() + File.separator
				+ fileName;
		File file = new File(fileInternally);
		try {
			if (!file.exists())
				FileUtil.copyPolicyFileFromAssetsToInternalStorage(
						getApplicationContext(), fileName, fileInternally);
		} catch (Exception ex) {
			Log.e("ERROR", ex.getMessage());
		}

		try {
			Runtime.getRuntime().exec("chmod 777 " + fileInternally);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return file;
	}

	private void deployPolicy() {
		Log.d(TAG, "deployPolicy button");
		try {
			// Read in the policy file
			BufferedReader rdr = new BufferedReader(new FileReader(policyFile));
			String line = "";
			String data = "";
			while ((line = rdr.readLine()) != null)
				data += line + "\n";
			rdr.close();
						
			// Deploy the policy
			Bundle event = new Bundle();
			event.putString("policy", data);

			Message m = Message.obtain();
			m.setData(event);
			deployPolicyConnection.getMessenger().send(m);
			
			Toast.makeText(getApplicationContext(),
					"Policy deployed", Toast.LENGTH_LONG)
					.show();
		} catch (Exception e) {
			Log.e(TAG, "Exception during deployment");
			Log.e(TAG, e.getMessage());
		}
	}

	public void openPolicyFile(View v) {
		// open policy file
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(policyFile), "text/xml");
		startActivity(intent);
	}
	
	public void deployPolicy(View v) {
		deployPolicy();
	}

}
