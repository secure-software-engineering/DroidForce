package de.ecspride.javaclasses;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class EventPEP {
	private final String TAG = "PEP";
	boolean isBound = false;
	Messenger messenger = null;
	private Messenger replyTo = null;

	public final Queue<Boolean> pdpAnswers = new ConcurrentLinkedQueue<Boolean>();
	
	final Object waitOnMe = new Object();

	public void init() {
		Log.i("PEP", "in EventPEP.init");

		HandlerThread ht = new HandlerThread("threadName");
		ht.start();
		Looper looper = ht.getLooper();
		Handler.Callback callback = new IncomingHandler(this);
		Handler handler = new Handler(looper, callback);
		this.replyTo = new Messenger(handler);
	}

	public boolean isStmtExecutionAllowed(Bundle event) {
		Log.i("PEP", "in EventPEP.isStmtExecutionAllowed");

		Message m = Message.obtain();
		m.what = 1; // sync blocking
		m.setData(event);
		m.replyTo = this.replyTo;
		try {
			if (this.messenger != null) {
				Log.i("PEP", "Messenger was not NULL");
				synchronized (waitOnMe) {
					Log.i("PEP", "here");
					String isEmpty = this.messenger == null ? "TRUE" : "FALSE";
					Log.i("PEP", "isEmpty: " + isEmpty);
					this.messenger.send(m);
					try {
						waitOnMe.wait();
						boolean allowed = pdpAnswers.poll();
						Log.d(TAG, "decision on event " + event.getString("eventname")  + ": " + allowed);
						return allowed;
					} catch (InterruptedException e) {
						Log.e(TAG, "we should have gotten our answer now.\n"
								+ e.getMessage());
					}
				}
			}
			else
				Log.i("PEP", "Messenger was NULL - SHIT");
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage());
		}
		Log.i("PEP", "Inhibited action since no answer from PDP was received");
		return false;
	}

}
