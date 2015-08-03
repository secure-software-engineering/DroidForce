package de.ecspride.javaclasses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class InstrumentationHelper {
	public static EventPEP eventPep = null;
	public static RemoteServiceConnection connection = null;
	private final static String keyBaseName = "taintinfo";
	private static String pdpPackage = "de.tum.in.i22.uc.pdp.android";
	private static String pdpClassFull = "de.tum.in.i22.uc.pdp.android.pdpService";
	
	public static Map<Integer, Set<String>> sourceSinkConnection = new HashMap<Integer, Set<String>>();
	
	public static void initializeEventPEP(Context context){
		Log.i("PEP", "in InstrumentationHelper.initializeEventPEP");
		if(eventPep == null){
			eventPep = new EventPEP();
			eventPep.init();
		}
		if(connection == null)
			connection = new RemoteServiceConnection(eventPep);
		
		setupBindService(context);
	}
	
	private static void setupBindService(Context context){
		Log.i("PEP", "in InstrumentationHelper.setupBindService");
		Intent intent=new Intent();
	    intent.setClassName(pdpPackage, pdpClassFull);
	    intent.setAction("de.tum.in.i22.uc.pdp.android.pdpService");
	    context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}
	
	public static boolean isStmtExecutionAllowed(String eventName, int dataFlowAvailable, Object... parameter){
		Log.i("PEP", "in InstrumentationHelper.isStmtExecutionAllowed");
		
		if (eventName == null) {
			Log.e("DroidForce", "eventName is null!");
			throw new RuntimeException("error: eventName is null");
		}
		if (parameter == null) {
			Log.e("DroidForce", "parameter is null!");
			throw new RuntimeException("error: parameter is null");
		}
			
		Bundle event = new Bundle();
		
		event.putString("eventname", eventName);

		if(parameter.length%2 != 0)
			throw new RuntimeException("Ooops problem in isStmtExecutionAllowed");
		for(int i = 0; i < parameter.length; i++){
			event.putString(parameter[i].toString(), parameter[++i].toString());
		}
		
		if(dataFlowAvailable != -1){
			Log.i("PEP", "dataFlowAvailable="+dataFlowAvailable);
			Log.i("PEP", sourceSinkConnection.toString());
//			if(!sourceSinkConnection.containsKey(dataFlowAvailable))
//				throw new RuntimeException("Oops, there should be a correct ID");
			if(sourceSinkConnection.containsKey(dataFlowAvailable)){
				for(String cat : sourceSinkConnection.get(dataFlowAvailable))
					event.putString("DATA_" + cat, "true");
			}
		}

		return InstrumentationHelper.eventPep.isStmtExecutionAllowed(event);
	}
	
	public static void killBinder(Context context){
		if(eventPep.isBound){
			context.unbindService(connection);
			eventPep.isBound = false;
		}
	}
	
	public static void addTaintInformationToIntent(Intent i, HashSet<String> taintCategories){
		boolean intentHasNoExtras= i.getExtras() == null ? true : false;
		
		//A bit of limitation here, because we do only care about the extras
		if(!intentHasNoExtras){
			Bundle extras = i.getExtras();
			
			String taintKeyName = generateKeyNameForTaintInfo(extras.keySet());
			
			String taintInformation = null;
			
			if(taintCategories.size() > 1)
				taintInformation = taintCategories.toString().substring(1,taintCategories.toString().length()-1);
			else
				taintInformation = taintCategories.iterator().next();
			
			i.putExtra(taintKeyName, taintInformation);
		}
	}
	
	private static String generateKeyNameForTaintInfo(Set<String> allIntentKeys){
		String keyName = keyBaseName;
		int counter = 0;
		
		for(String intentKey : allIntentKeys){
			if(intentKey.startsWith(keyBaseName)){
				String possibleNumber = intentKey.substring(keyBaseName.length());
				if(possibleNumber.length() > 0 && TextUtils.isDigitsOnly(possibleNumber)){
					int currentCounter = Integer.parseInt(possibleNumber);
					counter = currentCounter + 1;
				}
			}
		}
		
		if(counter != 0)
			keyName += counter;
		
		return keyName;
	}
	
	public static void registerNewSourceSinkConnection(int counter, String taintInfoOfSource){
		Log.i("PEP", "in registerNewSourceSinkConnection(int counter, String taintInfoOfSource)");
		Set<String> taintInfos = new HashSet<String>();
		taintInfos.add(taintInfoOfSource);
		sourceSinkConnection.put(counter, taintInfos);
	}
	
	public static void registerNewSourceSinkConnection(int counter, Bundle bundle){
		Log.i("PEP", "in registerNewSourceSinkConnection(int counter, Bundle bundle)");
		int taintInfoKeyCounter = 0;
		
		if(bundle != null){
			for(String intentKey : bundle.keySet()){
				if(intentKey.startsWith(keyBaseName)){
					String possibleNumber = intentKey.substring(keyBaseName.length());
					if(possibleNumber.length() > 0 && TextUtils.isDigitsOnly(possibleNumber)){
						int currentCounter = Integer.parseInt(possibleNumber);
						if(taintInfoKeyCounter < currentCounter)
							taintInfoKeyCounter = currentCounter;
					}
				}
			}
			
			if(taintInfoKeyCounter == 0){
				Log.i("PEP", "bundle:" + bundle.toString());
				if(bundle.containsKey(keyBaseName)){
					String taintSourceCats = bundle.getString(keyBaseName);
					String[] allCats = taintSourceCats.split(",");
					sourceSinkConnection.put(counter, new HashSet<String>(Arrays.asList(allCats)));
				}
			}
			else{
				if(bundle.containsKey(keyBaseName+taintInfoKeyCounter)){
					String taintSourceCats = bundle.getString(keyBaseName+taintInfoKeyCounter);
					String[] allCats = taintSourceCats.split(",");
					sourceSinkConnection.put(counter, new HashSet<String>(Arrays.asList(allCats)));
				}
			}
		}
	}
}
