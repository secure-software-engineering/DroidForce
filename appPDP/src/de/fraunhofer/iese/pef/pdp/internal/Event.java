package de.fraunhofer.iese.pef.pdp.internal;

import java.util.ArrayList;
import java.util.Hashtable;


  public class Event{

    private String eventAction;

    private boolean tryEvent;

    private int index;

    private long timestamp;

    private Hashtable<String, Param<?>> params = new Hashtable<String, Param<?>>();

    public Event() {
        this.eventAction = "noName";
        this.tryEvent = true;
        //this.index = Constants.IDX_ONGOING;
        this.timestamp = System.currentTimeMillis();
    }

    public Event(String action, boolean isTry) {
        this.eventAction = action;
        this.tryEvent = isTry;
        //this.index = Constants.IDX_ONGOING;
        this.timestamp = System.currentTimeMillis();
    }

    public Event(String action, boolean isTry, int index, long time) {
        this.eventAction = action;
        this.tryEvent = isTry;
        this.index = index;
        this.timestamp = time;
    }

    public void addParam(Param<?> param) {
        params.put(param.getName(), param);
    }

    public void removeParam(Param<?> param) {
        params.remove(param);
    }

    /**
     * @return the eventName
     */
    public String getEventAction() {
        return eventAction;
    }

    /**
     * @param eventAction the eventName to set
     */
    public void setEventAction(String eventAction) {
        this.eventAction = eventAction;
    }

    /**
     * @return the isTry
     */
    public boolean isTryEvent() {
        return tryEvent;
    }

    /**
     * @param isTry the isTry to set
     */
    public void setTryEvent(boolean isTry) {
        this.tryEvent = isTry;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the params
     */
    public ArrayList<Param<?>> getParams() {
        return new ArrayList<Param<?>>(params.values());
    }

    /**
     * @param params the params to set
     */
    public void setParams(Hashtable<String, Param<?>> params) {
        this.params = params;
    }

    public Param<?> getParameterForName(String name) {
        return params.get(name);
    }

    public Object getParameterValue(String name) {
        Param<?> param = params.get(name);
        if (param != null) {
            return params.get(name).getValue();
        }
        return null;
    }

    public void clear() {
        params.clear();
    }

    public void addStringParameter(String name, String value) {
        if (value != null) {
            addParam(new Param<String>(name, value, Constants.PARAMETER_TYPE_STRING));
        }
    }

    public void addIntParameter(String name, int value) {
        addParam(new Param<Integer>(name, value, Constants.PARAMETER_TYPE_INT));
    }

    public void addBooleanParameter(String name, boolean value) {
        addParam(new Param<Boolean>(name, value, Constants.PARAMETER_TYPE_BOOL));
    }

    public void addLongParameter(String name, long value) {
        addParam(new Param<Long>(name, value, Constants.PARAMETER_TYPE_LONG));
    }

    public void addStringArrayParameter(String name, String[] value) {
        if (value != null) {
            addParam(new Param<String[]>(name, value, Constants.PARAMETER_TYPE_STRING_ARRAY));
        }
    }

    public void addByteArrayParameter(String name, byte[] value) {
        if (value != null) {
            Param<byte[]> param = new Param<byte[]>(name, value, Constants.PARAMETER_TYPE_BINARY);
            addParam(param);
        }
    }

    public String getStringParameter(String name) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof String) {
                return (String)o;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public int getIntParameter(String name, int defaultValue) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof Integer) {
                return (Integer)o;
            }
        } catch (Exception e) {
            return defaultValue;
        }
        return defaultValue;
    }

    public boolean getBooleanParameter(String name, boolean defaultValue) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof Boolean) {
                return (Boolean)o;
            }
        } catch (Exception e) {
            return defaultValue;
        }
        return defaultValue;
    }

    public long getLongParameter(String name, long defaultValue) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof Long) {
                return (Long)o;
            }
        } catch (Exception e) {
            return defaultValue;
        }
        return defaultValue;
    }

    public byte[] getByteArrayParameter(String name) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof byte[]) {
                return (byte[])o;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public String[] getStringArrayParameter(String name) {
        try {
            Object o = getParameterForName(name).getValue();
            if (o instanceof String[]) {
                return (String[])o;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public String toString() {
        String str = "Event: " + eventAction + "\n";
        for (Param<?> param : params.values()) {
            str += param.toString() + "\n";
        }
        return str;
    }

}
