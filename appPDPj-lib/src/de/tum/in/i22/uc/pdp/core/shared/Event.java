package de.tum.in.i22.uc.pdp.core.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import de.tum.in.i22.uc.cm.datatypes.basic.EventBasic;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;

public class Event implements Serializable {
	private static final long serialVersionUID = 6399332064987815074L;

	protected String eventAction;
	protected boolean tryEvent;
	protected long timestamp;
	protected Hashtable<String, Param<?>> params = new Hashtable<String, Param<?>>();

	public Event() {
		this.eventAction = "noName";
		this.tryEvent = true;
		this.timestamp = System.currentTimeMillis();
	}


	/***
	 * Generate an (IESE) event out of a given (TUM) event
	 * @param ev
	 */
	public Event(IEvent ev) {
		if (ev != null) {
			this.eventAction = ev.getName();

			// NOTE that TUM events have isActual() while IESE events have
			// isTry()
			this.tryEvent = !ev.isActual();

			// TUM events only have strings parameters
			if (ev.getParameters() != null) {
				for (Map.Entry<String, String> entry : ev.getParameters()
						.entrySet()) {
					addStringParameter(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public Event(String action, boolean isTry) {
		this.eventAction = action;
		this.tryEvent = isTry;
		this.timestamp = System.currentTimeMillis();
	}

	public Event(String action, boolean isTry, long time) {
		this.eventAction = action;
		this.tryEvent = isTry;
		this.timestamp = time;
	}

	public void addParam(Param<?> param) {
		params.put(param.getName(), param);
	}

	public void removeParam(Param<?> param) {
		params.remove(param);
	}

	public String getEventAction() {
		return eventAction;
	}

	public void setEventAction(String eventAction) {
		this.eventAction = eventAction;
	}

	public boolean isTryEvent() {
		return tryEvent;
	}

	public void setTryEvent(boolean isTry) {
		this.tryEvent = isTry;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public ArrayList<Param<?>> getParams() {
		return new ArrayList<Param<?>>(params.values());
	}

	public void setParams(Hashtable<String, Param<?>> params) {
		this.params = params;
	}

	public Param<?> getParameterForName(String name) {
		return params.get(name);
	}

	public Object getParameterValue(String name) {
		Param<?> param = params.get(name);
		if (param != null)
			return params.get(name).getValue();
		return null;
	}

	public void clear() {
		params.clear();
	}

	public void addStringParameter(String name, String value) {
		if (value != null)
			addParam(new Param<String>(name, value,
					Constants.PARAMETER_TYPE_STRING));
	}

//	public void addIntParameter(String name, int value) {
//		addParam(new Param<Integer>(name, value, Constants.PARAMETER_TYPE_INT));
//	}
//
//	public void addBooleanParameter(String name, boolean value) {
//		addParam(new Param<Boolean>(name, value, Constants.PARAMETER_TYPE_BOOL));
//	}

//	public void addLongParameter(String name, long value) {
//		addParam(new Param<Long>(name, value, Constants.PARAMETER_TYPE_LONG));
//	}
//
//	public void addStringArrayParameter(String name, String[] value) {
//		if (value != null)
//			addParam(new Param<String[]>(name, value,
//					Constants.PARAMETER_TYPE_STRING_ARRAY));
//	}

//	public void addByteArrayParameter(String name, byte[] value) {
//		if (value != null) {
//			Param<byte[]> param = new Param<byte[]>(name, value,
//					Constants.PARAMETER_TYPE_BINARY);
//			addParam(param);
//		}
//	}

	public String getStringParameter(String name) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof String)
				return (String) o;
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public int getIntParameter(String name, int defaultValue) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof Integer)
				return (Integer) o;
		} catch (Exception e) {
			return defaultValue;
		}
		return defaultValue;
	}

	public boolean getBooleanParameter(String name, boolean defaultValue) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (Exception e) {
			return defaultValue;
		}
		return defaultValue;
	}

	public long getLongParameter(String name, long defaultValue) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof Long)
				return (Long) o;
		} catch (Exception e) {
			return defaultValue;
		}
		return defaultValue;
	}

	public byte[] getByteArrayParameter(String name) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof byte[])
				return (byte[]) o;
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public String[] getStringArrayParameter(String name) {
		try {
			Object o = getParameterForName(name).getValue();
			if (o instanceof String[])
				return (String[]) o;
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	@Override
	public String toString() {
		String str = "Event      action='" + eventAction + "' isTry='"
				+ tryEvent + "' timestamp='" + timestamp + "': [";
		for (Param<?> param : params.values())
			str += param.toString() + ", ";
		str += "]";
		return str;
	}

	public IEvent toIEvent(){
		Map<String,String> m= new HashMap<String,String>();
		for (Param<?> p : getParams()){
			m.put(p.getName(),p.getValue().toString());
		}
		return new EventBasic(this.eventAction, m, !isTryEvent());
	}

}
