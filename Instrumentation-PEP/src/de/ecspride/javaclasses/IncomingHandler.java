package de.ecspride.javaclasses;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IncomingHandler implements Handler.Callback {
	private EventPEP appPEP;

	public IncomingHandler(EventPEP paramMainActivity) {
		Log.i("PEP", "in IncomingHandler.IncomingHandler");
		this.appPEP = paramMainActivity;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		Log.i("PEP", "in IncomingHandler.handleMessage");
		
		String data = "";
		for (String key : msg.getData().keySet())
			data += key + " = " + msg.getData().getString(key);
		Log.d("PEP", "received answer from pdp service: " + data);
		
		synchronized (appPEP.waitOnMe) {
			appPEP.pdpAnswers.add(!msg.getData().getString("data").equals("false"));
			appPEP.waitOnMe.notifyAll();
		}
		return true;
	}
}
