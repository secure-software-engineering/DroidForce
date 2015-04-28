package de.tum.in.i22.uc.pdp.core;

import java.util.ArrayList;
import java.util.HashMap;

import de.tum.in.i22.uc.cm.settings.Settings;

public class ActionDescriptionStore {
	private HashMap<String, ArrayList<EventMatch>> eventMatchList = null;
	private HashMap<String, ArrayList<Mechanism>> mechanismList = null;

	private static ActionDescriptionStore instance = null;

	public ActionDescriptionStore() {
		this.eventMatchList = new HashMap<String, ArrayList<EventMatch>>();
		this.mechanismList = new HashMap<String, ArrayList<Mechanism>>();
	}

//	public static ActionDescriptionStore getInstance() {
//		/*
//		 * This implementation may seem odd, overengineered, redundant, or all of it.
//		 * Yet, it is the best way to implement a thread-safe singleton, cf.
//		 * http://www.journaldev.com/171/thread-safety-in-java-singleton-classes-with-example-code
//		 * -FK-
//		 */
//		if (instance == null) {
//			synchronized (ActionDescriptionStore.class) {
//				if (instance == null) instance = new ActionDescriptionStore();
//			}
//		}
//		return instance;
//	}

	public boolean addEventMatch(EventMatch e) {
		ArrayList<EventMatch> eventMatchList = this.eventMatchList.get(e.getAction());
		if (eventMatchList == null)
			eventMatchList = new ArrayList<EventMatch>();
		eventMatchList.add(e);

		this.eventMatchList.put(e.getAction(), eventMatchList);
		return true;
	}

	public void addMechanism(Mechanism m) {
		ArrayList<Mechanism> mechanismList = this.mechanismList.get(m.getTriggerEvent().getAction());
		if (mechanismList == null)
			mechanismList = new ArrayList<Mechanism>();
		mechanismList.add(m);

		this.mechanismList.put(m.getTriggerEvent().getAction(), mechanismList);
	}

	public ArrayList<EventMatch> getEventList(String eventAction) {
		if (eventAction==null) return null;
		return this.eventMatchList.get(eventAction);
	}

	public ArrayList<Mechanism> getMechanismList(String eventAction) {
		ArrayList<Mechanism> result = new ArrayList<Mechanism>();
		ArrayList<Mechanism> matchingEvent = this.mechanismList.get(eventAction);
		ArrayList<Mechanism> matchingStar = this.mechanismList.get(Settings.getInstance().getStarEvent());
		if (matchingEvent!=null) result.addAll(matchingEvent);
		if (matchingStar!=null) result.addAll(matchingStar);
		if (result.size()!=0) return result;
		return null;
	}

	public boolean removeMechanism(String eventAction) {
		return this.mechanismList.remove(eventAction) == null;
	}
}
