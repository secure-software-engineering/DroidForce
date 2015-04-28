package de.tum.in.i22.uc.pdp.android;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

public class RemoteServiceConnection implements ServiceConnection {

	private Messenger messenger = null;
	private boolean isBound = false;
	private List<ServiceBoundListener> onServiceBound = new ArrayList<ServiceBoundListener>();
	
	public RemoteServiceConnection() {
	}

	@Override
	public void onServiceConnected(ComponentName component, IBinder binder) {
		this.messenger = new Messenger(binder);
		this.isBound = true;
		
		for (ServiceBoundListener listener : onServiceBound)
			listener.serviceBound(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName component) {
		this.messenger = null;
		this.isBound = false;
	}
	
	public boolean isBound() {
		return isBound;
	}
	
	public Messenger getMessenger() {
		return messenger;
	}
	
	public void addOnServiceBoundListener(ServiceBoundListener listener) {
		this.onServiceBound.add(listener);
	}
	
}