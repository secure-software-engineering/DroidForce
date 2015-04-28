package de.ecspride.events;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains all information about a single event
 * 
 * @author Siegfried Rasthofer
 */
public class EventInformation {
	private final String eventName;
	private final boolean instrumentAfterStatement;
	private final Set<Pair<Integer, String>> parameterInformation = new HashSet<Pair<Integer, String>>();
	
	public EventInformation(String eventName, boolean instrumentAfterStatement){
		this.eventName = eventName;
		this.instrumentAfterStatement = instrumentAfterStatement;
	}
	
	public void setParameterInformation(int paramPos, String paramEventName){
		Pair<Integer, String> paramEvent = new Pair<Integer, String>(paramPos, paramEventName);
		if(parameterInformation.contains(paramEvent))
			throw new RuntimeException("Oops, something went all wonky!");
		parameterInformation.add(paramEvent);
	}

	public Set<Pair<Integer, String>> getParameterInformation() {
		return parameterInformation;
	}

	public boolean isInstrumentAfterStatement() {
		return instrumentAfterStatement;
	}

	public String getEventName() {
		return eventName;
	}
}
