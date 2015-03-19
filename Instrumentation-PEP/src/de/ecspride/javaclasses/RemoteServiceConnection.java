package de.ecspride.javaclasses;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class RemoteServiceConnection implements ServiceConnection {

	private EventPEP appPEP;
	
	public RemoteServiceConnection(EventPEP paramMainActivity) {
		Log.i("PEP", "in RemoteServiceConnection.RemoteServiceConnection");
		this.appPEP = paramMainActivity;
	}
	
	@Override
	public void onServiceConnected(ComponentName component, IBinder binder) {
		Log.i("PEP", "in RemoteServiceConnection.onServiceConnected");
		this.appPEP.messenger = new Messenger(binder);
		this.appPEP.isBound = true;
	}

	@Override
	public void onServiceDisconnected(ComponentName component) {
		Log.i("PEP", "in RemoteServiceConnection.onServiceDisconnected");
		this.appPEP.messenger = null;
		this.appPEP.isBound = false;
	}

}